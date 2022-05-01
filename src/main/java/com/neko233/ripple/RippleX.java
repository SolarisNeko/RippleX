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
//    private ArrayList<String> keepColumnNames;

    /**
     * 2. Calculate Needed Data
     */
    private int fieldNameSize = 0;
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
        // 1. set config options
        boolean isConfigSuccess = rememberAndSetConfigOptions();
        if (!isConfigSuccess) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> aggregateDataMapList = getAggregateDataMapList();
        // orm
        return (List<T>) Map2InstanceOrm.orm(aggregateDataMapList, schema);

    }

    private List<Map<String, Object>> getAggregateDataMapList() {
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
        return groupByDimensionValueMap.values().stream()
                .map(v -> {
                    Map<String, Object> aggMap = new HashMap<>();
                    // 必定是同一组
                    for (Map<String, Object> dataMap : v) {
                        // 指标名
                        for (String aggColumnName : aggColumnNameList) {
                            AggregateType aggregateType = measureConfig.getAggregateType(aggColumnName);
                            if (aggregateType == null) {
                                continue;
                            }
                            // 滚动计算的值
                            Object aggValue = dataMap.get(aggColumnName);
                            if (aggValue == null) {
                                continue;
                            }
                            MergeStrategy strategy = MergeStrategy.choose(aggregateType);
                            BiFunction<? super Object, ? super Object, ?> mergeBiFunction = strategy
                                    .getMergeBiFunction(aggValue.getClass());

                            // input -> Merge Algorithm -> output
                            String outputColumnName = measureConfig.getOutputColumnName(aggColumnName);
                            if (aggregateType == AggregateType.COUNT) {
                                aggMap.merge(outputColumnName, 1, mergeBiFunction);
                            } else if (aggregateType == AggregateType.KEEP_FIRST) {
                                aggMap.merge(aggColumnName, aggValue, (v1, v2) -> v1);
                            } else {
                                aggMap.merge(outputColumnName, aggValue, mergeBiFunction);
                            }
                        }
                    }
                    return aggMap;
                })
                .collect(Collectors.toList());
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
        // TODO 是否保留 group By 信息
//        aggColumnNameList.removeAll(groupColumnNames);
        aggColumnNameList.removeAll(Optional.ofNullable(excludeColumnList).orElse(new ArrayList<>()));

        // 3
//        List<String> keepColumnNames = new ArrayList<>(aClassAllColumnName);
//        keepColumnNames.removeAll(aggColumnNameList);
//        keepColumnNames.removeAll(Optional.ofNullable(excludeColumnList).orElse(new ArrayList<>()));
//        for (String keepColumnName : keepColumnNames) {
//            measureConfig.set(keepColumnName, AggregateType.KEEP_FIRST, keepColumnName);
//        }
        return true;
    }

    private void checkSchema() {
        if ("Object".equals(schema.getSimpleName())) {
            throw new RuntimeException("Object can't be a schema because it have no fields.");
        }
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
