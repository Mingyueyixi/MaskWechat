package com.lu.wxmask.util

import android.content.Context
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.util.ext.toJson
import com.lu.wxmask.util.ext.toJsonObject

class ConfigUtil {
    companion object {
        const val TABLE = "mask_wechat_config"
        val sp by lazy { AppUtil.getContext().getSharedPreferences(TABLE, Context.MODE_PRIVATE) }
        val KEY_MASK_LIST = "maskList"

        @JvmStatic
        private val dataSetObserverList = arrayListOf<ConfigSetObserver>()

        fun getMaskList(): ArrayList<MaskItemBean> {
            return try {
                getMaskListInternal()
            } catch (e: Throwable) {
                LogUtil.e(e)
                arrayListOf()
            }
        }

        private fun getMaskListInternal(): ArrayList<MaskItemBean> {
            val jsonText = sp.getString(KEY_MASK_LIST, "[]")
            val typ = GsonUtil.getType(ArrayList::class.java, MaskItemBean::class.java)
            try {
                return GsonUtil.fromJson<ArrayList<MaskItemBean>>(jsonText, typ)
            } catch (e: Throwable) {
                LogUtil.w(jsonText)
                throw e
            }
        }

        fun setMaskList(data: List<MaskItemBean>) {
            GsonUtil.toJson(data).let {
                sp.edit().putString(KEY_MASK_LIST, it).apply()
            }
            notifyConfigSetObserverChanged()
        }

        fun addMaskList(item: MaskItemBean) {
            val maskList = try {
                getMaskListInternal()
            } catch (e: Exception) {
                LogUtil.e(item.toJson(), e)
                null
            } ?: return
            maskList.add(item)
            GsonUtil.toJson(maskList).let {
                sp.edit().putString(KEY_MASK_LIST, it).apply()
            }
            notifyConfigSetObserverChanged()
        }

        fun registerConfigSetObserver(observer: ConfigSetObserver) {
            dataSetObserverList.add(observer)
        }

        fun unregisterConfigSetObserver(observer: ConfigSetObserver) {
            dataSetObserverList.remove(observer)
        }

        fun hasRegisterConfigSetObserver(observer: ConfigSetObserver): Boolean {
            return dataSetObserverList.contains(observer)
        }

        private fun notifyConfigSetObserverChanged() {
            dataSetObserverList.forEach {
                it.onConfigChange()
            }
        }

    }

    fun interface ConfigSetObserver {
        fun onConfigChange()
    }
}
