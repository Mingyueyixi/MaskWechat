package com.lu.wxmask.base;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.view.LayoutInflater;

public class BaseFragment extends Fragment {

    public LayoutInflater getLayoutInflaterCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getLayoutInflater();
        }
        return LayoutInflater.from(getContext());
    }

    //compat androidx requireActivity function
    public Activity requireActivity() {
        Activity act = getActivity();
        if (act == null) {
            throw new NullPointerException();
        }
        return act;
    }
}
