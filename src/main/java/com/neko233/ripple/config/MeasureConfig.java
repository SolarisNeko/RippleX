package com.neko233.ripple.config;

import com.neko233.ripple.constant.AggregateType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SolarisNeko
 * Date on 2022-05-01
 */
public class MeasureConfig {

    private final Map<String, AggregateType> aggregateTypeMap = new HashMap<>();

    public static MeasureConfig builder() {
        return new MeasureConfig();
    }

    public MeasureConfig set(String columnName, AggregateType aggregateType) {
        aggregateTypeMap.put(columnName, aggregateType);
        return this;
    }

    public Map<String, AggregateType> build() {
        return aggregateTypeMap;
    }

    /**
     * 外部使用
     * @param columnName 列名
     * @return 聚合类型
     */
    public AggregateType get(String columnName) {
        return aggregateTypeMap.get(columnName);
    }

}
