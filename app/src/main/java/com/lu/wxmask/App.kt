package com.lu.wxmask

import android.app.Application
import com.lu.wxmask.ui.JsonMenuManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        JsonMenuManager.updateMenuListFromRemote(this)
    }
    companion object{
        lateinit var instance: App
    }

}