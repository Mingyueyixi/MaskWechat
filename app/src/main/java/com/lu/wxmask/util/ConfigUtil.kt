package com.lu.wxmask.util

import android.content.Context
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.bean.MaskItemBean

class ConfigUtil {
    companion object {
        const val TABLE = "mask_wechat_config"
        val sp by lazy { AppUtil.getContext().getSharedPreferences(TABLE, Context.MODE_PRIVATE) }
        val KEY_MASK_LIST = "maskList"

        @JvmStatic
        private val dataSetObserverList = arrayListOf<ConfigSetObserver>()

        fun getMaskList(): ArrayList<MaskItemBean> {
            val result = kotlin.runCatching {
                sp.getString(KEY_MASK_LIST, "[]").let {
                    val typ = GsonUtil.getType(ArrayList::class.java, MaskItemBean::class.java)
                    GsonUtil.fromJson<ArrayList<MaskItemBean>>(it, typ)
                }
            }
            return result.getOrElse {
                LogUtil.e(it)
                sp.edit().remove(KEY_MASK_LIST).apply()
                arrayListOf()
            }
        }

        fun setMaskList(data: List<MaskItemBean>) {
            GsonUtil.toJson(data).let {
                sp.edit().putString(KEY_MASK_LIST, it).apply()
            }
            notifyConfigSetObserverChanged()
        }

        fun addMaskList(item: MaskItemBean) {
            val maskList = getMaskList()
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
