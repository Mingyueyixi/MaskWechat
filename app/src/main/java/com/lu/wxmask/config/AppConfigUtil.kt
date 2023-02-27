package com.lu.wxmask.config

import android.net.Uri
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.R
import com.lu.wxmask.util.TimeExpiredCalculator
import com.lu.wxmask.util.http.HttpConnectUtil
import java.io.File

class AppConfigUtil {
    companion object {
        private val configFilePath = "res/raw/app_config.json"
        val githubMainUrl = "https://raw.githubusercontent.com/Mingyueyixi/MaskWechat/main"

        //@main分支 或者@v1.6， commit id之类的，直接在写/main有时候不行
        //不指定版本，则取最后一个https://www.jsdelivr.com/?docs=gh
        val cdnMainUrl = "https://cdn.jsdelivr.net/gh/Mingyueyixi/MaskWechat@main"
        var config: AppConfig = AppConfig()

        //5分钟过期时间
        val releaseNoteExpiredSetting by lazy { TimeExpiredCalculator(5 * 60 * 1000L) }

        //  不通过构造函数创建对象
        // UnsafeAllocator.INSTANCE.newInstance(AppConfig::class.java)
        fun load(callBack: ((config: AppConfig, fromRemote: Boolean) -> Unit)? = null) {
            val rawUrl = "$githubMainUrl/$configFilePath"
            //例如分支v1.12, 写法url编码，且前缀加@v：@vv1.12%2Fdev
            val cdnUrl = "$cdnMainUrl/$configFilePath"

            val file = File(AppUtil.getContext().filesDir, configFilePath)
            if (!file.exists()) {
                try {
                    file.parentFile.mkdirs()
                } catch (e: Exception) {
                }
            }
            HttpConnectUtil.get(rawUrl, HttpConnectUtil.noCacheHttpHeader) { raw ->
                if (raw.error != null || raw.code != 200) {
                    LogUtil.d("request raw fail, $rawUrl", raw)
                    HttpConnectUtil.get(cdnUrl, HttpConnectUtil.noCacheHttpHeader) { cdn ->
                        if (cdn.error == null && cdn.code == 200) {
                            parseConfig(cdn.body)
                            saveLocalFile(file, cdn.body)
                            callBack?.invoke(config, true)
                        } else {
                            LogUtil.d("request cdn fail, $cdnUrl", cdn)
                            //本地读取
                            parseLocalConfig(file)
                            callBack?.invoke(config, false)
                        }
                    }
                } else {
                    parseConfig(raw.body)
                    saveLocalFile(file, raw.body)
                    callBack?.invoke(config, true)
                }
            }
        }

        private fun parseLocalConfig(file: File) {
            file.readBytes().let {
                parseConfig(it)
            }
        }

        private fun parseConfig(data: ByteArray) {
            val jsonText = data.toString(Charsets.UTF_8)
            val configBean = try {
                GsonUtil.fromJson(jsonText, AppConfig::class.java)
            } catch (e: Throwable) {
                LogUtil.w(e)
                null
            }
            if (configBean != null) {
                config = configBean
            }

        }

        private fun saveLocalFile(file: File, data: ByteArray) {
            file.outputStream().use {
                it.write(data)
            }
        }


        fun getReleaseNoteWebUrl(): String {
            val filePath = "res/html/releases_note.html"
            val cdnUrl = "$cdnMainUrl/$filePath"
            val githubUrl = "$githubMainUrl/$filePath"

            val file = getLocalFile(filePath)
            if (!file.exists()) {
                try {
                    file.parentFile.mkdirs()
                    AppUtil.getContext().resources.openRawResource(R.raw.releases_note).use { inStream ->
                        saveLocalFile(file, inStream.readBytes())
                    }
                } catch (e: Exception) {
                }
                releaseNoteExpiredSetting.updateLastTime(0)
            }
            if (releaseNoteExpiredSetting.isExpired()) {
                HttpConnectUtil.get(githubUrl) { github ->
                    if (github.error == null && github.body.isNotEmpty()) {
                        saveLocalFile(file, github.body)
                        releaseNoteExpiredSetting.updateLastTime()
                    } else {
                        LogUtil.i("get fail: ", githubUrl, github)
                        HttpConnectUtil.get(cdnUrl) { cdn ->
                            if (cdn.error == null && cdn.body.isNotEmpty()) {
                                saveLocalFile(file, cdn.body)
                                releaseNoteExpiredSetting.updateLastTime()
                            } else {
                                LogUtil.i("get fail: ", cdnUrl, cdn)
                            }
                        }
                    }
                }
            }
            return Uri.fromFile(file).toString()
        }

        private fun getLocalFile(relativePath: String): File {
            return File(AppUtil.getContext().filesDir, relativePath)
        }
    }
}