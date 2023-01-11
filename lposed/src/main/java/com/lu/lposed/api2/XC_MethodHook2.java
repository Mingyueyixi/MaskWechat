package com.lu.lposed.api2;

import de.robv.android.xposed.XC_MethodHook;

public abstract class XC_MethodHook2 extends XC_MethodHook {
    public XC_MethodHook2() {
    }

    public XC_MethodHook2(int priority) {
        super(priority);
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

    }

}

