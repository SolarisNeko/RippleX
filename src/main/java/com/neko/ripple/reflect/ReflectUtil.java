package com.neko.ripple.reflect;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author SolarisNeko
 * @date 2022-02-22
 */
public class ReflectUtil {


    public static Object getValueByField(Object data, String fieldName) {
        Object value;
        Field field;
        Class<?> aClass = data.getClass();
        try {
            field = aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        Class<?> superclass = aClass.getSuperclass();
        if (field == null) {
            field = getSuperFieldShortly(superclass, fieldName);
        }
        if (field == null) {
            return null;
        }

        field.setAccessible(true);
        try {
            value = field.get(data);
        } catch (IllegalAccessException e) {
            return null;
        }
        return value;
    }

    private static Field getSuperFieldShortly(Class<?> superclass, String fieldName) {
        while (!"Object".equals(superclass.getSimpleName())) {
            Field field;
            try {
                field = superclass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = null;
            }
            if (field != null) {
                return field;
            }
            superclass = superclass.getSuperclass();
        }
        return null;
    }

    public static List<Field> getFieldsRecursive(Class<?> schema) {
        List<Field> fields = new ArrayList<>();
        Class<?> temp = schema;
        while (!"Object".equals(temp.getSimpleName())) {
            Field[] declaredFields = temp.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            temp = temp.getSuperclass();
        }
        return fields;
    }
}
