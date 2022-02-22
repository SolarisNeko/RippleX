package com.neko.ripple.calculate;

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
public class RippleCalculator {

    /**
     * All columns = agg columns + group columns
     * columns must be Number.
     *
     * @param dataList     数据
     * @param groupColumns 分类用的 Column
     * @param exceptColumnList
     * @return
     */
    public static Map<String, Object> ripple(List dataList, String aggOpt, List<String> groupColumnList, List<String> exceptColumnList) throws IllegalAccessException, NoSuchFieldException {

        Object o = dataList.get(0);
        Field[] allColumns = o.getClass().getDeclaredFields();
        List<String> allColNameList = Arrays.stream(allColumns).map(Field::getName).collect(Collectors.toList());

        // 获取 not group by columns = 需要 aggregate columns
        List<String> aggColumnNameList = new ArrayList<>();
        aggColumnNameList.addAll(allColNameList);
        // 去除
        aggColumnNameList.removeAll(groupColumnList);
        aggColumnNameList.removeAll(exceptColumnList);

        List<String> keepColList = allColNameList.stream().filter(col -> !aggColumnNameList.contains(col)).collect(Collectors.toList());

        // 2、Aggregate
        Map<String, Object> outputMap = new HashMap(allColumns.length);
        for (String aggCol : aggColumnNameList) {
            outputMap.put(aggCol, 0);
        }
        // aggregate Field 必须是 Number 类型
        for (Object data : dataList) {
            for (String aggCol : aggColumnNameList) {
                Field aggField = data.getClass().getDeclaredField(aggCol);
                checkNumberType(aggField);
                aggField.setAccessible(true);
                Integer aggValue = Integer.valueOf(String.valueOf(
                        Optional.ofNullable(aggField.get(data)).orElse("0")
                ));

                outputMap.merge(aggCol, aggValue, (t1, t2) -> (Integer) t1 + (Integer) t2);
            }
            for (String keepCol : keepColList) {
                Field keepField = data.getClass().getDeclaredField(keepCol);
                keepField.setAccessible(true);
                String keepColValue = String.valueOf(
                        Optional.ofNullable(keepField.get(data)).orElse("")
                );

                if (keepColValue == null) {
                    continue;
                }
                outputMap.put(keepCol, keepColValue);
            }
        }


        return outputMap;
    }

    private static void checkNumberType(Field aggField) {
        Class<?> type = aggField.getType();
        if (type != Integer.class) {
            throw new RuntimeException("column " + aggField.getName() + " can't be statistic. ");
        }
    }


}
