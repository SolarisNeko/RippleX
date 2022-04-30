package com.neko233.ripple;

import com.neko233.ripple.caculator.Aggregator;
import com.neko233.ripple.constant.AggregateType;
import com.neko233.ripple.orm.Map2InstanceOrm;
import com.neko233.ripple.util.ReflectUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dimension/Measure Theory + ETL Thought
 *
 * @author SolarisNeko
 * @date 2022-02-22
 **/
public class RippleX {

    private static final String DELIMITER = ",";

    /**
     * 1. MetaData
     */
    private Class<?> schema;
    private List<?> dataList;
    private Map<String, AggregateType> aggregateRelationMap;
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
    public RippleX aggregateRelationMap(Map<String, AggregateType> aggregateRelationMap) {
        this.aggregateRelationMap = aggregateRelationMap;
        return this;
    }

    public RippleX groupColumnNames(String... groupColumnNames) {
        this.groupColumnNames = Arrays.asList(groupColumnNames);
        return this;
    }

    public RippleX groupColumnNames(List<String> groupColumnNames) {
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
        // 1. check
        List<Field> allColumns = ReflectUtil.getFieldsRecursive(schema);
        if (CollectionUtils.isEmpty(allColumns)) {
            return new ArrayList<>();
        }

        // 2. set config options
        rememberAndSetConfigOptions(allColumns);

        // 3. aggregate calculate
        for (Object data : dataList) {
            // 获取 new Map / cached Map, 基于
            Map<String, Object> aggMap = getGroupByMapOrCache(data);

            for (String aggColName : aggColumnNameList) {
                Object value = ReflectUtil.getValueByField(data, aggColName);
                // 计算聚合
                Aggregator.aggregateByStep(aggMap, aggregateRelationMap, aggColName, value);
            }

            // Keep not change column
            setNotChangeColumnValue2AggMap(data, aggMap);

            // 判断是否存在过
            if (IS_EXISTED_CACHE_FLAG) {
                IS_EXISTED_CACHE_FLAG = false;
            } else {
                resultMapList.add(aggMap);
            }
        }
        return resultMapList;
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

    private void rememberAndSetConfigOptions(List<Field> allColumns) {
        // 1 all field
        aClassAllColumnName = allColumns.stream().map(Field::getName).collect(Collectors.toList());
        fieldNameSize = aClassAllColumnName.size();

        // 2 aggregate fields
        aggColumnNameList = new ArrayList<>(aClassAllColumnName);
        aggColumnNameList.removeAll(groupColumnNames);
        aggColumnNameList.removeAll(excludeColumnList);

        // 3
        keepColumnNames = new ArrayList<>(aClassAllColumnName);
        keepColumnNames.removeAll(aggColumnNameList);
        keepColumnNames.removeAll(excludeColumnList);
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
