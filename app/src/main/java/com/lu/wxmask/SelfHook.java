package com.lu.wxmask;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.Keep;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.AppUtil;
import com.lu.magic.util.ResUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.wxmask.plugin.ui.MaskManagerCenterUI;
import com.lu.wxmask.util.ext.ResUtilXKt;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class SelfHook implements IPlugin {


    private final static class Holder {
        private static final SelfHook INSTANCE = new SelfHook();
    }

    public static SelfHook getInstance() {
        return Holder.INSTANCE;
    }

    //自己hook自己，改变其值，说明模块有效
    public boolean isModuleEnable() {
        return false;
    }

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                SelfHook.class.getName(),
                lpparam.classLoader,
                "isModuleEnable",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                }
        );

        if (BuildConfig.DEBUG) {
            handleDebugHook(context, lpparam);
        }

    }

    private void handleDebugHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                ClazzN.from("android.app.Activity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity act = (Activity) param.thisObject;
                        act.findViewById(ResUtilXKt.getViewId(ResUtil.INSTANCE, "action_bar")).setOnClickListener(v -> new MaskManagerCenterUI(act).show());
                    }
                });
    }
}