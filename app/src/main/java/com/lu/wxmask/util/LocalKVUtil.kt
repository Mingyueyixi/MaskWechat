package com.lu.wxmask.util

import android.content.Context
import android.content.SharedPreferences
import com.lu.magic.util.AppUtil

class LocalKVUtil {
    companion object {
        const val defaultTableName = "app"

        @JvmStatic
        @JvmOverloads
        fun getTable(name: String, mode: Int = Context.MODE_PRIVATE): SharedPreferences {
            return AppUtil.getContext().getSharedPreferences(name, mode)
        }

        @JvmStatic
        fun getDefaultTable(): SharedPreferences {
            return getTable(defaultTableName)
        }
    }
}