package com.lu.wxmask

class Constrant {
    companion object {
        //intent key, 标记来源是 Mask App
        const val KEY_INTENT_FROM_MASK = "KEY_INTENT_FROM_MASK"

        const val KEY_INTENT_PLUGIN_MODE = "KEY_INTENT_PLUGIN_MODE"
        /** 模式：管理配置 */
        const val VALUE_INTENT_PLUGIN_MODE_MANAGER = 1
        /** 模式：添加配置 */
        const val VALUE_INTENT_PLUGIN_MODE_ADD = 2

        //-------------
        //配置相关
        //------------
        /** 提示模式，点击符合条件的用户，弹出提示对话框 */
        const val WX_MASK_TIP_MODE_ALERT = 0
        /** 静默模式，点击符合条件的用户，进行静默处理，即无反应，不能发起聊天 */
        const val WX_MASK_TIP_MODE_SILENT = 10086
        const val WX_MASK_TIP_ALERT_MESS_DEFAULT = "该用户对您私密哦"
    }

}