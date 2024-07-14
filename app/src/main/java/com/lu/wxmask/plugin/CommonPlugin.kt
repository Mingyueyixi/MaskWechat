package com.lu.wxmask.plugin

import android.content.Context
import android.database.Cursor
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.CursorUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
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

//        HookPointManager.INSTANCE.init(context, lpparam)

//        XposedHelpers2.findAndHookMethod(
//            ClazzN.from("com.tencent.mm.sdk.platformtools.p2"),
//            "getLogLevel",
//            java.lang.Long.TYPE,
//            object : XC_MethodHook2() {
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    param.result = 0
//                    XposedHelpers2.setStaticIntField(ClazzN.from("com.tencent.mm.sdk.platformtools.k2"), "a", 0)
//                }
//            })
//
//        XposedHelpers2.findMethodsByExactPredicate(ClazzN.from("com.tencent.mm.sdk.platformtools.p2")) { m ->
//            return@findMethodsByExactPredicate m.name.startsWith("log")
//        }.forEach {
//            XposedHelpers2.hookMethod(it, object : XC_MethodHook2() {
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    LogUtil.d("wxLog: ", param.args)
//                }
//            })
//        }
//
//        XposedHelpers2.findMethodsByExactPredicate(ClazzN.from("com.tencent.mars.xlog.Xlog")) { m ->
//            return@findMethodsByExactPredicate m.name.startsWith("log") || m.name == "getLogLevel"
//        }.forEach {
//            if (it.name == "getLogLevel") {
//                XposedHelpers2.hookMethod(it, object : XC_MethodHook2() {
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                        param.result = 0
//                    }
//                })
//            } else {
//                XposedHelpers2.hookMethod(it, object : XC_MethodHook2() {
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                        LogUtil.d("wxLogXLog: ", param.args)
//                    }
//                })
//            }
//        }


        XposedHelpers2.findMethodsByExactPredicate(ClazzN.from("com.tencent.wcdb.database.SQLiteDatabase")) { m ->
            if (m.name == "rawQueryWithFactory") {
                LogUtil.d("rawQueryWithFactory", m.parameterTypes.size)
                return@findMethodsByExactPredicate m.parameterTypes.size == 4
            }
            false
        }.onEach {
            XposedHelpers2.hookMethod(it, object : XC_MethodHook2() {
                override fun beforeHookedMethod(param: MethodHookParam) {
//                    LogUtil.d("sql rawQueryWithFactory:", param.args[1], param.args[2], param.args[3])
//                    val sourceData = CursorUtil.getAll(cursor, true, true)
//                    LogUtil.d("sql rawQueryWithFactory result:", sourceData)
                    //通过拦截sql语句，来隐藏搜索，或者通过cursor代理来隐藏

                    var sql = param.args[1].toString()
                    val wxMaskPlugin = PluginProviders.from(WXMaskPlugin::class.java)
                    //sql 中，aux_index 列必须存在，因为接下来需要用来重新构造sql并过滤
                    val regexResult =
                        Regex("^SELECT (FTS5MetaContact|FTS5MetaTopHits|FTS5MetaKefuContact|FTS5MetaFeature|FTS5MetaWeApp|FTS5MetaFinderFollow|FTS5MetaFavorite)\\.docid, type, subtype, entity_id, aux_index,.*").find(
                            sql
                        )
                    val needReplace = wxMaskPlugin.maskIdList.isNotEmpty()
                            && (regexResult != null
                            // 查消息记录的
                            //SELECT type, subtype, entity_id, aux_index, MAX(timestamp) as maxTime, count(aux_index) as msgCount, talker FROM FTS5MetaMessage NOT INDEXED JOIN FTS5IndexMessage ON (FTS5MetaMessage.docid = FTS5IndexMessage.rowid) WHERE FTS5IndexMessage MATCH '("我")' AND status >= 0 GROUP BY aux_index ORDER BY timestamp desc LIMIT 4;
                            || sql.startsWith("SELECT type, subtype, entity_id, aux_index, MAX(timestamp) as maxTime, count(aux_index) as msgCount, talker FROM FTS5MetaMessage ")
                            //单聊搜索关键词查记录
                            || sql.startsWith("SELECT FTS5MetaMessage.docid, type, subtype, entity_id, aux_index, timestamp, talker FROM FTS5MetaMessage")
                            )

//                            sql.startsWith("SELECT FTS5MetaTopHits.docid")
//                                    || sql.startsWith("SELECT FTS5MetaContact.docid")
//                                    || sql.startsWith("SELECT FTS5MetaKefuContact.docid")
//                                    || sql.startsWith("SELECT FTS5MetaFeature.docid")
//                                    || sql.startsWith("SELECT FTS5MetaWeApp.docid")
//                                    || sql.startsWith("SELECT FTS5MetaFinderFollow.docid")
//                                    || sql.startsWith("SELECT FTS5MetaFavorite.docid"))

                    if (needReplace) {
                        val hideValueText = StringBuilder()
                        for ((index, s) in wxMaskPlugin.maskIdList.withIndex()) {
                            hideValueText.append("\"$s\"")
                            if (index < wxMaskPlugin.maskIdList.size - 1) {
                                hideValueText.append(",")
                            }
                        }

                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length - 1)
                        }
                        //对原始sql包围一层，过滤掉指定用户
                        val sql2 = "SELECT * FROM ($sql) AS a WHERE aux_index NOT IN ($hideValueText);"

                        param.args[1] = sql2
                        LogUtil.d("sql hide hit:", sql2)
                    }
//                    val result = XposedHelpers2.invokeOriginalMethod(param.method, param.thisObject, param.args)

//                    if (result is Cursor) {
//                        LogUtil.d("sql:", sql, param.args[2])
//                        LogUtil.d("sql result:", CursorUtil.getAll(result, true, true))
//                    }
//                    return result
                }

                override fun afterHookedMethod(param: MethodHookParam) {
//                for debug watch sql query result
                    LogUtil.d("sql: ", param.args[1])
                    LogUtil.d("sql reqult: ", CursorUtil.getAll(param.result as Cursor?, true, true))
                }
            })

        }

    }

//    private fun proxyCursor(cursor: Cursor, filterList: List<Map<String, Any?>>): Any {
//        if (!Proxy.isProxyClass(cursor::class.java)) {
//            // 创建代理类
//            Proxy.newProxyInstance(
//                cursor.javaClass.getClassLoader(),
//                cursor.javaClass.getInterfaces(),
//                CursorProxy(cursor, filterList)
//            )
//        }
//        return cursor
//
//    }

}

//class WxCursorWrap(val cursor: Cursor, val filterList: List<Map<String, Any?>>) : AbstractCursor {
//    override fun getCount(): Int {
//        return filterList.size
//    }
//
//    private inline fun getValue(column: Int): Any? {
//        val columnName = getColumnName(column)
//        return filterList[position][columnName]
//    }
//
//    override fun getColumnNames(): Array<String> {
//        return cursor.columnNames
//    }
//
//    override fun getString(column: Int): String? {
//        val result = getValue(column)
//        return result?.toString()
//    }
//
//    override fun getShort(column: Int): Short? {
//        val result = getValue(column)
//        if (result is Short) {
//            return result
//        }
//        return if (result is Number) {
//            result.toShort()
//        } else {
//            result.toString().toShortOrNull()
//        }
//    }
//
//    override fun getInt(column: Int): Int {
//        val result = getValue(column)
//        if (result is Int) {
//            return result
//        }
//        return if (result is Number) {
//            result.toInt()
//        } else {
//            result.toString().toIntOrNull() ?: 0
//        }
//    }
//
//    override fun getLong(column: Int): Long {
//        val result = getValue(column)
//        if (result is Long) {
//            return result
//        }
//        return if (result is Number) {
//            result.toLong()
//        } else {
//            result.toString().toLongOrNull() ?: 0
//        }
//    }
//
//    override fun getFloat(column: Int): Float {
//        val result = getValue(column)
//        return if (result is Float) {
//            result
//        } else if (result is Number) {
//            result.toFloat()
//        }else{
//            result.toString().toFloatOrNull() ?: 0f
//        }
//    }
//
//    override fun getDouble(column: Int): Double {
//        val result = getValue(column)
//        return if (result is Double) {
//            result
//        } else if (result is Number) {
//            result.toDouble()
//        }else {
//            result.toString().toDoubleOrNull() ?: 0.0
//        }
//    }
//
//    override fun isNull(column: Int): Boolean {
//        return getValue(column) == null
//    }
//
//}
//
//class CursorProxy(val cursor: Cursor, val filterList: List<Map<String, Any?>>) : InvocationHandler {
//
//    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any {
//        if ("getCount" == method.name) {
//            return filterList.size
//        }
//        if ()
//            return method.invoke(cursor, args)
//    }
//
//}