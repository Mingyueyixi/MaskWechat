package com.lu.wxmask.bean

import androidx.annotation.Keep
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.lu.magic.util.GsonUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.util.ext.toJsonObject
import org.json.JSONObject

@Keep
class MaskItemBean(
    var maskId: String,
    var tagName: String = "",
    var tipMode: Int = Constrant.WX_MASK_TIP_MODE_SILENT,
    var tipData: JsonElement? = JsonObject(),
    //伪装映射id
    var mapId: String = "officialaccounts"
) {

    companion object {
        fun fromJson(jsonText: String): MaskItemBean {
            val json = try {
                JSONObject(jsonText)
            } catch (e: Exception) {
                JSONObject()
            }
            return MaskItemBean(
                maskId = json.optString("maskId", ""),
                tagName = json.optString("tagName", ""),
                tipMode = json.optInt("tipMode", Constrant.WX_MASK_TIP_MODE_SILENT),
                tipData = GsonUtil.fromJson(json.optString("tipData", "{}"), JsonObject::class.java),
                mapId = json.optString("mapId", "officialaccounts")
            )
        }
    }

    @Keep
    class TipData(var mess: String = Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT) {
        companion object {
            @JvmStatic
            fun from(wrapper: MaskItemBean): TipData {
                return try {
                    GsonUtil.fromJson(wrapper.tipData, TipData::class.java).also {
                        //gson使用的是unsafe的方式来构建的实例，绕过了构造函数，所以仍然可能为空
                        //即当一个json如“{}”传进来，gson生成的实例的属性是空的，而不是kotlin声明的非空
                        //可以使用kotlin自己的json序列化库来避免此类问题：https://github.com/Kotlin/kotlinx.serialization
                        @Suppress("SENSELESS_COMPARISON")
                        if (it.mess == null) {
                            it.mess = Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT
                        }
                    }
                } catch (e: Exception) {
                    TipData()
                }
            }
        }
    }

}

