package com.lu.wxmask.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.Keep
import com.lu.lposed.api2.function.Consumer
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.R
import com.lu.wxmask.util.http.HttpConnectUtil
import java.nio.charset.Charset

class JsonMenuManager {
    @Keep
    class MenuBean(
        var groupId: Int = 0,
        var itemId: Int = 0,
        var order: Int = 0,
        var title: String? = "",
        var link: String? = "",
        var appLink: AppLink? = null
    )

    @Keep
    class AppLink(var links: Array<String?>? = null, var priority: Int = 0)

    companion object {
        private var isRemoteUpdating: Boolean = false
        private val menuFileName = "menu_ui.json"
        private var lastUpdateSuccessMills = 0L

        fun inflate(context: Context, menu: Menu) {
            for (menuBean in readMenuList(context)) {
                val menuItem = menu.add(menuBean.groupId, menuBean.itemId, menuBean.order, menuBean.title)
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
                menuItem.setOnMenuItemClickListener {
                    val appLink = menuBean.appLink
                    val clickLinkPriority = 0
                    val appLinks = appLink?.links
                    if (appLink == null || clickLinkPriority > appLink.priority || appLinks.isNullOrEmpty()) {
                        openLinkWith(context, menuBean.link) { err ->
                            LogUtil.w("open link error", err)
                        }
                    } else {
                        var failCount = 0
                        for (link in appLinks) {
                            openLinkWith(context, link) { err ->
                                failCount++;
                                LogUtil.w("open link faild", err)
                            }
                            if (failCount == 0) {
                                break
                            }
                        }
                        if (failCount == appLinks.size) {
                            LogUtil.w("open appLink with all error", it)
                            openLinkWith(context, menuBean.link) { err ->
                                LogUtil.w("try open link alse error", err)
                            }
                        }


                    }
                    return@setOnMenuItemClickListener true
                }
            }

        }

        private fun openLinkWith(context: Context, link: String?, onFail: Consumer<Throwable>? = null): Boolean {
            var openSuccess = true
            try {
                if (link == null) {
                    throw IllegalArgumentException("link is null")
                }
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(link)
                context.startActivity(intent)
            } catch (e: Throwable) {
                openSuccess = false
                onFail?.accept(e)
            }
            return openSuccess
        }

        private fun readMenuList(context: Context): ArrayList<MenuBean> {
            val file = context.getFileStreamPath(menuFileName)
            val retType = GsonUtil.getType(ArrayList::class.java, MenuBean::class.java)

            val ret: ArrayList<MenuBean> = try {
                if (file.exists()) {
                    GsonUtil.fromJson(file.readText(), retType)
                } else {
                    GsonUtil.fromJson(readMenuJsonTextFromRaw(context), retType)
                }
            } catch (e: Exception) {
                GsonUtil.fromJson(readMenuJsonTextFromRaw(context), retType)
            }
            return ret
        }

        private fun readMenuJsonTextFromRaw(context: Context): String {
            return context.resources.openRawResource(R.raw.menu_ui).use {
                it.readBytes().toString(Charset.forName("UTF-8"))
            }
        }

        fun updateMenuListFromRemoteIfNeed(ctx: Context) {
            // 2 hour
            if (!isRemoteUpdating && System.currentTimeMillis() - lastUpdateSuccessMills > 1000 * 60 * 60 * 2) {
                updateMenuListFromRemote(ctx)
            }
        }

        fun updateMenuListFromRemote(ctx: Context) {
            val releatePath = "res/raw/menu_ui.json"
            val context = ctx.applicationContext

            val rawJsonMenuUrl = "https://raw.githubusercontent.com/Mingyueyixi/MaskWechat/main/$releatePath"
            isRemoteUpdating = true
            HttpConnectUtil.getWithRetry(rawJsonMenuUrl, HttpConnectUtil.noCacheHttpHeader, 2, { retryCount, res ->
                LogUtil.i("onFetch retry:$retryCount", rawJsonMenuUrl)
            }, {
                if (it.error == null && it.code == 200 && it.body.isNotEmpty()) {
                    writeRemoteToLocal(context, it.body)
                    isRemoteUpdating = false
                } else {
                    LogUtil.w("request raw remote menu fail", it);
                    //val githubRawPath = "https://github.com/$releatePath"
                    //@main分支 或者@v1.6， commit id之类的，直接在写/main有时候不行
                    //不指定版本，则取最后一个https://www.jsdelivr.com/?docs=gh
                    val cdnRawPath = "https://cdn.jsdelivr.net/gh/Mingyueyixi/MaskWechat@main/$releatePath"
                    LogUtil.i("request $cdnRawPath")
                    HttpConnectUtil.get(cdnRawPath, HttpConnectUtil.noCacheHttpHeader) { cdnRes ->
                        if (cdnRes.error == null && cdnRes.code == 200 && cdnRes.body.isNotEmpty()) {
                            writeRemoteToLocal(context, cdnRes.body)
                        } else {
                            LogUtil.i("request jscdn remote menu fail", cdnRes)
                        }
                        isRemoteUpdating = false
                    }

                }

            })
        }

        private fun writeRemoteToLocal(context: Context, body: ByteArray) {
            context.openFileOutput(menuFileName, Context.MODE_PRIVATE).use { out ->
                out.write(body)
            }
            lastUpdateSuccessMills = System.currentTimeMillis()
        }

    }

}