package com.lu.wxmask.util.ext

import android.graphics.Color
import android.widget.TextView
import com.google.gson.JsonObject
import com.lu.magic.util.AppUtil
import com.lu.magic.util.ColorUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ResUtil
import com.lu.magic.util.SizeUtil
import com.lu.wxmask.util.ColorUtilX
import kotlin.math.roundToLong
import kotlin.time.times


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
fun CharSequence?.toLongElse(fallback: Long): Long = try {
    if (this == null) {
        fallback
    }
    this.toString().toLong()
} catch (e: Exception) {
    fallback
}

fun TextView.setTextColorTheme(color: Int) {
    if (ResUtil.isAppNightMode(this.context)) {
        setTextColor(ColorUtilX.invertColor(color))
    } else {
        setTextColor(color)
    }
}

fun Class<*>?.createEmptyOrNullObject(): Any? {
    if (this == null) {
        return null
    }
    return runCatching {
        this::class.java.getDeclaredConstructor().newInstance()
    }.getOrElse {
        runCatching {
            return GsonUtil.fromJson("{}", this::class.java)
        }.getOrDefault(null)
    }
}


fun Number.day2Mills(): Long {
    if (this is Double) {
        return (this.toDouble() * 24L * 60L * 60L * 1000L).toLong()
    }
    if (this is Float) {
        return (this.toFloat() * 24L * 60L * 60L * 1000L).toLong()
    }
    return (this.toLong() * 24L * 60L * 60L * 1000L)
}

fun Long.mills2Day(): Int {
    return ((this / 24L / 60L / 60L / 1000L).toInt())
}

/**
 * 天数文本转毫秒数
 */
fun TextView?.dayText2Mills(fallback : Long=0): Long {
    try {
        return this?.text?.toString()?.toLongOrNull()?.day2Mills() ?: fallback
    } catch (e: Exception) {
    }
    return fallback
}