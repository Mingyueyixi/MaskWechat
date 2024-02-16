package com.lu.wxmask.util.ext

import android.view.TextureView
import android.view.View
import androidx.core.util.Consumer
import com.google.gson.JsonObject
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.SizeUtil
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val sizeIntCache = HashMap<String, Int>()
val sizeFloatCache = HashMap<String, Float>()

fun Any?.toJsonObject(): JsonObject {
    return GsonUtil.toJsonTree(this).asJsonObject
}

fun Any?.toJson(): String {
    return GsonUtil.toJson(this)
}

val Int.dp: Int
    get() = sizeIntCache[this.toString()].let {
        if (it == null) {
            val v = SizeUtil.dp2px(AppUtil.getContext().resources, this.toFloat()).toInt()
            sizeIntCache[this.toString()] = v
            return@let v
        }
        return it
    }

val Float.dp: Float
    get() = sizeFloatCache[this.toString()].let {
        if (it == null) {
            val v = SizeUtil.dp2px(AppUtil.getContext().resources, this.toFloat())
            sizeFloatCache[this.toString()] = v
            return@let v
        }
        return it
    }

val Double.dp: Float
    get() = this.toFloat().dp

fun CharSequence?.toIntElse(fallback: Int): Int = try {
    if (this == null) {
        fallback
    }
    Integer.parseInt(this.toString())
} catch (e: Exception) {
    fallback
}
