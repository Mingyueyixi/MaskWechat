package com.lu.wxmask.plugin.ui.view

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.animation.addListener

class BottomPopUI(
    val content: View,
    /**
     * 顶部可拖动区域高度
     */
    var topDragBoundHeight: Int = 0
) : AttachUI(content.context) {
    private var downY: Float = 0f
    private var isCanMove = false
    private val mContentView = FrameLayout(context)

    init {
        mContentView.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        mContentView.isClickable = true
        mContentView.setOnTouchListener(DragMoveListener())
    }

    override fun onCreateView(container: ViewGroup): View {
        container.setBackgroundColor(0x66666666)
        container.setOnClickListener {
            dismiss()
        }
        mContentView.addView(content)
        return mContentView
    }

    override fun dismiss() {
        if (!isShowing()) {
            return
        }
        val startValue = if (mContentView.translationY > 0) {
            mContentView.translationY
        } else {
            0f
        }
        ValueAnimator.ofFloat(startValue, mContentView.height.toFloat()).apply {
            duration = 300
            addUpdateListener {
                mContentView.translationY = it.animatedValue as Float
            }
            addListener(onEnd = {
                mContentView.translationY = 0f
                super.dismiss()
            })
        }.start()
    }

    override fun show() {
        if (isShowing() && mContentView.translationY <= 0f) {
            return
        }
        post {
            val startValue = if (mContentView.translationY > 0f) {
                mContentView.translationY
            } else if (mContentView.height > 0f) {
                mContentView.height.toFloat()
            } else {
                mContentView.layoutParams.height.toFloat()
            }
            ValueAnimator.ofFloat(startValue, 0f).apply {
                duration = 200
                addUpdateListener {
                    mContentView.translationY = it.animatedValue as Float
                }
                addListener(
                    onStart = {
                        mContentView.visibility = View.VISIBLE
                    },
                    onEnd = {
                        mContentView.translationY = 0f
                    })
            }.start()
        }
        mContentView.visibility = View.INVISIBLE
        super.show()
    }


    private inner class DragMoveListener : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event == null) {
                return false
            }
            if (topDragBoundHeight <= 0) {
                return false
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    isCanMove = downY < topDragBoundHeight
                    return isCanMove
                }

                MotionEvent.ACTION_MOVE -> {
                    val distance = event.y - downY
                    if (isCanMove && distance > 0) {
                        mContentView.translationY = distance
                        return true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (mContentView.translationY == 0f) {
                        v?.performClick()
                        return false
                    } else if (mContentView.translationY > mContentView.height / 5f) {
                        dismiss()
                    } else {
                        show()
                    }
                    return true
                }

            }
            return false
        }

    }
}