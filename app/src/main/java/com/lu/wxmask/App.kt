package com.lu.wxmask

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.lu.wxmask.ui.JsonMenuManager

class App : Application(), ViewModelStoreOwner {
    override fun onCreate() {
        super.onCreate()
        instance = this
        JsonMenuManager.updateMenuListFromRemote(this)
    }
    companion object{
        lateinit var instance: App
    }

    override fun getViewModelStore(): ViewModelStore {
        return ViewModelStore()
    }
}