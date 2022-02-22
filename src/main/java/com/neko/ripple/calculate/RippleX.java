package com.neko.ripple.calculate;

import com.neko.ripple.ReflectUtil;
import com.neko.ripple.constant.AggregateOption;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author SolarisNeko
 * @date 2022-02-22
 **/
public class RippleX {

    private List dataList;
    private Map<String, AggregateOption> aggOperateMap;
    private List<String> groupColumnList;
    private List<String> exceptColumnList;

    private List<String> allColNameList;
    private ArrayList<String> aggColumnNameList;
    private ArrayList<String> keepColList;
    int count = 0;

    String currentCacheKey = null;
    Map<String, Map> groupByMapCache = new HashMap<>();

    List<Map<String, Object>> aggMapList = new ArrayList<>();

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
    public RippleX aggOperateMap(Map<String, AggregateOption> aggOperateMap) {
        this.aggOperateMap = aggOperateMap;
        return this;
    }

    public RippleX groupColumns(List<String> groupColumnList) {
        this.groupColumnList = groupColumnList;
        return this;
    }


    public RippleX except(List<String> exceptColumnList) {
        this.exceptColumnList = exceptColumnList;
        return this;
    }

    public List<Map<String, Object>> build() {

        Object o = dataList.get(0);
        Field[] allColumns = o.getClass().getDeclaredFields();

        // 1 all field
        allColNameList = Arrays.stream(allColumns).map(Field::getName).collect(Collectors.toList());
        count = allColNameList.size();

        // 2 aggregate fields
        aggColumnNameList = new ArrayList<>(allColNameList);
        aggColumnNameList.removeAll(groupColumnList);
        aggColumnNameList.removeAll(exceptColumnList);

        // 3
        keepColList = new ArrayList<>(allColNameList);
        keepColList.removeAll(aggColumnNameList);
        keepColList.removeAll(exceptColumnList);

        // 2、Aggregate
        // aggregate Field 必须是 Number 类型

        for (Object data : dataList) {
            Map<String, Object> aggMap = getGroupByMap(data);

            // 对 Group By Map 进行 agg / create once 操作
            for (String aggCol : aggColumnNameList) {
                Object value = ReflectUtil.getValueByField(data, aggCol);

                Integer aggValue = Integer.valueOf(String.valueOf(
                    Optional.ofNullable(value).orElse("1")
                ));

                // Aggregate 应该动态选择使用
                handleAggregateByType(aggOperateMap, aggMap, aggCol, aggValue);
            }
            // Keep col
            for (String keepCol : keepColList) {
                if (aggMap.get(keepCol) != null) {
                    continue;
                }
                Object value = ReflectUtil.getValueByField(data, keepCol);
                if (value == null) {
                    continue;
                }
                String keepColValue = String.valueOf(
                    Optional.ofNullable(value).orElse("")
                );

                if (keepColValue == null) {
                    continue;
                }
                aggMap.put(keepCol, keepColValue);
            }

            aggMapList.add(aggMap);
        }

        return aggMapList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * group By [N column] : 1 Map
     *
     * @param data 处理的数据
     * @return group By Map
     */
    private Map<String, Object> getGroupByMap(Object data) {

        // 通过 group By column... 确认一个 Map
        Map<String, Object> aggMap = getExistsGroupByMap(data, groupColumnList);
        if (aggMap != null) {
            return aggMap;
        }

        aggMap = new HashMap<>();

        // 不存在， 构建一个 Group By Map
        for (String groupColumn : groupColumnList) {
            Object value = ReflectUtil.getValueByField(data, groupColumn);
            if (value == null) {
                continue;
            }

            aggMap.put(groupColumn, value);
        }

        groupByMapCache.put(currentCacheKey, aggMap);
        return aggMap;
    }

    private Map getExistsGroupByMap(Object data, List<String> groupColumnList) {
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


    private static void handleAggregateByType(Map<String, AggregateOption> aggOperateMap, Map<String, Object> outputMap, String aggCol, Integer aggValue) {
        AggregateOption aggType = aggOperateMap.get(aggCol);
        switch (aggType) {
            case SUM: {
                outputMap.merge(aggCol, aggValue, (t1, t2) -> (Integer) t1 + (Integer) t2);
                break;
            }
            case COUNT: {
                outputMap.merge(aggCol, aggValue, (t1, t2) -> (Integer) t1 + 1);
                break;
            }
            case MAX: {
                outputMap.merge(aggCol, aggValue, (t1, t2) -> (Integer) t1 > (Integer) t2 ? t1 : t2);
                break;
            }
            case MIN: {
                outputMap.merge(aggCol, aggValue, (t1, t2) -> (Integer) t1 < (Integer) t2 ? t1 : t2);
                break;
            }
            default: {
                break;
            }
        }
    }

    private static void checkTypeByAggregateOption(Map<String, AggregateOption> aggOperateMap, Field aggField) {
        String name = aggField.getName();
        AggregateOption aggregateOption = aggOperateMap.get(name);

        switch (aggregateOption) {
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


}