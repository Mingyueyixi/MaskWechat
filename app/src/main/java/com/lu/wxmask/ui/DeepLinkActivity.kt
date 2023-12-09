package com.lu.wxmask.ui

import android.content.Intent
import android.os.Bundle
import com.lu.wxmask.base.BaseActivity

class DeepLinkActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.putExtra("from", DeepLinkActivity::class.java.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

//        intent.data?.let {
//            if (MaskAppRouter.isPageGroup(it)) {
//                MaskAppRouter.route(this, it.toString())
//                finish()
//                return
//            }
//        }
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}