package com.lu.wxmask.util;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class EachUtil {

    public static void eachFieldValue(Object obj, Predicate<Object> consumer) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object v = field.get(obj);
                    boolean isBreak = consumer.test(v);
                    if (isBreak) {
                        break;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            try {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                break;
            }
        }
    }
}
