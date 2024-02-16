package com.lu.wxmask.plugin.ui

import android.content.Context
import android.widget.TextView
import com.lu.magic.util.ResUtil
import com.lu.wxmask.util.ext.setTextColorTheme

interface Theme {
    companion object {
        @JvmStatic
        val Color = Theme.Colors()
        val Style = Theme.Styles()

    }

    class Colors {
        val bgRippleColor: Int = 0xFF33AAAAAA.toInt()
        val bgColor = 0xFFF9F6F6.toInt()
        val bgColorDialogTranslucence = 0x33000000.toInt()
        val bgColorDark = 0xFF0B0B0B.toInt()
        val divider = 0XFFDDDDDD.toInt()

        val bgPrimary = fun(context: Context): Int {
            if (ResUtil.isSystemNightMode(context)) {
                return bgColorDark
            }
            return bgColor
        }
    }

    class Styles {
        val itemTitleStyle = fun(value: TextView) {
            value.apply {
                textSize = 16f
                setTextColorTheme(android.graphics.Color.BLACK)
            }
        }
        val itemSubTitleStyle: (v: TextView) -> Unit = {
            it.apply {
                textSize = 14f
                setTextColorTheme(android.graphics.Color.BLACK)
            }
        }


    }

}