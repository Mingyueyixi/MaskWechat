package com.lu.wxmask.bean

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
class PointBean(
    var featId: String,
    var clazz: String,
    var method: String
) {
    companion object {
        fun fromJson(value: JSONObject): PointBean {
            val featId = value.optString("featId", null)
            val clazz = value.optString("clazz", null)
            val method = value.optString("method", null)
            return PointBean(featId, clazz, method)
        }
    }
}
