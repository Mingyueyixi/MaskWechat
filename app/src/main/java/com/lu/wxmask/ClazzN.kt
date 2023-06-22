package com.lu.wxmask

import com.lu.lposed.api2.XposedHelpers2
import com.lu.magic.util.AppUtil

interface ClazzN {
    companion object {
        const val BaseChattingUIFragment = "com.tencent.mm.ui.chatting.BaseChattingUIFragment"
        const val LauncherUI = "com.tencent.mm.ui.LauncherUI"
        const val ChattingUI = "com.tencent.mm.ui.chatting.ChattingUI"
        const val MainUI = "com.tencent.mm.ui.conversation.MainUI"
        const val ConversationListView = "com.tencent.mm.ui.conversation.ConversationListView"
        const val BaseContact = "com.tencent.mm.autogen.table.BaseContact"
        const val BaseConversation = "com.tencent.mm.autogen.table.BaseConversation"

        //        const val com_tencent_mm_boot_Buildconfig = "com.tencent.mm.boot.BuildConfig"
        @JvmStatic
        @JvmOverloads
        fun from(clazz: String, classLoader: ClassLoader = AppUtil.getContext().classLoader): Class<*>? {
            return XposedHelpers2.findClassIfExists(clazz, classLoader)
        }
    }
}