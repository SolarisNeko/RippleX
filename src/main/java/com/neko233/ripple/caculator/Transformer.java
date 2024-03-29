package com.neko233.ripple.caculator;

import com.neko233.ripple.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SolarisNeko
 * Date on 2022-05-01
 */
public class Transformer {


    public static Map<String, Object> transformObject2Map(Object obj, List<String> groupColumnNames, List<String> aggColumnNameList) {
        Map<String, Object> dataMap = new HashMap<>();
        // group by Column
        for (String groupColumn : groupColumnNames) {
            Object value = ReflectUtil.getValueByField(obj, groupColumn);
            if (value == null) {
                continue;
            }
            dataMap.put(groupColumn, value);
        }
        // aggregate Column
        for (String aggColumnName : aggColumnNameList) {
            Object value = ReflectUtil.getValueByField(obj, aggColumnName);
            if (value == null) {
                continue;
            }
            dataMap.put(aggColumnName, value);
        }
        return dataMap;
    }
}
