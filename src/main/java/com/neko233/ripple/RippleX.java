package com.neko233.ripple;

import com.neko233.ripple.caculator.Transformer;
import com.neko233.ripple.config.MeasureConfig;
import com.neko233.ripple.constant.AggregateType;
import com.neko233.ripple.orm.Map2InstanceOrm;
import com.neko233.ripple.strategy.merge.MergeStrategy;
import com.neko233.ripple.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Dimension/Measure Theory + ETL Thought
 *
 * @author SolarisNeko
 * @date 2022-02-22
 **/
@Slf4j
public class RippleX {

    private static final String DELIMITER = ",";

    /**
     * 1. MetaData
     */
    private Class<?> schema;
    private List<?> dataList;
    private MeasureConfig measureConfig;
    private List<String> groupColumnNames;
    private List<String> excludeColumnList;
    private List<String> aClassAllColumnName;
    private ArrayList<String> aggColumnNameList;
    private ArrayList<String> keepColumnNames;

    /**
     * 2. Calculate Needed Data
     */
    private int fieldNameSize = 0;
    private boolean IS_EXISTED_CACHE_FLAG = false;
    private String CURRENT_GROUP_BY_KEY = null;
    /**
     * groupByKey : toNextAggregateDataMap< FieldName, value >
     */
    private Map<String, Map<String, Object>> aggMapCache = new HashMap<>();

    /**
     * 3. Response
     */
    private List<Map<String, Object>> resultMapList = new ArrayList<>();


    private RippleX() {
    }

    public static RippleX builder() {
        return new RippleX();
    }

    public <T> RippleX data(List<T> dataList) {
        this.dataList = dataList;
        return this;
    }

    /**
     * 字段的聚合，使用的操作，Map<columnName, Operate>
     */
    public RippleX measureConfig(MeasureConfig measureConfig) {
        this.measureConfig = measureConfig;
        return this;
    }

    public RippleX dimensionColumnNames(String... groupColumnNames) {
        this.groupColumnNames = Arrays.asList(groupColumnNames);
        return this;
    }

    public RippleX dimensionColumnNames(List<String> groupColumnNames) {
        this.groupColumnNames = groupColumnNames;
        return this;
    }

    public RippleX excludeColumnNames(String... excludeColumnNames) {
        this.excludeColumnList = Arrays.asList(excludeColumnNames);
        return this;
    }

    public RippleX excludeColumnNames(List<String> excludeColumnNames) {
        this.excludeColumnList = excludeColumnNames;
        return this;
    }

    public RippleX returnType(Class<?> schemaClass) {
        this.schema = schemaClass;
        return this;
    }

    /**
     * build
     *
     * @return 构建出分组计算后的 List<Map>
     */
    public <T> List<T> build() {

        checkSchema();
        // get Map
        List<Map<String, Object>> aggregateDataMapList = getAggregateDataMapList();
        // orm
        return (List<T>) Map2InstanceOrm.orm(aggregateDataMapList, schema);

    }

    private List<Map<String, Object>> getAggregateDataMapList() {
        // 1. set config options
        boolean isConfigSuccess = rememberAndSetConfigOptions();
        if (!isConfigSuccess) {
            return new ArrayList<>();
        }


        List<Map<String, Object>> dataMapList = dataList.stream()
                .map(obj -> Transformer.transformObject2Map(obj, groupColumnNames, aggColumnNameList))
                .collect(Collectors.toList());


        Map<String, List<Map<String, Object>>> groupByDimensionValueMap = dataMapList.stream()
                .collect(Collectors.groupingBy(map -> {
                    List<String> valueStringList = new ArrayList<>();
                    for (String groupColumnName : groupColumnNames) {
                        String valueString = String.valueOf(map.get(groupColumnName));
                        valueStringList.add(valueString);
                    }
                    return String.join(DELIMITER, valueStringList);
                }));

        // TODO 指标滚动计算
        List<Map<String, Object>> outputMeasureMapList = groupByDimensionValueMap.entrySet().stream()
                .map(entry -> {
                    List<Map<String, Object>> v = entry.getValue();
                    Map<String, Object> aggMap = new HashMap<>();
                    // 必定是同一组
                    for (Map<String, Object> dataMap : v) {
                        // 指标名
                        for (String aggColumnName : aggColumnNameList) {
                            AggregateType aggregateType = measureConfig.get(aggColumnName);
                            if (aggregateType == null) {
                                continue;
                            }
                            MergeStrategy strategy = MergeStrategy.choose(aggregateType);
                            // 滚动计算的值
                            Object aggValue = dataMap.get(aggColumnName);
                            if (aggValue == null) {
                                continue;
                            }
                            BiFunction<? super Object, ? super Object, ?> mergeBiFunction = strategy.getMergeBiFunction(aggValue.getClass());
                            aggMap.merge(aggColumnName, aggValue, mergeBiFunction);
                        }
                    }
                    return aggMap;
                })
                .collect(Collectors.toList());


        // FIXME 2. aggregate calculate
//        for (Object data : dataList) {
//            // 获取 new Map / cached Map, 基于
//            Map<String, Object> aggMap = getGroupByMapOrCache(data);
//
//            for (String aggColName : aggColumnNameList) {
//                Object value = ReflectUtil.getValueByField(data, aggColName);
//                // 计算聚合
//                Aggregator.aggregateByStep(aggMap, measureConfig, aggColName, value);
//            }
//
//            // Keep not change column
//            setNotChangeColumnValue2AggMap(data, aggMap);
//
//            // 判断是否存在过
//            if (IS_EXISTED_CACHE_FLAG) {
//                IS_EXISTED_CACHE_FLAG = false;
//            } else {
//                resultMapList.add(aggMap);
//            }
//        }
        return outputMeasureMapList;
    }

    private void setNotChangeColumnValue2AggMap(Object data, Map<String, Object> dataMap) {
        for (String keepColName : keepColumnNames) {
            if (dataMap.get(keepColName) != null) {
                continue;
            }
            Object value = ReflectUtil.getValueByField(data, keepColName);
            if (value == null) {
                continue;
            }
            dataMap.put(keepColName, value);
        }
    }

    private boolean rememberAndSetConfigOptions() {
        List<Field> allColumns = ReflectUtil.getFieldsRecursive(schema);
        if (CollectionUtils.isEmpty(allColumns)) {
            return false;
        }

        // 1 all field
        aClassAllColumnName = allColumns.stream().map(Field::getName).collect(Collectors.toList());
        fieldNameSize = aClassAllColumnName.size();

        // 2 aggregate fields
        aggColumnNameList = new ArrayList<>(aClassAllColumnName);
        aggColumnNameList.removeAll(groupColumnNames);
        aggColumnNameList.removeAll(Optional.ofNullable(excludeColumnList).orElse(new ArrayList<>()));

        // 3
        keepColumnNames = new ArrayList<>(aClassAllColumnName);
        keepColumnNames.removeAll(aggColumnNameList);
        keepColumnNames.removeAll(Optional.ofNullable(excludeColumnList).orElse(new ArrayList<>()));
        return true;
    }

    private void checkSchema() {
        if ("Object".equals(schema.getSimpleName())) {
            throw new RuntimeException("Object can't be a schema because it have no fields.");
        }
    }

    /**
     * group By [N column] : 1 Map
     * "column_value_1,column_value_2" : aggregate Map
     *
     * @param obj 处理的数据
     * @return group By Map
     */
    private Map<String, Object> getGroupByMapOrCache(Object obj) {
        Map<String, Object> cacheMap = getMapFromCacheByObjectValues(obj);
        if (MapUtils.isNotEmpty(cacheMap)) {
            IS_EXISTED_CACHE_FLAG = true;
            return cacheMap;
        }

        // 不存在， 构建一个 Aggregate Map
        Map<String, Object> dataMap = new HashMap<>();
        for (String groupColumn : groupColumnNames) {
            Object value = ReflectUtil.getValueByField(obj, groupColumn);
            if (value == null) {
                continue;
            }
            dataMap.put(groupColumn, value);
        }
        // 放入缓存
        aggMapCache.put(CURRENT_GROUP_BY_KEY, dataMap);
        return dataMap;
    }

    /**
     * 尝试获取已经存在的 GroupByMap
     *
     * @param data data
     * @return
     */
    private Map<String, Object> getMapFromCacheByObjectValues(Object data) {
        List<String> valueStrings = getColumnValueStrList(data, groupColumnNames);
        CURRENT_GROUP_BY_KEY = String.join(DELIMITER, valueStrings);
        return aggMapCache.get(CURRENT_GROUP_BY_KEY);
    }

    private List<String> getColumnValueStrList(Object data, List<String> columnList) {
        List<String> valueStrings = new ArrayList<>();
        for (String col : columnList) {
            Object valueByField = ReflectUtil.getValueByField(data, col);
            valueStrings.add(String.valueOf(valueByField));
        }
        return valueStrings;
    }


}
