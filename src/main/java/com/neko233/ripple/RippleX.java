package com.neko233.ripple;

import com.neko233.ripple.constant.AggregateType;
import com.neko233.ripple.orm.Map2InstanceOrm;
import com.neko233.ripple.strategy.AggregateStrategy;
import com.neko233.ripple.util.ReflectUtil;
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

    private int fieldNameSize = 0;
    private boolean isMapCacheExist = false;
    private String currentCacheKey = null;
    private Map<String, Map<String, Object>> groupByMapCache = new HashMap<>();
    private List<Map<String, Object>> resultMapList = new ArrayList<>();
    private Class<?> schema;
    private List<?> dataList;
    private Map<String, AggregateType> aggregateRelationMap;
    private List<String> groupColumnNames;
    private List<String> excludeColumnList;
    private List<String> aClassAllColumnName;
    private ArrayList<String> aggColumnNameList;
    private ArrayList<String> keepColumnNames;

    private RippleX() {
    }

    public static RippleX builder() {
        return new RippleX();
    }


    private static void checkTypeByAggregateOption(Map<String, AggregateType> aggOperateMap, Field aggField) {
        String name = aggField.getName();
        AggregateType aggregateType = aggOperateMap.get(name);

        switch (aggregateType) {
            case SUM: {
                Class<?> type = aggField.getType();
                if (type != Integer.class) {
                    throw new RuntimeException("column " + aggField.getName() + " can't be statistic. ");
                }
                break;
            }
            default: {
                break;
            }
        }
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
     * @return 构建出分组计算后的 List<Map>
     */
    public <T> List<T> build() {

        checkSchema();
        List<Map<String, Object>> aggregateDataMapList = getAggregateDataMapList();

        // TODO - ORM
        return (List<T>) Map2InstanceOrm.orm(aggregateDataMapList, schema);

    }

    private List<Map<String, Object>> getAggregateDataMapList() {
        // schema 必须拥有和 dataList 一样的 fieldName, 否则统计统计失败
        List<Field> allColumns = ReflectUtil.getFieldsRecursive(schema);

        if (CollectionUtils.isEmpty(allColumns)) {
            return new ArrayList<>();
        }

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

        // 2、Aggregate
        // aggregate Field 必须是 Number 类型

        for (Object data : dataList) {
            Map<String, Object> dataMap = getGroupByMap(data);

            // 对 Group By Map 进行 agg / create once 操作
            for (String aggColName : aggColumnNameList) {
                Object value = ReflectUtil.getValueByField(data, aggColName);

                // Aggregate 应该动态选择使用
                AggregateStrategy.aggregate(dataMap, aggregateRelationMap, aggColName, value);
            }

            // Keep not change column
            for (String keepColName : keepColumnNames) {
                if (dataMap.get(keepColName) != null) {
                    continue;
                }
                Object value = ReflectUtil.getValueByField(data, keepColName);
                if (value == null) {
                    continue;
                }
                String keepColValue = String.valueOf(
                    Optional.ofNullable(value).orElse("")
                );

                if (keepColValue == null) {
                    continue;
                }
                dataMap.put(keepColName, keepColValue);
            }

            if (isMapCacheExist) {
                isMapCacheExist = resetCache();
            } else {
                resultMapList.add(dataMap);
            }
        }
        return resultMapList;
    }

    private void checkSchema() {
        if ("Object".equals(schema.getSimpleName())) {
            throw new RuntimeException("Object can't be a schema because it have no fields.");
        }
    }

    private boolean resetCache() {
        return false;
    }

    /**
     * group By [N column] : 1 Map
     * "column_value_1,column_value_2" : aggregate Map
     *
     * @param data 处理的数据
     * @return group By Map
     */
    private Map<String, Object> getGroupByMap(Object data) {
        Map<String, Object> aggMap = getExistsGroupByMap(data, groupColumnNames);
        if (aggMap != null) {
            isMapCacheExist = true;
            return aggMap;
        }

        aggMap = new HashMap<>();
        // 不存在， 构建一个 Group By Map
        for (String groupColumn : groupColumnNames) {
            Object value = ReflectUtil.getValueByField(data, groupColumn);
            if (value == null) {
                continue;
            }

            aggMap.put(groupColumn, value);
        }

        groupByMapCache.put(currentCacheKey, aggMap);
        return aggMap;
    }

    private Map<String, Object> getExistsGroupByMap(Object data, List<String> groupColumnList) {
        List<String> valueStrings = getColumnValueStrList(data, groupColumnList);
        String join = String.join(",", valueStrings);
        currentCacheKey = join;
        return groupByMapCache.get(join);
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
