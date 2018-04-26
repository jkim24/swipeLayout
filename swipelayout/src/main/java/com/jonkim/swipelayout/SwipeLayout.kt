package com.jonkim.swipelayout

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout

class SwipeLayout :
        FrameLayout{

    enum class SwipeDirection(var value: Int){
        LEFT(0), RIGHT(1);

        companion object {
            fun from(value: Int): SwipeDirection {
                return enumValues<SwipeDirection>().first { it.value == value }
            }
        }
    }

    enum class Status(var value: Int) {
        OPEN(0), CLOSED(1);

        companion object {
            fun from(value: Int): Status {
                return enumValues<Status>().first { it.value == value }
            }
        }
    }

    private lateinit var viewDragHelper : ViewDragHelper
    private lateinit var swipeDirection : SwipeDirection
    private var swipeDuration : Long = 150
    private var status: Status = Status.CLOSED
    private var lastX = 0f
    private var touchSlop = 0

    constructor(context: Context?) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context?.obtainStyledAttributes(
                attrs,
                R.styleable.SwipeLayout,
                0,0
        )
        typedArray?.apply {
            try {
                swipeDirection = SwipeDirection.from(this.getInteger(R.styleable.SwipeLayout_swipeDirection, 0))
                swipeDuration = this.getInteger(R.styleable.SwipeLayout_swipeDurationInMilis, 75).toLong()
            } finally {
                this.recycle()
            }
        }
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        viewDragHelper = ViewDragHelper.create(this, 1.5f,dragHelperCallback)
    }

    private val dragHelperCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == getChildAt(1)
        }

        override fun getViewHorizontalDragRange(child: View): Int = getChildAt(0).width

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val topBound = paddingTop
            val bottomBound = paddingBottom

            return Math.min(Math.max(top, topBound), bottomBound)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            handleOnViewRelease(releasedChild, xvel)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            when (swipeDirection) {
                SwipeDirection.LEFT -> {
                    if (left > paddingRight) {
                        return paddingRight
                    }
                    if (left < -(paddingRight + getChildAt(0).width)) {
                        return - getChildAt(0).width
                    }
                    return left

                }
                SwipeDirection.RIGHT -> {
                    if (left < paddingLeft) {
                        return paddingLeft
                    }
                    if (left > paddingLeft + getChildAt(0).width) {
                        return paddingLeft + getChildAt(0).width
                    }
                    return left
                }
                else -> return super.clampViewPositionHorizontal(child, left, dx)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        //TODO
    }

    override fun computeScroll() {
        super.computeScroll()
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            viewDragHelper.processTouchEvent(this)
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        var isBeingDragged = false
        when (ev?.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> viewDragHelper.cancel()
            MotionEvent.ACTION_DOWN -> {
                viewDragHelper.processTouchEvent(ev)
                lastX = ev.x
            }
            MotionEvent.ACTION_MOVE -> {
                val x = ev.x
                val xDelta = Math.abs(x - lastX)
                if (xDelta > 10) isBeingDragged = true
            }
        }

        return isBeingDragged

//        return viewDragHelper.shouldInterceptTouchEvent(ev!!)
    }

    private fun handleOnViewRelease(releasedChild: View, xvel: Float) {
        if (swipeDirection == SwipeDirection.LEFT) {
            when {
                xvel < -1500 -> openWithSwipe(true)
                xvel > 1500 -> closeWithSwipe(true)
                releasedChild.x < -(getChildAt(0).width.div(2.0f)) -> openWithSwipe(true)
                releasedChild.x > -(getChildAt(0).width.div(2.0f)) -> closeWithSwipe(true)
            }
        } else if (swipeDirection == SwipeDirection.RIGHT) {
            when {
                xvel > 1500 -> openWithSwipe(false)
                xvel < -1500 -> closeWithSwipe(false)
                releasedChild.x > getChildAt(0).width.div(2.0f) -> openWithSwipe(false)
                releasedChild.x < getChildAt(0).width.div(2.0f) -> closeWithSwipe(false)
            }
        }
    }

    fun open() {
        if (getIsLeftSwipe()) {
            if (viewDragHelper.smoothSlideViewTo(getChildAt(1), -getChildAt(0).width, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            if (viewDragHelper.smoothSlideViewTo(getChildAt(1), getChildAt(0).width + paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    fun close() {
        if (getIsLeftSwipe()) {
            if (viewDragHelper.smoothSlideViewTo(getChildAt(1), paddingRight, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            if (viewDragHelper.smoothSlideViewTo(getChildAt(1), paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    private fun openWithSwipe(isLeftSwipe: Boolean) {
        if (isLeftSwipe) {
            if (viewDragHelper.settleCapturedViewAt(-getChildAt(0).width, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else if (!isLeftSwipe) {
            if (viewDragHelper.settleCapturedViewAt(getChildAt(0).width + paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    private fun closeWithSwipe(isLeftSwipe: Boolean) {
        if (isLeftSwipe) {
            if (viewDragHelper.settleCapturedViewAt(paddingRight, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else if (!isLeftSwipe) {
            if (viewDragHelper.settleCapturedViewAt(paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    private fun getIsLeftSwipe() : Boolean =
            swipeDirection == SwipeDirection.LEFT
}