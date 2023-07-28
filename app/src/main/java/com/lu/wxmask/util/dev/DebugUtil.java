package com.lu.wxmask.util.dev;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.lu.lposed.api2.function.Predicate;
import com.lu.magic.util.GsonUtil;
import com.lu.magic.util.log.LogUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DebugUtil {
    public static Map<String, Map<String, Object>> getAllFields(Object obj) {
        if (obj == null) {
            return null;
        }
        HashMap<String, Map<String, Object>> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            Map<String, Object> fieldValueMap = collectFieldValueMap(clazz, obj);
            map.put(clazz.getName(), fieldValueMap);
            try {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                break;
            }
        }
        return map;
    }

    public static Field findFirstField(Class<?> clazz, Predicate<Field> predicate) {
        Class<?> clz = clazz;
        do {
            for (Field field : clz.getDeclaredFields()) {
                field.setAccessible(true);
                if (predicate.test(field)) {
                    return field;
                }
            }
        } while ((clz = clz.getSuperclass()) != null);
        return null;
    }

    public static void printAllFields(Object obj) {
        Map<String, Map<String, Object>> data = getAllFields(obj);
        try {
            LogUtil.d(GsonUtil.toJson(data));
        } catch (Exception e) {
            for (Map.Entry<String, Map<String, Object>> ele1 : data.entrySet()) {
                String k1 = ele1.getKey();

                Map<String, Object> v1 = ele1.getValue();

                Iterator<Map.Entry<String, Object>> iterV1 = v1.entrySet().iterator();
                while (iterV1.hasNext()) {
                    Map.Entry<String, Object> ele2 = iterV1.next();
                    String k2 = ele2.getKey();
                    Object v2 = ele2.getValue();
                    JsonElement jsonV2 = null;
                    try {
                        jsonV2 = GsonUtil.toJsonTree(v2);
                    } catch (Exception ex) {
                        new JsonPrimitive(v2 + "");
                    }
                    v1.put(k2, jsonV2);
                }
            }
            try {
                LogUtil.d("item data class: ", GsonUtil.toJson(data));
            } catch (Exception ex) {
                LogUtil.e(ex);
            }
        }

    }

    private static Map<String, Object> collectFieldValueMap(Class<?> clazz, Object obj) {
        HashMap<String, Object> map = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String k = field.getDeclaringClass() + "_" + field.getName();
                //String type = field.getType().getSimpleName();
                Object v = field.get(obj);
                map.put(k, v);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }
}
