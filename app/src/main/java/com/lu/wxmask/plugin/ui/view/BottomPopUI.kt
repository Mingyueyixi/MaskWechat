package com.lu.wxmask.plugin.ui.view

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.animation.addListener

class BottomPopUI(
    val contentView: View,
    /**
     * 顶部可拖动区域高度
     */
    var topDragBoundHeight: Int = 0
) : AttachUI(contentView.context) {
    private val mContentContainer = FrameLayout(context)
    //需要进行动画平移的是contentView而不是容器，因为当前容器参数不定，transfer会引发大量layout残影
    private var mTransferView = contentView
    init {
        mContentContainer.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        mContentContainer.isClickable = true
        //使用容器，而不是contentView自己来监听，因为contentView是外层传进来的，避免事件覆盖
        mContentContainer.setOnTouchListener(DragMoveListener())
    }

    override fun onCreateView(container: ViewGroup): View {
        container.setBackgroundColor(0x66666666)
        container.setOnClickListener {
            dismiss()
        }
        mContentContainer.addView(contentView)
        return mContentContainer
    }

    override fun dismiss() {
        if (!isShowing()) {
            return
        }
        val startValue = if (mTransferView.translationY > 0) {
            mTransferView.translationY
        } else {
            0f
        }
        ValueAnimator.ofFloat(startValue, mTransferView.height.toFloat()).apply {
            duration = 300
            addUpdateListener {
                mTransferView.translationY = it.animatedValue as Float
            }
            addListener(onEnd = {
                mTransferView.translationY = 0f
                super.dismiss()
            })
        }.start()
    }

    override fun show() {
        if (isShowing() && mTransferView.translationY <= 0f) {
            return
        }
        post {
            val startValue = if (mTransferView.translationY > 0f) {
                mTransferView.translationY
            } else if (mTransferView.height > 0f) {
                mTransferView.height.toFloat()
            } else {
                mTransferView.layoutParams.height.toFloat()
            }
            ValueAnimator.ofFloat(startValue, 0f).apply {
                duration = 200
                addUpdateListener {
                    mTransferView.translationY = it.animatedValue as Float
                }
                addListener(
                    onStart = {
                        mTransferView.visibility = View.VISIBLE
                    },
                    onEnd = {
                        mTransferView.translationY = 0f
                    })
            }.start()
        }
        mTransferView.visibility = View.INVISIBLE
        super.show()
    }


    private inner class DragMoveListener : OnTouchListener {
        private var downY: Float = 0f
        private var isOnBounds = false
        private var moveY: Float = 0f
        private var isCanDrag = false
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
                    isOnBounds = downY < topDragBoundHeight
                    return isOnBounds
                }

                MotionEvent.ACTION_MOVE -> {
                    val distance = event.y - downY
                    if (Math.abs(event.y - moveY) > ViewConfiguration.get(context).scaledTouchSlop) {
                        isCanDrag = true
                    }
                    if (isOnBounds && distance > 0 && isCanDrag) {
                        mTransferView.translationY = distance
                        moveY = event.y
                        return true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (mTransferView.translationY == 0f) {
                        v?.performClick()
                        return false
                    } else if (mTransferView.translationY > mTransferView.height / 5f) {
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