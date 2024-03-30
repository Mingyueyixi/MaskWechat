package com.lu.wxmask.plugin

import android.content.Context
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.kxt.toElseString
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.bean.DBItem
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.WxSQLiteManager
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WXDbPlugin : IPlugin {

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        if (ConfigUtil.getOptionData().viewWxDbPw) {
            hookDatabase(context, lpparam)
        }
    }

    private fun hookDatabase(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers2.findAndHookMethod(
            ClazzN.from("com.tencent.wcdb.database.SQLiteDatabase"),
            "openDatabase",
            "java.lang.String",
            "[B",
            "com.tencent.wcdb.database.SQLiteCipherSpec",
            "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory",
            "int",
            "com.tencent.wcdb.DatabaseErrorHandler",
            "int",
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val password = param.args[1].let {
                        if (it == null) {
                            return@let null
                        }
                        return@let String(it as ByteArray)
                    }
                    val dbName = param.args[0].toElseString("")
                    LogUtil.d("hook db", dbName, password, param.result)
                    if (dbName != "") {
                        WxSQLiteManager.Store[dbName] = DBItem(dbName, password, param.result)
                    }
                }
            }

        )
//        ClazzN.from("com.tencent.wcdb.database.SQLiteDatabase")?.methods?.forEach {
//            XposedHelpers2.hookMethod(it, object : XC_MethodHook2() {
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    LogUtil.w(param.method.name, param.args)
//                }
//            })
//        }
//        SQLiteDatabase
//        XposedHelpers2.findAndHookMethod(
//            ClazzN.from("com.tencent.wcdb.database.SQLiteDatabase"),
//            "rawQueryWithFactory",
//            "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory",
//            "java.lang.String",
//            "[Ljava.lang.Object;",
//            "java.lang.String",
//            "com.tencent.wcdb.support.CancellationSignal", object : XC_MethodHook2() {
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    val sArg = param.args[2].let {
//                        if (it == null) {
//                            return@let null
//                        }
//                        return@let (it as Array<Object?>).contentToString()
//                    }
//                    LogUtil.w("watch sql", param.args[1], sArg)
//                }
//            })

    }
}