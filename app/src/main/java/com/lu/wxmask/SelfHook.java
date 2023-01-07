package com.lu.wxmask;

import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SelfHook {
    private static class Holder {
        private static final SelfHook INSTANCE = new SelfHook();
    }

    public static SelfHook getInstance() {
        return Holder.INSTANCE;
    }

    //自己hook自己，改变其值，说明模块有效
    public boolean isModuleEnable() {
        return false;
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
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

    }
}