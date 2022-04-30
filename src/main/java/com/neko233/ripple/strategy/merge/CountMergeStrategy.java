package com.neko233.ripple.strategy.merge;

import java.util.function.BiFunction;

/**
 * @author SolarisNeko
 * Date on 2022-04-30
 */
public class CountMergeStrategy implements MergeStrategy {

    private static final MergeStrategy INSTANCE = new CountMergeStrategy();

    public static MergeStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public BiFunction<? super Object, ? super Object, ?> merge(Class sumType) {
        return (t1, t2) -> (Integer) t1 + 1;
    }
}
