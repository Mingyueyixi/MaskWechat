package com.lu.wxmask.plugin

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.magic.util.view.SelfDeepCheck
import com.lu.magic.util.view.ViewUtil
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.util.AppVersionUtil
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CommonPlugin : IPlugin {
    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
//        if (!BuildConfig.DEBUG) {
//            return
//        }
//        LogUtil.w("WeChat MainUI not found Adapter for ListView, guess start.")
//        val setAdapterMethod = XposedHelpers2.findMethodExactIfExists(
//            ListView::class.java.name,
//            context.classLoader,
//            "setAdapter",
//            ListAdapter::class.java
//        )
//        if (setAdapterMethod == null) {
//            LogUtil.w( "setAdapterMethod is null")
//            return
//        }
//        XposedHelpers2.hookMethod(
//            setAdapterMethod,
//            object : XC_MethodHook2() {
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    val adapter = param.args[0] ?: return
//                    LogUtil.i("hook List adapter ", adapter)
//                }
//            }
//        )
    }
}