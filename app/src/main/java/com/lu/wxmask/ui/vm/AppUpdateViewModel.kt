package com.lu.wxmask.ui.vm

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.lu.wxmask.util.AppUpdateCheckUtil

class AppUpdateViewModel : ViewModel() {
    private var hasOnCheckAction = false
    fun checkOnEnter(context: Context) {
        if (hasOnCheckAction) {
            return
        }
        if (!AppUpdateCheckUtil.getCheckFlagOnEnter(context)) {
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
                    openUri(context, url)
                }
                .setPositiveButton("不再提示") { _, _ ->
                    AppUpdateCheckUtil.setCheckFlagOnEnter(context, false)
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
                    openUri(context, url)
                }
                .setOnDismissListener {
                    hasOnCheckAction = false
                }
                .show()
        }
    }

    private fun openUri(context: Context, url: String) {
        val intent = Intent()
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

}