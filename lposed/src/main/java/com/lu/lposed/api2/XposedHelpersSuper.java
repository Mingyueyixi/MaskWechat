package com.lu.lposed.api2;

import android.content.res.Resources;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 代理了XposedBridge与XposedHelpers的所有方法，内部使用，用于子类覆盖继承，不提供外部使用
 */
class XposedHelpersSuper {

    public static int getXposedVersion() {
        return XposedBridge.getXposedVersion();
    }

    public static void log(String text) {
        XposedBridge.log(text);
    }

    public static void log(Throwable t) {
        XposedBridge.log(t);
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        return XposedBridge.hookMethod(hookMethod, callback);
    }

    @Deprecated
    public static void unhookMethod(Member hookMethod, XC_MethodHook callback) {
        XposedBridge.unhookMethod(hookMethod, callback);
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        return XposedBridge.hookAllMethods(hookClass, methodName, callback);
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        return XposedBridge.hookAllConstructors(hookClass, callback);
    }

    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args) throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return XposedBridge.invokeOriginalMethod(method, thisObject, args);
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        return XposedHelpers.findClass(className, classLoader);
    }

    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        return XposedHelpers.findClassIfExists(className, classLoader);
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return XposedHelpers.findField(clazz, fieldName);
    }

    public static Field findFieldIfExists(Class<?> clazz, String fieldName) {
        return XposedHelpers.findFieldIfExists(clazz, fieldName);
    }

    public static Field findFirstFieldByExactType(Class<?> clazz, Class<?> type) {
        return XposedHelpers.findFirstFieldByExactType(clazz, type);
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        return XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        return XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
        return XposedHelpers.findMethodExact(clazz, methodName, parameterTypes);
    }

    public static Method findMethodExactIfExists(Class<?> clazz, String methodName, Object... parameterTypes) {
        return XposedHelpers.findMethodExactIfExists(clazz, methodName, parameterTypes);
    }

    public static Method findMethodExact(String className, ClassLoader classLoader, String methodName, Object... parameterTypes) {
        return XposedHelpers.findMethodExact(className, classLoader, methodName, parameterTypes);
    }

    public static Method findMethodExactIfExists(String className, ClassLoader classLoader, String methodName, Object... parameterTypes) {
        return XposedHelpers.findMethodExactIfExists(className, classLoader, methodName, parameterTypes);
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return XposedHelpers.findMethodExact(clazz, methodName, parameterTypes);
    }

    public static Method[] findMethodsByExactParameters(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        return XposedHelpers.findMethodsByExactParameters(clazz, returnType, parameterTypes);
    }

    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return XposedHelpers.findMethodBestMatch(clazz, methodName, parameterTypes);
    }

    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Object... args) {
        return XposedHelpers.findMethodBestMatch(clazz, methodName, args);
    }

    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
        return XposedHelpers.findMethodBestMatch(clazz, methodName, parameterTypes, args);
    }

    public static Class<?>[] getParameterTypes(Object... args) {
        return XposedHelpers.getParameterTypes(args);
    }

    public static Class<?>[] getClassesAsArray(Class<?>... clazzes) {
        return XposedHelpers.getClassesAsArray(clazzes);
    }

    public static Constructor<?> findConstructorExact(Class<?> clazz, Object... parameterTypes) {
        return XposedHelpers.findConstructorExact(clazz, parameterTypes);
    }

    public static Constructor<?> findConstructorExactIfExists(Class<?> clazz, Object... parameterTypes) {
        return XposedHelpers.findConstructorExactIfExists(clazz, parameterTypes);
    }

    public static Constructor<?> findConstructorExact(String className, ClassLoader classLoader, Object... parameterTypes) {
        return XposedHelpers.findConstructorExact(className, classLoader, parameterTypes);
    }

    public static Constructor<?> findConstructorExactIfExists(String className, ClassLoader classLoader, Object... parameterTypes) {
        return XposedHelpers.findConstructorExactIfExists(className, classLoader, parameterTypes);
    }

    public static Constructor<?> findConstructorExact(Class<?> clazz, Class<?>... parameterTypes) {
        return XposedHelpers.findConstructorExact(clazz, parameterTypes);
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        return XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        return XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
    }

    public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Class<?>... parameterTypes) {
        return XposedHelpers.findConstructorBestMatch(clazz, parameterTypes);
    }

    public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Object... args) {
        return XposedHelpers.findConstructorBestMatch(clazz, args);
    }

    public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Class<?>[] parameterTypes, Object[] args) {
        return XposedHelpers.findConstructorBestMatch(clazz, parameterTypes, args);
    }

    public static void setObjectField(Object obj, String fieldName, Object value) {
        XposedHelpers.setObjectField(obj, fieldName, value);
    }

    public static void setBooleanField(Object obj, String fieldName, boolean value) {
        XposedHelpers.setBooleanField(obj, fieldName, value);
    }

    public static void setByteField(Object obj, String fieldName, byte value) {
        XposedHelpers.setByteField(obj, fieldName, value);
    }

    public static void setCharField(Object obj, String fieldName, char value) {
        XposedHelpers.setCharField(obj, fieldName, value);
    }

    public static void setDoubleField(Object obj, String fieldName, double value) {
        XposedHelpers.setDoubleField(obj, fieldName, value);
    }

    public static void setFloatField(Object obj, String fieldName, float value) {
        XposedHelpers.setFloatField(obj, fieldName, value);
    }

    public static void setIntField(Object obj, String fieldName, int value) {
        XposedHelpers.setIntField(obj, fieldName, value);
    }

    public static void setLongField(Object obj, String fieldName, long value) {
        XposedHelpers.setLongField(obj, fieldName, value);
    }

    public static void setShortField(Object obj, String fieldName, short value) {
        XposedHelpers.setShortField(obj, fieldName, value);
    }

    public static<E> E getObjectField(Object obj, String fieldName) {
        return (E) XposedHelpers.getObjectField(obj, fieldName);
    }

    public static Object getSurroundingThis(Object obj) {
        return XposedHelpers.getSurroundingThis(obj);
    }

    public static boolean getBooleanField(Object obj, String fieldName) {
        return XposedHelpers.getBooleanField(obj, fieldName);
    }

    public static byte getByteField(Object obj, String fieldName) {
        return XposedHelpers.getByteField(obj, fieldName);
    }

    public static char getCharField(Object obj, String fieldName) {
        return XposedHelpers.getCharField(obj, fieldName);
    }

    public static double getDoubleField(Object obj, String fieldName) {
        return XposedHelpers.getDoubleField(obj, fieldName);
    }

    public static float getFloatField(Object obj, String fieldName) {
        return XposedHelpers.getFloatField(obj, fieldName);
    }

    public static int getIntField(Object obj, String fieldName) {
        return XposedHelpers.getIntField(obj, fieldName);
    }

    public static long getLongField(Object obj, String fieldName) {
        return XposedHelpers.getLongField(obj, fieldName);
    }

    public static short getShortField(Object obj, String fieldName) {
        return XposedHelpers.getShortField(obj, fieldName);
    }

    public static void setStaticObjectField(Class<?> clazz, String fieldName, Object value) {
        XposedHelpers.setStaticObjectField(clazz, fieldName, value);
    }

    public static void setStaticBooleanField(Class<?> clazz, String fieldName, boolean value) {
        XposedHelpers.setStaticBooleanField(clazz, fieldName, value);
    }

    public static void setStaticByteField(Class<?> clazz, String fieldName, byte value) {
        XposedHelpers.setStaticByteField(clazz, fieldName, value);
    }

    public static void setStaticCharField(Class<?> clazz, String fieldName, char value) {
        XposedHelpers.setStaticCharField(clazz, fieldName, value);
    }

    public static void setStaticDoubleField(Class<?> clazz, String fieldName, double value) {
        XposedHelpers.setStaticDoubleField(clazz, fieldName, value);
    }

    public static void setStaticFloatField(Class<?> clazz, String fieldName, float value) {
        XposedHelpers.setStaticFloatField(clazz, fieldName, value);
    }

    public static void setStaticIntField(Class<?> clazz, String fieldName, int value) {
        XposedHelpers.setStaticIntField(clazz, fieldName, value);
    }

    public static void setStaticLongField(Class<?> clazz, String fieldName, long value) {
        XposedHelpers.setStaticLongField(clazz, fieldName, value);
    }

    public static void setStaticShortField(Class<?> clazz, String fieldName, short value) {
        XposedHelpers.setStaticShortField(clazz, fieldName, value);
    }

    public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticObjectField(clazz, fieldName);
    }

    public static boolean getStaticBooleanField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticBooleanField(clazz, fieldName);
    }

    public static byte getStaticByteField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticByteField(clazz, fieldName);
    }

    public static char getStaticCharField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticCharField(clazz, fieldName);
    }

    public static double getStaticDoubleField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticDoubleField(clazz, fieldName);
    }

    public static float getStaticFloatField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticFloatField(clazz, fieldName);
    }

    public static int getStaticIntField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticIntField(clazz, fieldName);
    }

    public static long getStaticLongField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticLongField(clazz, fieldName);
    }

    public static short getStaticShortField(Class<?> clazz, String fieldName) {
        return XposedHelpers.getStaticShortField(clazz, fieldName);
    }

    public static <T> T callMethod(Object obj, String methodName, Object... args) {
        return (T) XposedHelpers.callMethod(obj, methodName, args);
    }

    public static <T> T  callMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        return (T) XposedHelpers.callMethod(obj, methodName, parameterTypes, args);
    }

    public static <T> T  callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return (T) XposedHelpers.callStaticMethod(clazz, methodName, args);
    }

    public static <T> T  callStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        return (T) XposedHelpers.callStaticMethod(clazz, methodName, parameterTypes, args);
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        return XposedHelpers.newInstance(clazz, args);
    }

    public static Object newInstance(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        return XposedHelpers.newInstance(clazz, parameterTypes, args);
    }

    public static Object setAdditionalInstanceField(Object obj, String key, Object value) {
        return XposedHelpers.setAdditionalInstanceField(obj, key, value);
    }

    public static Object getAdditionalInstanceField(Object obj, String key) {
        return XposedHelpers.getAdditionalInstanceField(obj, key);
    }

    public static Object removeAdditionalInstanceField(Object obj, String key) {
        return XposedHelpers.removeAdditionalInstanceField(obj, key);
    }

    public static Object setAdditionalStaticField(Object obj, String key, Object value) {
        return XposedHelpers.setAdditionalStaticField(obj, key, value);
    }

    public static Object getAdditionalStaticField(Object obj, String key) {
        return XposedHelpers.getAdditionalStaticField(obj, key);
    }

    public static Object removeAdditionalStaticField(Object obj, String key) {
        return XposedHelpers.removeAdditionalStaticField(obj, key);
    }

    public static Object setAdditionalStaticField(Class<?> clazz, String key, Object value) {
        return XposedHelpers.setAdditionalStaticField(clazz, key, value);
    }

    public static Object getAdditionalStaticField(Class<?> clazz, String key) {
        return XposedHelpers.getAdditionalStaticField(clazz, key);
    }

    public static Object removeAdditionalStaticField(Class<?> clazz, String key) {
        return XposedHelpers.removeAdditionalStaticField(clazz, key);
    }

    public static byte[] assetAsByteArray(Resources res, String path) throws IOException {
        return XposedHelpers.assetAsByteArray(res, path);
    }

    public static String getMD5Sum(String file) throws IOException {
        return XposedHelpers.getMD5Sum(file);
    }

    public static int incrementMethodDepth(String method) {
        return XposedHelpers.incrementMethodDepth(method);
    }

    public static int decrementMethodDepth(String method) {
        return XposedHelpers.decrementMethodDepth(method);
    }

    public static int getMethodDepth(String method) {
        return XposedHelpers.getMethodDepth(method);
    }
}
