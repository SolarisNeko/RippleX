package com.neko.ripple;

import java.lang.reflect.Field;

/**
 * @author SolarisNeko
 * @date 2022-02-22
 */
public class ReflectUtil {


    public static Object getValueByField(Object data, String groupColumn) {
        Object value;
        Field field;
        try {
            field = data.getClass().getDeclaredField(groupColumn);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        if (field == null) {

        }

        field.setAccessible(true);
        try {
            value = field.get(data);
        } catch (IllegalAccessException e) {
            return null;
        }
        return value;
    }
}
