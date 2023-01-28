package com.lu.wxmask.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.Keep
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.thread.AppExecutor
import com.lu.wxmask.R
import com.lu.wxmask.util.http.HttpConnectUtil
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset

class JsonMenuManager {
    @Keep
    class MenuBean(
        var groupId: Int = 0,
        var itemId: Int = 0,
        var order: Int = 0,
        var title: String = "",
        var link: String = ""
    )

    companion object {
        val menuFileName = "menu_ui.json"
        fun inflate(context: Context, menu: Menu) {
            for (menuBean in readMenuList(context)) {
                val menuItem = menu.add(menuBean.groupId, menuBean.itemId, menuBean.order, menuBean.title)
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
                if (menuBean.link.isBlank()) {
                    continue
                }
                menuItem.setOnMenuItemClickListener {
                    val clickLink = menuBean.link
                    if (clickLink.startsWith("https://") || clickLink.startsWith("http://")) {
                        val intent = Intent()
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.data = Uri.parse(menuBean.link)
                        context.startActivity(intent)
                    }
                    return@setOnMenuItemClickListener true
                }
            }

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

        fun updateMenuListFromRemote(context: Context) {
            val releatePath = "Mingyueyixi/MaskWechat/tree/main/app/src/main/res/raw/menu_ui.json"
            //val githubRawPath = "https://github.com/$releatePath"
            val cdnRawPath = "https://cdn.jsdelivr.net/gh/$releatePath"

            HttpConnectUtil.get(cdnRawPath) {
                if (it.error != null && it.code == 200 && it.body.isNotEmpty()) {
                    context.openFileOutput(menuFileName, Context.MODE_PRIVATE).use { out ->
                        out.write(it.body)
                    }
                }
            }

        }

    }

}