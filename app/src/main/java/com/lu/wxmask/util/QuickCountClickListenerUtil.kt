package com.lu.wxmask.util

import android.view.View
import com.lu.lposed.api2.XposedHelpers2
import com.lu.magic.util.log.LogUtil

class QuickCountClickListenerUtil {
    companion object {
        fun register(view: View?, fullCount: Int, maxDuration: Int, callBack: View.OnClickListener) {
            if (view == null) {
                return
            }
            val viewListener = try {
                if (view.hasOnClickListeners()) {
                    val listenerInfo = XposedHelpers2.callMethod<Any?>(view, "getListenerInfo")
                    XposedHelpers2.getObjectField<View.OnClickListener>(listenerInfo, "mOnClickListener")
                } else {
                    null
                }
            } catch (e: Throwable) {
                null
            }
            if (viewListener is QuickCountClickListener) {
                viewListener.sourceListener
            } else {
                viewListener
            }.let {
                view.setOnClickListener(QuickCountClickListener(it, callBack, fullCount, maxDuration))
            }
        }

        fun unRegister(view: View?) {
            if (view == null) {
                return
            }
            val viewListener = try {
                if (view.hasOnClickListeners()) {
                    val listenerInfo = XposedHelpers2.callMethod<Any?>(view, "getListenerInfo")
                    XposedHelpers2.getObjectField<View.OnClickListener>(listenerInfo, "mOnClickListener")
                } else {
                    null
                }
            } catch (e: Throwable) {
                null
            }
            if (viewListener is QuickCountClickListener) {
                view.setOnClickListener(viewListener.sourceListener)
            }

        }

    }

    class QuickCountClickListener(
        //原始点击监听器
        @JvmField var sourceListener: View.OnClickListener?,
        @JvmField var fullQuickCallBack: View.OnClickListener,
        /**点击满多少次后， 触发fullQuickCallBack */
        @JvmField var fullCount: Int = 5,
        /**最大间隔时间。当前默认150毫秒**/
        @JvmField var maxDuration: Int = 150
    ) : View.OnClickListener {
        var quickClickCount = 0
        var lastMills = 0L

        override fun onClick(v: View) {
            val currMills = System.currentTimeMillis()
            LogUtil.d(quickClickCount, lastMills, currMills)
            if (lastMills == 0L) {
                lastMills = currMills
            }

            if (currMills - lastMills < maxDuration) {
                quickClickCount++
            } else {
                quickClickCount = 0
            }
            lastMills = currMills

            if (quickClickCount > fullCount) {
                quickClickCount = 0
                lastMills = 0
                //快速点击超过5次，回调一下
                fullQuickCallBack.onClick(v)
            }
            sourceListener?.onClick(v)
        }

    }

}