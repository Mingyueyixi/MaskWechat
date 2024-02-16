package com.lu.wxmask.util.ext

import com.lu.magic.util.AppUtil
import com.lu.magic.util.ResUtil

private val resMap = HashMap<String, Int>()

@JvmOverloads
fun ResUtil.getViewId(idName: String, packageName: String = AppUtil.getPackageName()): Int {
    val k = "@id/$idName"
    var id = resMap[k]
    if (id == null) {
        id = AppUtil.getContext().resources.getIdentifier(idName, "id", packageName)
        resMap[k] = id
    }
    return id;
}


