package com.lu.wxmask.plugin.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.core.view.NestedScrollingParent
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.plugin.ui.Theme
import com.lu.wxmask.util.KeyBoxUtil
import kotlin.math.abs

class BottomPopUI(
    val contentView: View,
    /**
     * 顶部可拖动区域高度
     */
    var topDragBoundHeight: Int = 0,
) : AttachUI(contentView.context), NestedScrollingParent {
    private val mContentContainer = BottomDragLayout(context)
    var needScrollChild: View? = null

    //需要进行动画平移的是contentView而不是容器，因为当前容器参数不定，transfer会引发大量layout残影
    private var mTransferView = contentView

    init {
        mContentContainer.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        mContentContainer.isClickable = true
    }

    override fun onCreateView(container: ViewGroup): View {
        container.setBackgroundColor(Theme.Color.bgColorDialogTranslucence)
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
            duration = 400
            interpolator = DecelerateInterpolator()
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
                duration = 400
                interpolator = DecelerateInterpolator()
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
        if (!isShowing()) {
            mTransferView.visibility = View.INVISIBLE
        }
        super.show()
    }

    inner class BottomDragLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
        private var downY: Float = 0f
        private var isOnBounds = false
        private var touchY: Float = 0f
        private var lastTouchY: Float = 0f
        private var isCanDrag = false
        private val OFFSET_LIMIT = ViewConfiguration.get(context).scaledTouchSlop

        override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
            if (event == null) {
                return super.onInterceptTouchEvent(event)
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                downY = event.y
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                val distance = event.y - downY
                if (distance > 0 && needScrollChild?.canScrollVertically(-1) == false) {
                    //手指向下滑动，视图已经到顶部
                    return true
                }
                if (distance < 0 && needScrollChild?.canScrollVertically(1) == false) {
                    //手指向上滑动，视图已经到底部
                    return true
                }
            }
            return super.onInterceptTouchEvent(event)
        }


        override fun onTouchEvent(event: MotionEvent?): Boolean {
            if (event == null) {
                return false
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    lastTouchY = touchY
                    touchY = event.y
                    isOnBounds = topDragBoundHeight <= 0 || downY < topDragBoundHeight
                }

                MotionEvent.ACTION_MOVE -> {
                    isOnBounds = topDragBoundHeight <= 0 || downY < topDragBoundHeight
                    val distance = event.y - downY
                    if (abs(event.y - touchY) > OFFSET_LIMIT) {
                        isCanDrag = true
                    }
                    if (isOnBounds && distance > 0 && isCanDrag) {
                        mTransferView.translationY = distance
                    }
                    lastTouchY = touchY
                    touchY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    val offset = event.y - lastTouchY
                    LogUtil.d(">>>", offset, OFFSET_LIMIT)
                    if (offset > OFFSET_LIMIT) {
                        //手指向下滑动趋势
                        dismiss()
                    } else if (offset < 0 && -offset > OFFSET_LIMIT) {
                        //手指向上滑动趋势
                        show()
                    } else {
                        if (mTransferView.translationY == 0f) {
                            performClick()
                        } else if (mTransferView.translationY > mTransferView.height / 5f) {
                            dismiss()
                        } else {
                            show()
                        }
                    }
                }

            }
            return super.onTouchEvent(event)
        }
    }
}