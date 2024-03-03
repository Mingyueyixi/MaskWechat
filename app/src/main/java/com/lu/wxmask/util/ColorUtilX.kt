package com.lu.wxmask.util

import android.graphics.Color
import com.lu.magic.util.ColorUtil

class ColorUtilX {
    companion object {
        /**
         * 颜色取反
         */
        @JvmStatic
        fun invertColor(color: Int): Int {
            // 将颜色值转换为ARGB格式
            val a = Color.alpha(color)
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            // 对RGB分量进行取反
            val invR = 255 - r
            val invG = 255 - g
            val invB = 255 - b

            // 构建新的颜色值
            return Color.argb(a, invR, invG, invB)
        }

    }

}

fun ColorUtil.invertColor(color: Int) = ColorUtilX.invertColor(color)