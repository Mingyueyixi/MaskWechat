package com.lu.wxmask

class Constrant {
    companion object {
        const val KEY_INTENT_MASK_WECHAT = "KEY_INTENT_MASK_WECHAT"
        /** 提示模式，点击符合条件的用户，弹出提示对话框 */
        const val WX_MASK_TIP_MODE_ALERT = 0
        /** 静默模式，点击符合条件的用户，进行静默处理，即无反应，不能发起聊天 */
        const val WX_MASK_TIP_MODE_SILENT = 10086
        const val WX_MASK_TIP_ALERT_MESS_DEFAULT = "该用户对您私密哦"
    }

}