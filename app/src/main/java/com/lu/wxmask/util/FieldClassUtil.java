package com.lu.wxmask.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author Lu
 * @date 2024/6/10 15:11
 * @description
 */
public class FieldClassUtil {

    public static List<String> getAllClassName(Object object) {
        List<String> classList = new ArrayList<>();
        Class<? extends Object> clazz;
        clazz = object.getClass();
        classList.add(clazz.getName());
        while (true) {
            Class<?> sClazz = clazz.getSuperclass();
            if (sClazz == null || sClazz == java.lang.Object.class) {
                break;
            }
            clazz = sClazz;
            classList.add(clazz.getName());
        }
        return classList;
    }

    public static List<Class<?>> getAllSuperClass(Object object) {
        List<Class<?>> classList = new ArrayList<>();
        Class<? extends Object> clazz;
        clazz = object.getClass();
        while (true) {
            Class<?> sClazz = clazz.getSuperclass();
            if (sClazz == null) {
                break;
            }
            clazz = sClazz;
            classList.add(clazz);
        }
        return classList;
    }

    public static List<String> getAllTextFieldList(Object firstObj) {
        HashMap<String, String> map = getAllTextFieldMap(firstObj);
        return new ArrayList<>(map.values());
    }

    public static HashMap<String, String> getAllTextFieldMap(Object firstObj) {
        HashMap<String, String> result = new HashMap<>();
        if (firstObj == null) {
            return result;
        }
        List<Object> fieldValueList = new ArrayList<>();
        fieldValueList.add(firstObj);
        while (!fieldValueList.isEmpty()) {
            Object obj = fieldValueList.get(0);
            Class<?> clazz = obj.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true); // 允许访问私有字段
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                Object fieldValue = null;
                try {
                    fieldValue = field.get(obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (fieldValue instanceof CharSequence) {
                    result.put(field.toString(), fieldValue.toString());
                } else if (fieldValue == null
                        || fieldValue instanceof Number
                        || fieldValue instanceof Byte
                        || fieldValue instanceof Boolean
                        || fieldValue.getClass() == Object.class
                        || TextKit.isContain(getAllClassName(fieldValue), "android")) {
                    continue;
                } else {
                    if (!fieldValueList.contains(fieldValue) && fieldValue != null) {
                        fieldValueList.add(fieldValue);
                    }
                }
            }
            if (!fieldValueList.isEmpty()) {
                fieldValueList.remove(0);
            }
        }
        return result;
    }

    public static void extractStringProperties(Object obj, List<String> properties, int stopStep) {
        extractStringProperties(obj, properties, 0, stopStep);
    }

    private static void extractStringProperties(Object obj, List<String> properties, int step, int stopStep) {
        if (obj == null) return;
        if (step > stopStep) {
            return;
        }
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            boolean con = false;
            try {
                field.setAccessible(true); // 允许访问私有字段
            } catch (Exception e) {
                con = true;
            }
            if (con) {
                continue;
            }
            Object fieldValue = null;
            try {
                fieldValue = field.get(obj);
            } catch (IllegalAccessException e) {
            }
            if (fieldValue instanceof CharSequence) {
                properties.add(fieldValue.toString());
            } else {
                if (fieldValue != null) {
                    // 如果字段是对象，则递归调用
                    extractStringProperties(fieldValue, properties, step + 1, stopStep);
                }
            }
        }
    }
}
