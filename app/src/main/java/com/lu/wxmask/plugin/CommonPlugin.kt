package com.lu.wxmask.plugin

import android.content.Context
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.plugin.part.EnterChattingUIPluginPart
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
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
                return@findMethodsByExactPredicate m.parameterTypes.size == 4
            }
            false
        }.onEach { method ->
            XposedHelpers2.hookMethod(method, object : XC_MethodHook2() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[1].toString()
                    val wxMaskPlugin = PluginProviders.from(WXMaskPlugin::class.java)

                    // 1. 获取真正处于锁定状态的 maskId（排除掉当前已经被临时解锁的好友）
                    val lockedMaskIdList = wxMaskPlugin.maskIdList.filter { id ->
                        id != null && !EnterChattingUIPluginPart.unlockedUsers.contains(id)
                    }

                    if (lockedMaskIdList.isEmpty()) {
                        // 所有好友都已解锁或者没有私密好友，直接放行真实数据库查询
                        return
                    }

                    if (needReplaceChatSearchHistory(sql)) {
                        handleHideSearchList(lockedMaskIdList, param, sql)
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    // Ignore
                }
            })
        }
    }

    private fun needReplaceChatSearchHistory(sql: String): Boolean {
        return regex.containsMatchIn(sql) ||
                sql.startsWith(SQL_SELECT_MESSAGE) ||
                sql.startsWith(SQL_SELECT_MESSAGES_BY_KEYWORD)
    }

    /**
     * 过滤掉所有未解锁的私密好友的数据
     */
    private fun handleHideSearchList(lockedMaskIdList: List<String?>, param: MethodHookParam, sql: String) {
        val hideValueText = lockedMaskIdList.joinToString(",") { "\"$it\"" }

        val sql2 = if (sql.endsWith(";")) {
            sql.dropLast(1)
        } else {
            sql
        }.let { "SELECT * FROM ($it) AS a WHERE aux_index NOT IN ($hideValueText);" }

        param.args[1] = sql2
    }
}