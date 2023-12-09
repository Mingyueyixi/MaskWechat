package com.lu.wxmask.util;

import android.app.Activity;
import android.app.Fragment;

import androidx.annotation.IdRes;

import java.util.LinkedHashMap;

public class FragmentNavigation {
    private Activity activity;
    private @IdRes int containerId;
    private LinkedHashMap<String, Fragment> fragmentMap;

    public FragmentNavigation(Activity activity, int containerId) {
        this.activity = activity;
        this.containerId = containerId;
    }

    public void navigate(Fragment fragment) {
        String tag = fragment.getClass().getName();
        Fragment frag = activity.getFragmentManager().findFragmentByTag(tag);
        if (frag != null) {
            //&& frag == fragment ???
            activity.getFragmentManager().beginTransaction()
                    .show(fragment)
                    .commitNowAllowingStateLoss();
        } else {
            activity.getFragmentManager().beginTransaction()
                    .add(containerId, fragment, tag)
                    .commitNowAllowingStateLoss();
        }
    }

    public boolean navigateBack() {
        return activity.getFragmentManager().popBackStackImmediate();
    }
}
