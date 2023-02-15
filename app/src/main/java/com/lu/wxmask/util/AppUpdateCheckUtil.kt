package com.lu.wxmask.util

import android.content.res.Resources.NotFoundException
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.thread.AppExecutor
import com.lu.wxmask.util.http.HttpConnectUtil
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class AppUpdateCheckUtil {
    companion object {
        //val sourceLastReleaseUrl = "https://api.github.com/repos/Mingyueyixi/MaskWechat/releases/latest"
        val repoLastReleaseUrl = "https://api.github.com/repos/Xposed-Modules-Repo/com.lu.wxmask/releases/latest"
        private val key_check_app_update_on_enter = "check_app_update_on_enter"

        fun setCheckFlagOnEnter(check: Boolean) {
            LocalKVUtil.getDefaultTable().edit().putBoolean(key_check_app_update_on_enter, check).apply()
        }

        fun hasCheckFlagOnEnter(): Boolean {
            return LocalKVUtil.getDefaultTable().getBoolean(key_check_app_update_on_enter, true)
        }

        fun checkUpdate(callBack: (downloadUrl: String, name: String, err: Throwable?) -> Unit) {
            val currVersionCode = AppVersionUtil.getVersionCode()
            if (currVersionCode == -1) {
                AppExecutor.executeMain {
                    callBack.invoke("", "", IllegalStateException("无法获取当前版本号"))
                }
                return
            }
            checkLastReleaseByRepo { url, code, name, err ->
                if (url != null && code > currVersionCode) {
                    AppExecutor.executeMain {
                        callBack.invoke(url, name, null)
                    }
                } else {
                    AppExecutor.executeMain {
                        callBack.invoke("", "", err)
                    }
                }
            }
        }

        private fun checkLastReleaseByRepo(callBack: (url: String?, versionCode: Int, versionName: String, error: Throwable?) -> Unit) {
            HttpConnectUtil.get(repoLastReleaseUrl) {
                var apkDownloadUrl: String? = null
                var error: Throwable? = null
                var versionCode = -1
                var versionName = ""

                try {
                    if (it.error != null) {
                        throw Exception(error)
                    }
                    if (it.code != 200) {
                        throw Exception("response code is not 200")
                    }
                    val json = JSONObject(it.body.toString(Charsets.UTF_8))
                    val tagName = json.optString("tag_name")
                    val assets = json.optJSONArray("assets")

                    if (tagName.isBlank() || assets == null || assets.length() == 0) {
                        throw Exception("tagName is Blank or assets is empty")
                    } else {
                        apkDownloadUrl = findDownloadUrl(assets)
                        if (apkDownloadUrl == null) {
                            throw Exception("assets node can't find downloadUrl")
                        }

                        val index = tagName.indexOf("-")
                        if (index > -1) {
                            versionCode = tagName.substring(0, index).toInt()
                            versionName = tagName.substring(index + 1)
                        } else {
                            throw Exception("tag_name format error: $tagName")
                        }
                    }

                } catch (e: Exception) {
                    error = e
                }
                callBack.invoke(apkDownloadUrl, versionCode, versionName, error)
            }
        }

        private fun findDownloadUrl(assetsJsonArr: JSONArray): String? {
            for (i in 0 until assetsJsonArr.length()) {
                val ele = assetsJsonArr.getJSONObject(i)
                val url = ele.optString("browser_download_url")
                if (url.endsWith(".apk")) {
                    return url
                }
            }
            return null
        }

    }
}