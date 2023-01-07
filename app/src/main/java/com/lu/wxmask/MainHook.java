package com.lu.wxmask;

import android.app.Application;
import android.content.Context;

import com.lu.magic.util.AppUtil;
import com.lu.wxmask.plugin.PluginRegistry;
import com.lu.wxmask.plugin.WXConfigPlugin;
import com.lu.wxmask.plugin.WXMaskPlugin;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private boolean hasInit = false;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            SelfHook.getInstance().handleLoadPackage(lpparam);
            return;
        }
        if (!"com.tencent.mm".equals(lpparam.processName)) {
            return;
        }

        XposedHelpers.findAndHookMethod(
                Application.class.getName(),
                lpparam.classLoader,
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        initHook((Context) param.thisObject, lpparam);
                    }
                }
        );
//        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI",
//                lpparam.classLoader,
//                "onCreate",
//                Bundle.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        initHook((Context) param.thisObject, lpparam);
//                    }
//                });
    }

    private void initHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (hasInit) {
            return;
        }
        hasInit = true;
        AppUtil.attachContext(context);
        //目前生成的plugin都是单例的
        PluginRegistry.register(
                WXConfigPlugin.class,
                WXMaskPlugin.class
        ).handleHooks(context, lpparam);
    }

}