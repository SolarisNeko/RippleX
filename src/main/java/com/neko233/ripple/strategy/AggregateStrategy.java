package com.neko233.ripple.strategy;

import com.neko233.ripple.constant.AggregateType;
import com.neko233.ripple.strategy.merge.MergeStrategy;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author SolarisNeko
 * Date on 2022-04-30
 */
public class AggregateStrategy {

    public static void aggregate(Map<String, Object> aggregateDataMap, Map<String, AggregateType> aggTypeMap, String aggColName, Object aggValue) {
        AggregateType aggType = aggTypeMap.get(aggColName);
        if (aggType == null) {
            return;
        }
        String valueTypename = aggValue.getClass().getName();
        if (valueTypename == null) {
            return;
        }

        // 选择合并策略
        MergeStrategy mergeStrategy = MergeStrategy.choose(aggType);
        BiFunction<? super Object, ? super Object, ?> merge = mergeStrategy.merge(aggValue.getClass());
        if (aggType == AggregateType.COUNT) {
            aggregateDataMap.merge(aggColName, 1, merge);
        } else {
            aggregateDataMap.merge(aggColName, aggValue, merge);
        }
    }


}
