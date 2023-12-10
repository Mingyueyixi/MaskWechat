package com.lu.wxmask;

import android.app.Application;

import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.lu.wxmask.ui.JsonMenuManager;

import org.jetbrains.annotations.NotNull;

public final class App extends Application implements ViewModelStoreOwner {
    public static final Companion Companion = new Companion();
    public static App instance;

    public void onCreate() {
        super.onCreate();
        Companion.setInstance(this);
        JsonMenuManager.Companion.updateMenuListFromRemote(this);
    }

    @Override
    public ViewModelStore getViewModelStore() {
        return new ViewModelStore();
    }

    public static final class Companion {
        private Companion() {
        }

        public final App getInstance() {
            App app = instance;
            if (app != null) {
                return app;
            }
            return null;
        }

        public final void setInstance(@NotNull App app) {
            instance = app;
        }

    }
}
