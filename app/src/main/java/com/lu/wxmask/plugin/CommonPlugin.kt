package com.lu.wxmask.plugin

import android.content.Context
import android.content.res.XModuleResources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.ResUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.plugin.ui.MaskManagerCenterUI
import com.lu.wxmask.util.HookPointManager
import com.lu.wxmask.util.ext.getViewId
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

        HookPointManager.INSTANCE.init(context, lpparam)
    }
}