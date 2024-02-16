package com.lu.wxmask.plugin.ui

import android.view.View
import android.widget.TextView
import androidx.core.util.Consumer

interface Theme {
    companion object {
        @JvmStatic
        val Color = Theme.Colors()
        val Style = Theme.Styles()

    }

    class Colors {
        val bgColor = 0xFFF9F6F6.toInt()
        val bgColorDark = 0xFF303030.toInt()
        val divider = 0XFFCCCCCC.toInt()
    }

    class Styles {
        val itemTitleStyle = fun(value: TextView) {
            value.apply {
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
            }
        }
        val itemSubTitleStyle: (v: TextView) -> Unit = {
            it.apply {
                textSize = 14f
                setTextColor(android.graphics.Color.BLACK)
            }
        }


    }

}