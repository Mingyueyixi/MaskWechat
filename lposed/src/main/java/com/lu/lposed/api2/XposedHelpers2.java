package com.lu.lposed.api2;

import com.lu.lposed.api2.function.Consumer;
import com.lu.lposed.api2.function.Predicate;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposedHelpers2 extends XposedHelpersSuper {
    public static class Config {
        private static Consumer<Throwable> throwableCallBack = Throwable::printStackTrace;

        static void onFailed(Throwable throwable) {
            if (throwableCallBack == null) {
                return;
            }
            throwableCallBack.accept(throwable);
        }

        public static void setThrowableCallBack(Consumer<Throwable> callback) {
            throwableCallBack = callback;
        }
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        return XposedBridge.hookMethod(hookMethod, createCallbackProxy(callback));
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        return XposedBridge.hookAllMethods(hookClass, methodName, createCallbackProxy(callback));
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        return XposedBridge.hookAllConstructors(hookClass, createCallbackProxy(callback));
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        modifyCallbackProxy(parameterTypesAndCallback);
        return XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        modifyCallbackProxy(parameterTypesAndCallback);
        return XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        modifyCallbackProxy(parameterTypesAndCallback);
        return XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        modifyCallbackProxy(parameterTypesAndCallback);
        return XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
    }


    private static void modifyCallbackProxy(Object... parameterTypesAndCallback) {
        // kotlin vararg可变参数不能这样直接修改数组的值，对方法之外的数据无效
        // 因为kotlin调用方法传递可变参数时，需要使用*修饰符，这个修饰符编译后实际复制了一份数组作为参数
        int lastIndex = parameterTypesAndCallback.length - 1;
        Object callback = parameterTypesAndCallback[lastIndex];
        if (callback instanceof XC_MethodHook) {
            parameterTypesAndCallback[lastIndex] = createCallbackProxy((XC_MethodHook) callback);
        }
    }

    public static XC_MethodHook createCallbackProxy(XC_MethodHook callback) {
        // XC_MethodReplacement继承自XC_MethodHook，所以先判断replace
        if (callback instanceof XC_MethodReplacement) {
            return new XC_MethodReplacement2Proxy((XC_MethodReplacement) callback);
        }
        return new XC_MethodHook2Proxy(callback);
    }

    // --------
    // 扩展增强
    // --------
    public static Method[] findMethodsByExactPredicate(Class<?> clazz, Predicate<Method> predicate) {
        List<Method> result = new LinkedList<Method>();
        if (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                boolean match = predicate.test(method);
                if (match) {
                    method.setAccessible(true);
                    result.add(method);
                }
            }
        }
        return result.toArray(new Method[result.size()]);
    }

    public static Method[] findMethodsByExactPredicate(String className, ClassLoader classLoader, Predicate<Method> predicate) {
        Class<?> clazz = findClassIfExists(className, classLoader);
        return findMethodsByExactPredicate(clazz, predicate);
    }

}
