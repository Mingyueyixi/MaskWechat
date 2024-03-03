package com.lu.wxmask.plugin.ui.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.get
import com.lu.wxmask.util.KeyBoxUtil
import com.lu.wxmask.util.ext.dp


open class AttachUI @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), ViewCompat.OnUnhandledKeyEventListenerCompat {
    protected var isViewCreated = false
    var onShowListener: ((v: AttachUI) -> Unit)? = null
    var onDismissListener: ((v: AttachUI) -> Unit)? = null

    var _rootContainer: ViewGroup? = null
    var rootContainer: ViewGroup? = _rootContainer
        get() {
            if (_rootContainer == null) {
                _rootContainer = (getActivity()?.window?.decorView) as ViewGroup?
            }
            return _rootContainer
        }


    open fun onCreateView(container: ViewGroup): View {
        return View(context)
    }

    open fun dismiss() {
        if (!isShowing()) {
            return
        }
        KeyBoxUtil.hideSoftInput(this)
        val container = parent as ViewGroup?
        container?.removeView(this)

        onDismissListener?.invoke(this)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
            dismiss()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }


    override fun onUnhandledKeyEvent(v: View, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            //处理未处理的按键事件，避免多个AttachUI实例，无法一一dismiss掉
            dismiss()
            return true
        }
        return false
    }

    override fun dispatchWindowFocusChanged(hasFocus: Boolean) {
        super.dispatchWindowFocusChanged(hasFocus)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        init()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        clearFocus()
    }

    open fun show() {
        if (isShowing()) {
            return
        }
        getActivity()?.let {
            KeyBoxUtil.hideSoftInput(it)
        }
        var lp = layoutParams
        if (lp == null) {
            lp = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT)
        } else {
            lp.width = MarginLayoutParams.MATCH_PARENT
            lp.height = MarginLayoutParams.MATCH_PARENT
        }
        if (parent == null) {
            rootContainer?.addView(this, lp)
        }
        onShowListener?.invoke(this)
        z = 99f
    }

    open fun isShowing(): Boolean {
        return parent != null && visibility == View.VISIBLE
    }

    private fun init() {
        //防止点击穿透
        isClickable = true

        //获取焦点，以便处理返回事件
        isFocusableInTouchMode = true
        requestFocus()

        if (Build.VERSION.SDK_INT >= 28) {
            //监听处理未处理的按键事件，避免多个AttachUI实例，无法一一dismiss掉
            ViewCompat.removeOnUnhandledKeyEventListener(this, this)
            ViewCompat.addOnUnhandledKeyEventListener(this, this)
        }

        setPadding(paddingLeft, 44.dp, paddingRight, paddingBottom)
        if (!isViewCreated) {
            addView(onCreateView(this))
        }
        isViewCreated = true

    }

    protected fun getActivity(): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            ctx = if (context is Activity) {
                return context as Activity?
            } else {
                (context as ContextWrapper).baseContext
            }
        }
        return null
    }


}