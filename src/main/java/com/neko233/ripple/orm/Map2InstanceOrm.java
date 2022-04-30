package com.neko233.ripple.orm;

import com.neko233.ripple.strategy.transform.TransformValueStrategy;
import com.neko233.ripple.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author SolarisNeko
 * Date on 2022-04-30
 */
@Slf4j
public class Map2InstanceOrm {


    public static <T> List<T> orm(List<Map<String, Object>> mapList, Class<T> schema) {
        return mapList.stream().map(map -> (T) orm(map, schema)).collect(Collectors.toList());
    }

    public static <T> T orm(Map<String, Object> dataMap, Class<?> schema) {
        List<Field> fieldList = ReflectUtil.getFieldsRecursive(schema);
        if (CollectionUtils.isEmpty(fieldList)) {
            return null;
        }

        Object newInstance;
        try {
            newInstance = schema.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("New a instance error. ", e);
            return null;
        }

        for (Field field : fieldList) {
            Object fieldValue = dataMap.get(field.getName());
            field.setAccessible(true);
            try {
                Object transformValue = TransformValueStrategy.transform(field.getType(), fieldValue);
                field.set(newInstance, transformValue);
            } catch (IllegalAccessException e) {
                try {
                    field.set(newInstance, null);
                } catch (IllegalAccessException ex) {
                    log.error("Field = {} can't access.", field.getName());
                }
            }

        }

        return (T) newInstance;
    }

}
