package com.lu.wxmask.util

import android.database.Cursor
import com.lu.magic.util.CursorUtil
import com.lu.magic.util.ReflectUtil
import com.lu.wxmask.bean.DBItem
import com.tencent.wcdb.database.SQLiteDatabase
import java.lang.reflect.Array
import java.util.Arrays

//import com.tencent.wcdb.database.SQLiteDatabase

class WxSQLiteManager {
    companion object {
        val Store = HashMap<String, DBItem>()
        fun sqlite(dbName: String, password: String?): Any? {
            return Store[dbName]?.sqliteDatabase

        }

        fun getAllTables(dbName: String, password: String?): MutableList<String> {
            return try {
                val sql = "SELECT name FROM sqlite_master WHERE type='table'"
                val sqliteInstance = sqlite(dbName, password)
                val queryRaw = sqliteInstance?.javaClass?.getMethod("rawQuery", String::class.java, Array.newInstance(Object::class.java, 0)::class.java)
                val cursor = queryRaw?.invoke(sqliteInstance, sql, null)
//                val cursor = ReflectUtil.invokeMethod(sqlite(dbName, password), "rawQuery", sql, null)
//                val cursor = sqlite(dbName, password)?.rawQuery(".table", arrayOf());
                CursorUtil.getAll(cursor as Cursor?, String::class.java)
            } catch (e: Throwable) {
                e.printStackTrace()
                mutableListOf()
            }
        }
    }
}