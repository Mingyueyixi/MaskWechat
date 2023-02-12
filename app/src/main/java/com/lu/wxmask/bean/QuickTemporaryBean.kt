package com.lu.wxmask.bean

import androidx.annotation.Keep
import com.google.gson.JsonObject
import com.lu.magic.util.kxt.optInt
import com.lu.wxmask.Constrant


@Keep
open class BaseTemporary(var mode: Int)


@Keep
class QuickTemporaryBean(var duration: Int = 150, var clickCount: Int = 5) :
    BaseTemporary(Constrant.CONFIG_TEMPORARY_MODE_QUICK_CLICK) {

    constructor(json: JsonObject) : this(
        json.optInt("duration", 150), json.optInt("clickCount", 5)
    )

}