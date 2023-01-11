package com.lu.lposed.api2;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

//代理类将异常抓取掉
class XC_MethodReplacement2Proxy extends XC_MethodReplacement {
    private XC_MethodReplacement callBack;

    XC_MethodReplacement2Proxy(XC_MethodReplacement callBack) {
        super(callBack.priority);
        this.callBack = callBack;
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
        try {
            if (callBack instanceof XC_MethodReplacement2) {
                return ((XC_MethodReplacement2) callBack).replaceHookedMethod(param);
            }
            return XposedHelpers.callMethod(callBack, "replaceHookedMethod", param);
        } catch (Throwable e) {
            XposedHelpers2.Config.onFailed(e);
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }
    }

}
