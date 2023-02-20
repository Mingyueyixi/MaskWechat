package com.lu.wxmask

import android.util.Base64
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class MaskAppBuildInfo(
    val buildTime: String,
    val branch: String,
    val commit: String,
) {
    companion object {
        fun of(): MaskAppBuildInfo {
            val json = JSONObject(Base64.decode(BuildConfig.buildInfoJson64, Base64.DEFAULT).toString(Charsets.UTF_8))
            return MaskAppBuildInfo(
                SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(json.optLong("time")),
                json.optString("branch"),
                json.optString("commit").substring(0, 11)
            )
        }
    }
}

