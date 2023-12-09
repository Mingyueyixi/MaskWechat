package com.lu.wxmask.util;

import android.view.View;
import android.view.ViewGroup;

import com.lu.magic.util.function.Consumer;
import com.lu.magic.util.function.Predicate;

import java.util.ArrayList;
import java.util.List;

public class ChildDeepCheck {
    public void each(View rootView, Consumer<View> function) {
        if (rootView != null && rootView instanceof ViewGroup) {
            function.accept(rootView);
            ViewGroup viewGroup = (ViewGroup) rootView;
            ArrayList<ViewGroup> parentList = new ArrayList();
            parentList.add(viewGroup);

            while (parentList.size() > 0) {
                ViewGroup parent = (ViewGroup) parentList.get(0);
                parentList.remove(0);

                for (int i = 0; i < parent.getChildCount(); ++i) {
                    View view = parent.getChildAt(i);
                    function.accept(view);
                    if (view instanceof ViewGroup) {
                        parentList.add((ViewGroup) view);
                    }
                }
            }

        }
    }

    public <T extends View> List<T> filter(View rootView, Class<T> tClass, Predicate<View> function) {
        List<T> viewList = new ArrayList();
        this.each(rootView, (view) -> {
            if (function.test(view)) {
                viewList.add((T) tClass.cast(view));
            }

        });
        return viewList;
    }

    public List<View> filter(View rootView, Predicate<View> function) {
        return this.filter(rootView, View.class, function);
    }

    public boolean eachCheck(View rootView, Predicate<View> function) {
        if (rootView != null && rootView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) rootView;
            ArrayList<ViewGroup> parentList = new ArrayList();
            parentList.add(viewGroup);

            while (parentList.size() > 0) {
                ViewGroup parent = (ViewGroup) parentList.get(0);
                parentList.remove(0);

                for (int i = 0; i < parent.getChildCount(); ++i) {
                    View view = parent.getChildAt(i);
                    if (function.test(view)) {
                        return true;
                    }

                    if (view instanceof ViewGroup) {
                        parentList.add((ViewGroup) view);
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }
}
