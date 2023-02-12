package com.lu.wxmask.config

import android.content.Context
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.IOUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.util.http.HttpConnectUtil

class AppConfigUtil {
    companion object {
        private val configFileName = "app_config.json"
        var config: AppConfig = AppConfig()

        //            不通过构造函数创建对象
//            UnsafeAllocator.INSTANCE.newInstance(AppConfig::class.java)
        fun load(callBack: ((config: AppConfig, fromRemote: Boolean) -> Unit)? = null) {
            val releatePath = "res/raw/app_config.json"
            val rawUrl = "https://raw.githubusercontent.com/Mingyueyixi/MaskWechat/main/$releatePath"
            val cdnUrl = "https://cdn.jsdelivr.net/gh/Mingyueyixi/MaskWechat@vv1.12%2Fdev/$releatePath"

            HttpConnectUtil.get(rawUrl, HttpConnectUtil.noCacheHttpHeader) { raw ->
                if (raw.error != null || raw.code != 200) {
                    LogUtil.d("request raw fail, $rawUrl", raw)
                    HttpConnectUtil.get(cdnUrl, HttpConnectUtil.noCacheHttpHeader) { cdn ->
                        if (cdn.error == null && cdn.code == 200) {
                            parseConfig(cdn.body)
                            saveConfig(cdn.body)
                            callBack?.invoke(config, true)
                        } else {
                            LogUtil.d("request cdn fail, $cdnUrl", cdn)
                            //本地读取
                            parseLocalConfig()
                            callBack?.invoke(config, false)
                        }
                    }
                } else {
                    parseConfig(raw.body)
                    saveConfig(raw.body)
                    callBack?.invoke(config, true)
                }
            }
        }

        private fun parseLocalConfig() {
            AppUtil.getContext().openFileInput(configFileName).use {
                parseConfig(it.readBytes())
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

        private fun saveConfig(data: ByteArray) {
            val outStream = AppUtil.getContext().openFileOutput(configFileName, Context.MODE_PRIVATE)
            IOUtil.writeByByte(data, outStream)
            IOUtil.closeQuietly(outStream)
        }


    }
}