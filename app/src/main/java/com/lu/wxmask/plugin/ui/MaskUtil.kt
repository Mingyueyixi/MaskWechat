package com.lu.wxmask.plugin.ui

import com.lu.wxmask.bean.MaskItemBean


class MaskUtil {

    companion object {
        @JvmStatic
        fun checkExitMaskId(lst: List<MaskItemBean>, maskId: String): Boolean {
            return lst.indexOfFirst { it.maskId == maskId } > -1
        }

        @JvmStatic
        fun findIndex(lst: List<MaskItemBean>, maskId: String): Int {
            return lst.indexOfFirst { maskId == it.maskId }
        }
    }


}