package com.lu.wxmask.plugin

import android.content.Context
import com.lu.lposed.plugin.IPlugin
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CommonPlugin:IPlugin {
    override fun handleHook(context: Context?, lpparam: XC_LoadPackage.LoadPackageParam?) {

    }
}