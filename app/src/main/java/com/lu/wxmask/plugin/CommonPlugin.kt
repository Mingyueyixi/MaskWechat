package com.lu.wxmask.plugin

import android.content.Context
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CommonPlugin : IPlugin {

    companion object {
        // 查询消息记录
        private const val SQL_SELECT_MESSAGE =
            "SELECT type, subtype, entity_id, aux_index, MAX(timestamp) as maxTime, count(aux_index) as msgCount, talker FROM FTS5MetaMessage"

        // 单聊搜索关键词记录
        private const val SQL_SELECT_MESSAGES_BY_KEYWORD =
            "SELECT FTS5MetaMessage.docid, type, subtype, entity_id, aux_index, timestamp, talker FROM FTS5MetaMessage"
    }

    // 缓存正则表达式
    private val regex by lazy {
        Regex("^SELECT (FTS5MetaContact|FTS5MetaTopHits|FTS5MetaKefuContact|FTS5MetaFeature|FTS5MetaWeApp|FTS5MetaFinderFollow|FTS5MetaFavorite)\\.docid, type, subtype, entity_id, aux_index,.*")
    }

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {

        XposedHelpers2.findMethodsByExactPredicate(ClazzN.from("com.tencent.wcdb.database.SQLiteDatabase")) { m ->
            if (m.name == "rawQueryWithFactory") {
                LogUtil.d("rawQueryWithFactory", m.parameterTypes.size)
                return@findMethodsByExactPredicate m.parameterTypes.size == 4
            }
            false
        }.onEach { method ->
            XposedHelpers2.hookMethod(method, object : XC_MethodHook2() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    //通过拦截sql语句，来隐藏搜索，或者通过cursor代理来隐藏

                    val sql = param.args[1].toString()
                    val wxMaskPlugin = PluginProviders.from(WXMaskPlugin::class.java)

                    if (wxMaskPlugin.maskIdList.isNotEmpty()) {
                        val needReplace =
                            regex.containsMatchIn(sql) ||
                                    sql.startsWith(SQL_SELECT_MESSAGE) ||
                                    sql.startsWith(SQL_SELECT_MESSAGES_BY_KEYWORD)

                        if (needReplace) {
                            val hideValueText =
                                wxMaskPlugin.maskIdList.joinToString(",") { "\"$it\"" }

                            val sql2 = if (sql.endsWith(";")) {
                                sql.dropLast(1)
                            } else {
                                sql
                            }.let { "SELECT * FROM ($it) AS a WHERE aux_index NOT IN ($hideValueText);" }

                            param.args[1] = sql2
                            LogUtil.d("sql hide hit:", sql2)
                        }
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    // Ignore
                }
            })
        }
    }
}