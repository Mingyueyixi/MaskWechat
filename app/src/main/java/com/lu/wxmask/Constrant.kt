package com.lu.wxmask

class Constrant {
    companion object {
        const val ONE_YEAR_MILLS = 1000L * 60 * 60 * 24 * 365L
        const val ONE_DAY_MILLS = 1000L * 60 * 60 * 24L

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
        const val CONFIG_TIP_MODE_ALERT = 0

        /** 临时解除模式：快速点击 */
        const val CONFIG_TEMPORARY_MODE_QUICK_CLICK = 0
        const val CONFIG_TEMPORARY_MODE_LONG_PRESS = 1
        const val CONFIG_TEMPORARY_MODE_CIPHER = 2

        /** 静默模式，点击符合条件的用户，进行静默处理，即无反应，不能发起聊天 */
        const val WX_MASK_TIP_MODE_SILENT = 10086
        const val WX_MASK_TIP_ALERT_MESS_DEFAULT = "该用户已对您私密（拉黑），请联系对方解除~"

        //微信版本号
        const val WX_CODE_8_0_22 = 2140
        const val WX_CODE_8_0_32 = 2300
        const val WX_CODE_8_0_33 = 2320
        const val WX_CODE_8_0_34 = 2340

        //不知道为毛8.0.35找到一个和版本号与8.0.34重复的版本
        //const val WX_CODE_8_0_35 = 2340
        const val WX_CODE_8_0_35 = 2360
        const val WX_CODE_8_0_37 = 2380
        const val WX_CODE_8_0_38 = 2400
        const val WX_CODE_8_0_40 = 2420
        const val WX_CODE_8_0_41 = 2441
        const val WX_CODE_8_0_42 = 2460
        const val WX_CODE_8_0_43 = 2480
        const val WX_CODE_8_0_44 = 2502
        const val WX_CODE_8_0_45 = 2521
        const val WX_CODE_8_0_46 = 2540
        const val WX_CODE_8_0_47 = 2560
    }

}