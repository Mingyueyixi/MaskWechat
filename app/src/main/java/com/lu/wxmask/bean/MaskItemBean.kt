package com.lu.wxmask.bean

import androidx.annotation.Keep
import com.google.gson.JsonObject
import com.lu.wxmask.Constrant

@Keep
class MaskItemBean(
    var keyWord: String,
    var tipMode: Int = Constrant.WX_MASK_TIP_MODE_ALERT,
    var tipData: JsonObject = JsonObject()
) {

    @Keep
    class AlertTipData(var mess: String = Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT)
}

