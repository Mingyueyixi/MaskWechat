package com.lu.wxmask.plugin.point;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.api2.function.Predicate;
import com.lu.lposed.plugin.IPlugin;
import com.lu.lposed.plugin.PluginProviders;
import com.lu.magic.util.ReflectUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.wxmask.ClazzN;
import com.lu.wxmask.bean.PointBean;
import com.lu.wxmask.plugin.WXMaskPlugin;
import com.lu.wxmask.plugin.part.HideSearchListUIPluginPart;
import com.lu.wxmask.util.ConfigUtil;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HideSearchListPoint implements IPlugin {
    private PointBean mPointBean;

    public HideSearchListPoint(@NotNull PointBean it) {
        mPointBean = it;
    }

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (mPointBean == null) {
            return;
        }
        Method method = XposedHelpers2.findMethodExactIfExists(ClazzN.from(mPointBean.getClazz()), mPointBean.getMethod(), Integer.TYPE);
        if (method == null) {
            return;
        }
        XposedHelpers2.hookMethod(method, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ConfigUtil.Companion.getOptionData().getHideMainSearch()) {
                    LogUtil.i("off hideMainSearch");
                    return;
                }

                Object result = param.getResult();
                if (result == null) {
                    return;
                }

                WXMaskPlugin wxMaskPlugin = PluginProviders.from(WXMaskPlugin.class);
                HideSearchListUIPluginPart pluginPart = wxMaskPlugin.getHideSearchListPluginPart();
                if (pluginPart.needHideUserName2(param, result)) {
                    LogUtil.d(">>> need hide", result);
//                    Object empty = null;
//                    try {
//                        empty = result.getClass().newInstance();
//                        param.setResult(empty);
//                    } catch (Exception e) {
//                        param.setResult(null);
//                    }

                    Field[] fields = XposedHelpers2.findFieldsByExactPredicate(param.thisObject.getClass(), new Predicate<Field>() {
                        @Override
                        public boolean test(Field field) {
                            if (SparseArray.class.equals(field.getType())) {
                                return true;
                            }
                            return false;
                        }
                    });
                    Object newResult = null;
                    for (Field field : fields) {
                        field.setAccessible(true);
                        SparseArray<?> value = (SparseArray<?>) field.get(param.thisObject);
                        if (value.size() > 0) {
                            Object first = value.get(0);
                            if (first != null && first.getClass().isAssignableFrom(result.getClass())) {
                                newResult = first;
                            }
                        }
                    }
                    param.setResult(newResult);
                }
            }

        });
    }
}
