package com.lu.wxmask.bean

import androidx.annotation.Keep
import com.google.gson.JsonObject
import com.lu.magic.util.GsonUtil
import com.lu.wxmask.Constrant

@Keep
class MaskItemBean(
    var maskId: String,
    var tagName: String = "",
    var tipMode: Int = Constrant.WX_MASK_TIP_MODE_ALERT,
    var tipData: JsonObject = JsonObject()
) {

    @Keep
    class AlertTipData(var mess: String = Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT) {
        companion object {
            @JvmStatic
            fun from(wrapper: MaskItemBean): AlertTipData {
                return GsonUtil.fromJson(wrapper.tipData, AlertTipData::class.java)
            }
        }
    }
}

