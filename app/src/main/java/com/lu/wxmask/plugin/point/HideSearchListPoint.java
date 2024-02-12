package com.lu.wxmask.plugin.point;

import android.content.Context;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.lposed.plugin.PluginProviders;
import com.lu.magic.util.log.LogUtil;
import com.lu.wxmask.ClazzN;
import com.lu.wxmask.bean.PointBean;
import com.lu.wxmask.plugin.WXMaskPlugin;
import com.lu.wxmask.plugin.part.HideSearchListUIPluginPart;

import org.jetbrains.annotations.NotNull;

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
                WXMaskPlugin wxMaskPlugin = PluginProviders.from(WXMaskPlugin.class);
                HideSearchListUIPluginPart pluginPart = wxMaskPlugin.getHideSearchListPluginPart();

                if (pluginPart.needHideUserName2(param, param.getResult())) {
                    LogUtil.d("hide hahah", param.getResult());
                    param.setResult(null);
                }
            }

        });
    }
}
