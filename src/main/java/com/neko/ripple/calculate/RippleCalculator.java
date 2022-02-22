package com.neko.ripple.calculate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author LuoHaoJun
 * @date 2022-02-22
 **/
public class RippleCalculator {


    /**
     * All columns = agg columns + group columns
     * columns must be Number.
     *
     * @param dataList     数据
     * @param groupColumns 分类用的 Column
     * @return
     */
    public static Map<String, Integer> ripple(List dataList, String aggOpt, String... groupColumns) throws IllegalAccessException, NoSuchFieldException {
        List<String> groupColumnList = Arrays.asList(groupColumns);

        Object o = dataList.get(0);
        Field[] declaredFields = o.getClass().getDeclaredFields();

        // 获取 not group by columns = 需要 aggregate columns
        List<String> aggColList = new ArrayList<>();
        for (Field field : declaredFields) {
            String fieldName = field.getName();
            if (groupColumnList.contains(fieldName)) {
                continue;
            }
            aggColList.add(fieldName);
        }

        for (String groupCol : groupColumnList) {
            aggColList.remove(groupCol);
        }

        // aggregate Column 必须是数字
        Map<String, Integer> aggMap = new HashMap();
        for (String aggCol : aggColList) {
            aggMap.put(aggCol, 0);
        }

        for (Object data : dataList) {
            for (String aggCol : aggColList) {
                Field declaredField = data.getClass().getDeclaredField(aggCol);
                declaredField.setAccessible(true);
                Integer aggValue = Integer.valueOf(String.valueOf(Optional.ofNullable(declaredField.get(data)).orElse("0")));

                aggMap.merge(aggCol, aggValue, Integer::sum);
            }
        }

        return aggMap;


    }


}
