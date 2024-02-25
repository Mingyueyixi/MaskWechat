package com.lu.wxmask.bean

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
class OptionData(
    var hideMainSearch: Boolean,
    var enableMapConversation: Boolean,
    var hideSingleSearch: Boolean
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
                hideSingleSearch = json.optBoolean("hideSingleSearch", true)
            )
        }

    }
}

