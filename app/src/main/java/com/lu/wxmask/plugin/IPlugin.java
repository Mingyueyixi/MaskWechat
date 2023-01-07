package com.lu.wxmask.plugin;

import android.content.Context;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface IPlugin {

    default void onCreate() {
    }

    void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam);

}
