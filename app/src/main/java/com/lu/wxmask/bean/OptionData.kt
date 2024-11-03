package com.lu.wxmask.bean

import androidx.annotation.Keep
import org.json.JSONObject



@Keep
class OptionData
/**
 * @param hideMainSearch 主页搜索隐藏
 * @param enableMapConversation 主页消息变脸
 * @param hideSingleSearch 单聊搜索隐藏
 * @param hideMainSearchStrong 主页搜索，暴力隐藏（已废弃）
 * @param viewWxDbPw 查看微信数据库密码
 * @param travelTime 时间穿越（单位/毫秒）
 * @param enableTravelTime 是否启用时间穿越
 */
private constructor(
    var hideMainSearch: Boolean,
    var enableMapConversation: Boolean,
    var hideSingleSearch: Boolean,
    var hideMainSearchStrong: Boolean,
    var viewWxDbPw: Boolean,
    var travelTime: Long,
    var enableTravelTime: Boolean
) {

    companion object {
        fun fromJson(jsonText: String): OptionData {
            val json = try {
                JSONObject(jsonText)
            } catch (e: Exception) {
                JSONObject()
            }
            return OptionData(
                hideMainSearch = json.optBoolean("hideMainSearch", true),
                enableMapConversation = json.optBoolean("enableMapConversation", false),
                hideSingleSearch = json.optBoolean("hideSingleSearch", true),
                hideMainSearchStrong = json.optBoolean("hideMainSearchStrong", false),
                viewWxDbPw = json.optBoolean("viewWxDbPw", false),
                travelTime = json.optLong("travelTime", 0L),
                enableTravelTime = json.optBoolean("enableTravelTime", false)
            )
        }
        fun toJson(data: OptionData): String {
            return JSONObject().apply {
                put("hideMainSearch", data.hideMainSearch)
                put("enableMapConversation", data.enableMapConversation)
                put("hideSingleSearch", data.hideSingleSearch)
                put("hideMainSearchStrong", data.hideMainSearchStrong)
                put("viewWxDbPw", data.viewWxDbPw)
                put("travelTime", data.travelTime)
                put("enableTravelTime", data.enableTravelTime)
            }.toString()
        }

    }
}
