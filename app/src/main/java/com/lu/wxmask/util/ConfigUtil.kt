package com.lu.wxmask.util

import com.google.gson.JsonObject
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.bean.BaseTemporary
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.util.ext.toJson

class ConfigUtil {
    companion object {
        val sp by lazy { LocalKVUtil.getTable("mask_wechat_config") }
        val KEY_MASK_LIST = "maskList"
        val KEY_TEMPORARY = "temporary"

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

        fun getTemporaryJson(): JsonObject? {
            val text = sp.getString(KEY_TEMPORARY, null) ?: return null
            return try {
                GsonUtil.fromJson(text, JsonObject::class.java)
            } catch (e: Exception) {
                null
            }
        }

        fun <T : BaseTemporary> setTemporary(data: T) {
            try {
                sp.edit().putString(KEY_TEMPORARY, data.toJson()).apply()
            } catch (e: Exception) {
                LogUtil.w("save temporary fail", e)
            }
        }


        fun clearData() {
            try {
                val result = sp.edit().clear().commit()
                if (!result) {
                    LogUtil.w("clear sp data fail$result")
                }
                notifyConfigSetObserverChanged()
            } catch (e: Exception) {
                LogUtil.w("clear sp data fail", e)
            }
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
