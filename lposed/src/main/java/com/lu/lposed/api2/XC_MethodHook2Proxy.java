package com.lu.lposed.api2;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

//代理类将异常抓取掉
class XC_MethodHook2Proxy extends XC_MethodHook {
    private final XC_MethodHook callBack;

    public XC_MethodHook2Proxy(XC_MethodHook callBack) {
        super(callBack.priority);
        this.callBack = callBack;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        try {
            if (callBack instanceof XC_MethodHook2) {
                ((XC_MethodHook2) this.callBack).beforeHookedMethod(param);
            } else {
                XposedHelpers.callMethod(callBack, "beforeHookedMethod", param);
            }
        } catch (Throwable e) {
            XposedHelpers2.Config.onFailed(e);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) {
        try {
            if (callBack instanceof XC_MethodHook2) {
                ((XC_MethodHook2) this.callBack).afterHookedMethod(param);
            } else {
                XposedHelpers.callMethod(callBack, "afterHookedMethod", param);
            }
        } catch (Throwable e) {
            XposedHelpers2.Config.onFailed(e);
        }
    }
}