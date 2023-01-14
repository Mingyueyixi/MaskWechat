package com.lu.mask.donate.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class AutoFillParentImage extends ImageView {
    public AutoFillParentImage(Context context) {
        super(context);
    }

    public AutoFillParentImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFillParentImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoFillParentImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp == null) {
            return;
        }
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableHeight <= 0 || drawableWidth <= 0) {
            return;
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            int pw = parent.getMeasuredWidth();
            int ph = parent.getMeasuredHeight();
            if (ph <= 0 || pw <= 0) {
                return;
            }
            float maxWidth = pw - parent.getPaddingRight() - parent.getPaddingLeft();
            float maxHeight = ph - parent.getPaddingTop() - parent.getPaddingBottom();

            float drawableRatio = drawableWidth / (float) drawableHeight;
            float targetWidth = maxWidth;
            float targetHeight = targetWidth / drawableRatio;

            if (targetHeight > maxHeight) {
                //假设放大到与父布局允许的最大宽度，高度超出了最大值
                //则说明应以高度为基准放大
                targetHeight = maxHeight;
                targetWidth = maxHeight * drawableRatio;
            }
            setMeasuredDimension((int) targetWidth, (int) targetHeight);
        }
    }


}
