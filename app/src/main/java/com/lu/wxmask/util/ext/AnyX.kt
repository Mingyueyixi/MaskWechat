package com.lu.wxmask.util.ext

import com.google.gson.JsonObject
import com.lu.magic.util.GsonUtil

fun Any?.toJsonObject(): JsonObject {
    return GsonUtil.toJsonTree(this).asJsonObject
}

fun Any?.toJson(): String {
    return GsonUtil.toJson(this)
}