package com.lu.lposed.api2;

import de.robv.android.xposed.XC_MethodReplacement;

public abstract class XC_MethodReplacement2 extends XC_MethodReplacement{
    @Override
    abstract protected Object replaceHookedMethod(MethodHookParam param) throws Throwable;
}
