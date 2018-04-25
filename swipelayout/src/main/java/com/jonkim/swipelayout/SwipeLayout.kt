package com.jonkim.swipelayout

import android.animation.Animator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout

class SwipeLayout :
        FrameLayout,
        Animator.AnimatorListener{

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
    private var isAnimationFinished = true
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
//                    Log.e("clamp", "left = " + left + " , dx = " + dx)
//                    val leftBound = - getChildAt(0).width
//                    val rightBound = paddingRight
//
//                    val newPos = child.x + dx
//                    when {
//                        newPos < leftBound -> child.x = leftBound.toFloat()
//                        newPos > rightBound -> child.x = rightBound.toFloat()
//                        else -> child.x = child.x + dx
//                    }
//                    return paddingRight
                    if (left > paddingLeft) {
                        return paddingLeft
                    }
                    if (left < paddingLeft - getChildAt(0).width){
                        return - getChildAt(0).width
                    }
                    return left

                }
                SwipeDirection.RIGHT -> {
                    val leftBound = paddingLeft
                    val rightBound = getChildAt(0).width + paddingLeft + paddingRight

                    val newPos = child.x + dx
                    when {
                        newPos > rightBound -> child.x = rightBound.toFloat()
                        newPos < leftBound -> child.x = leftBound.toFloat()
                        else -> child.x = child.x + dx
                    }
                    return paddingLeft
                }
                else -> return super.clampViewPositionHorizontal(child, left, dx)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
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
//        var isBeingDragged = false
//        when (ev?.action) {
//            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> viewDragHelper.processTouchEvent(ev)
//            MotionEvent.ACTION_DOWN -> {
//                viewDragHelper.processTouchEvent(ev)
//                lastX = ev.x
//            }
//            MotionEvent.ACTION_MOVE -> {
//                val x = ev.x
//                val xDelta = Math.abs(x - lastX)
//                if (xDelta > 10) isBeingDragged = true
//            }
//        }
//
//        return isBeingDragged


        return viewDragHelper.shouldInterceptTouchEvent(ev!!)
    }

    override fun onAnimationEnd(animation: Animator?) {
        isAnimationFinished = true
//        postInvalidate()
    }

    override fun onAnimationStart(animation: Animator?) {}

    override fun onAnimationRepeat(animation: Animator?) {}

    override fun onAnimationCancel(animation: Animator?) {}

    private fun handleOnViewRelease(releasedChild: View, xvel: Float) {
        if (swipeDirection == SwipeDirection.LEFT) {
            when {
                xvel < -1500 -> {
                    if (viewDragHelper.settleCapturedViewAt(-getChildAt(0).width, paddingTop)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                xvel > 1500 -> {
                    if (viewDragHelper.settleCapturedViewAt(paddingRight, paddingTop)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                releasedChild.x < -(getChildAt(0).width.div(2.0f)) -> {
                    if (viewDragHelper.settleCapturedViewAt(-getChildAt(0).width, paddingTop)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                releasedChild.x > -(getChildAt(0).width.div(2.0f)) -> {
                    if (viewDragHelper.settleCapturedViewAt(paddingRight, paddingTop)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
            }


//            when {
//                xvel < -1500 -> openWithSwipe(true)
//                xvel > 1500 -> closeWithSwipe(true)
//                releasedChild.x < -(getChildAt(0).width.div(2.0f)) -> openWithSwipe(true)
//                releasedChild.x > -(getChildAt(0).width.div(2.0f)) -> closeWithSwipe(true)
//            }
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
        if (isAnimationFinished) {
        }
        if (getIsLeftSwipe()) {
            if (viewDragHelper.smoothSlideViewTo(getChildAt(1), -getChildAt(0).width, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
//                getChildAt(1).animate()
//                        .x(-(getChildAt(0).width).toFloat())
//                        .setDuration(300)
//                        .setListener(this)
//                        .start()
//                status = Status.OPEN
            isAnimationFinished = false
        } else {
            getChildAt(1).animate()
                    .x(getChildAt(0).width + paddingLeft + paddingRight.toFloat())
                    .setDuration(300)
                    .setListener(this)
                    .start()
            status = Status.OPEN
            isAnimationFinished = false
        }
    }

    fun close() {
//        if (isAnimationFinished) {
//        }
        if (getIsLeftSwipe()) {
            if (viewDragHelper.smoothSlideViewTo(getChildAt(1), paddingRight, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
//                getChildAt(1).animate()
//                        .x(paddingRight.toFloat())
//                        .setDuration(300)
//                        .setListener(this)
//                        .start()
//                status = Status.CLOSED
            isAnimationFinished = false
        } else {
            getChildAt(1).animate()
                    .x(paddingLeft.toFloat())
                    .setDuration(300)
                    .setListener(this)
                    .start()
            status = Status.CLOSED
            isAnimationFinished = false
        }
    }

    private fun openWithSwipe(isLeftSwipe: Boolean) {
        if (isLeftSwipe) {
            getChildAt(1).animate()
                    .x(-(getChildAt(0).width).toFloat())
                    .setDuration(swipeDuration)
                    .setListener(this)
                    .start()
            status = Status.OPEN
            isAnimationFinished = false
        } else if (!isLeftSwipe) {
            getChildAt(1).animate()
                    .x(getChildAt(0).width + paddingLeft + paddingRight.toFloat())
                    .setDuration(swipeDuration)
                    .setListener(this)
                    .start()
            status = Status.OPEN
            isAnimationFinished = false
        }
    }

    private fun closeWithSwipe(isLeftSwipe: Boolean) {
        if (isLeftSwipe) {
            getChildAt(1).animate()
                    .x(paddingRight.toFloat())
                    .setDuration(swipeDuration)
                    .setListener(this)
                    .start()
            status = Status.CLOSED
            isAnimationFinished = false
        } else if (!isLeftSwipe) {
            getChildAt(1).animate()
                    .x(paddingLeft.toFloat())
                    .setDuration(swipeDuration)
                    .setListener(this)
                    .start()
            status = Status.CLOSED
            isAnimationFinished = false
        }
    }

    private fun getIsLeftSwipe() : Boolean =
            swipeDirection == SwipeDirection.LEFT
}
