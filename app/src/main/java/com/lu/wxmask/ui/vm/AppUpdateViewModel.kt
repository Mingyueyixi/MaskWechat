package com.lu.wxmask.ui.vm

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.ViewModel
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.route.MaskAppRouter
import com.lu.wxmask.util.AppUpdateCheckUtil

class AppUpdateViewModel : ViewModel() {
    private var hasOnCheckAction = false
    fun checkOnEnter(context: Context) {
        if (hasOnCheckAction) {
            return
        }
        if (!AppUpdateCheckUtil.hasCheckFlagOnEnter()) {
            return
        }
        hasOnCheckAction = true

        AppUpdateCheckUtil.checkUpdate { url, name ->
            if (url.isBlank() || name.isBlank()) {
                hasOnCheckAction = false
                return@checkUpdate
            }
            AlertDialog.Builder(context)
                .setTitle("更新提示")
                .setMessage("检查到新版本：$name，是否更新？")
                .setNegativeButton("取消", null)
                .setNeutralButton("确定") { _, _ ->
                    openBrowserDownloadUrl(context, url)
                }
                .setPositiveButton("不再提示") { _, _ ->
                    AppUpdateCheckUtil.setCheckFlagOnEnter(false)
                }
                .setOnDismissListener {
                    hasOnCheckAction = false
                }
                .show()
        }
    }

    fun checkOnce(context: Context) {
        if (hasOnCheckAction) {
            return
        }
        hasOnCheckAction = true
        AppUpdateCheckUtil.checkUpdate { url, name ->
            if (url.isBlank() || name.isBlank()) {
                hasOnCheckAction = false
                return@checkUpdate
            }
            AlertDialog.Builder(context)
                .setTitle("更新提示")
                .setMessage("检查到新版本$name，是否更新？")
                .setNegativeButton("取消", null)
                .setNeutralButton("确定") { _, _ ->
                    openBrowserDownloadUrl(context, url)
                }
                .setOnDismissListener {
                    hasOnCheckAction = false
                }
                .show()
        }
    }

    private fun openBrowserDownloadUrl(context: Context, url: String) {
        try {
            MaskAppRouter.route(context, url)
        } catch (e: Exception) {
            ToastUtil.show("下载链接打开失败")
            LogUtil.w(e)
        }
    }

}