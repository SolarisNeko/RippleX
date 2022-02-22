package com.neko.ripple.reflect;

import java.lang.reflect.Field;

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
}
