package com.lu.wxmask.util

import com.lu.magic.util.AppUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.Constrant

class AppVersionUtil {
    companion object {
        private var versionCode = -1
        private var versionName = ""

        @JvmStatic
        fun getVersionName(): String? {
            if (versionName.isBlank()) {
                versionName = try {
                    val manager = AppUtil.getContext().packageManager
                    val info = manager.getPackageInfo(AppUtil.getContext().packageName, 0)
                    return info.versionName
                } catch (e: Exception) {
                    LogUtil.e(e)
                    ""
                }
            }
            return versionName
        }

        @JvmStatic
        fun getVersionCode(): Int {
            if (versionCode == -1) {
                versionCode = try {
                    val manager = AppUtil.getContext().packageManager
                    val info = manager.getPackageInfo(AppUtil.getContext().packageName, 0)
                    info.versionCode
                } catch (e: Exception) {
                    LogUtil.e(e)
                    -1
                }
            }
            return versionCode
        }

        @JvmStatic
        fun isSupportWechat(): Boolean {
            return when (getVersionCode()) {
                Constrant.WX_CODE_8_0_22, Constrant.WX_CODE_8_0_32, Constrant.WX_CODE_8_0_33 -> true
                else -> false
            }
        }
    }
}