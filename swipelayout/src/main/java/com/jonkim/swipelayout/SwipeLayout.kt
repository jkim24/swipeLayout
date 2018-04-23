package com.jonkim.swipelayout

import android.animation.Animator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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
    private var status: Status = Status.CLOSED
    private var isAnimationFinished = true

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
        viewDragHelper = ViewDragHelper.create(this, dragHelperCallback)
    }

    private val dragHelperCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == getChildAt(1)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val topBound = paddingTop
            val bottomBound = paddingBottom

            return Math.min(Math.max(top, topBound), bottomBound)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            handleOnViewRelease(releasedChild, xvel, yvel)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            when (swipeDirection) {
                SwipeDirection.LEFT -> {
                    val leftBound = - (getChildAt(0).width + paddingRight)
                    val rightBound = paddingRight

                    val newPos = child.x + dx
                    when {
                        newPos < leftBound -> child.x = leftBound.toFloat()
                        newPos > rightBound -> child.x = rightBound.toFloat()
                        else -> child.x = child.x + dx
                    }
                    return 0

                }
                SwipeDirection.RIGHT -> {
                    val leftBound = paddingLeft
                    val rightBound = getChildAt(0).width + paddingLeft

                    val newPos = child.x + dx
                    when {
                        newPos > rightBound -> child.x = rightBound.toFloat()
                        newPos < leftBound -> child.x = leftBound.toFloat()
                        else -> child.x = child.x + dx
                    }
                    return 0
                }
                else -> return super.clampViewPositionHorizontal(child, left, dx)
            }
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
            MotionEvent.ACTION_DOWN -> viewDragHelper.processTouchEvent(ev)
            MotionEvent.ACTION_MOVE -> isBeingDragged = true
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> viewDragHelper.cancel()
        }

        return isBeingDragged
    }

    override fun onAnimationEnd(animation: Animator?) {
        isAnimationFinished = true
    }

    override fun onAnimationStart(animation: Animator?) {}

    override fun onAnimationRepeat(animation: Animator?) {}

    override fun onAnimationCancel(animation: Animator?) {}

    private fun handleOnViewRelease(releasedChild: View, xvel: Float, yvel: Float) {
        if (swipeDirection == SwipeDirection.LEFT) {
            if (releasedChild.x < -(getChildAt(0).width.div(2.0f))) {
                openWithSwipe(true)
            } else {
                closeWithSwipe(true)
            }
        } else if (swipeDirection == SwipeDirection.RIGHT) {
            if (releasedChild.x > getChildAt(0).width.div(2.0f)) {
                openWithSwipe(false)
            } else {
                closeWithSwipe(false)
            }
        }
        invalidate()
    }

    fun open() {
        if (isAnimationFinished) {
            if (getIsLeftSwipe()) {
                getChildAt(1).animate()
                        .x(-(getChildAt(0).width + paddingRight).toFloat())
                        .setDuration(300)
                        .setListener(this)
                        .start()
                status = Status.OPEN
                isAnimationFinished = false
            } else {
                getChildAt(1).animate()
                        .x(getChildAt(0).width + paddingLeft.toFloat())
                        .setDuration(300)
                        .setListener(this)
                        .start()
                status = Status.OPEN
                isAnimationFinished = false
            }
        }
    }

    fun close() {
        if (isAnimationFinished) {
            if (getIsLeftSwipe()) {
                getChildAt(1).animate()
                        .x(paddingRight.toFloat())
                        .setDuration(300)
                        .setListener(this)
                        .start()
                status = Status.CLOSED
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
    }

    private fun openWithSwipe(isLeftSwipe: Boolean) {
        if (isLeftSwipe) {
            getChildAt(1).animate()
                    .x(-(getChildAt(0).width + paddingRight).toFloat())
                    .setDuration(75)
                    .setListener(this)
                    .start()
            status = Status.OPEN
            isAnimationFinished = false
        } else if (!isLeftSwipe) {
            getChildAt(1).animate()
                    .x(getChildAt(0).width + paddingLeft.toFloat())
                    .setDuration(75)
                    .setListener(this)
                    .start()
            status = Status.OPEN
            isAnimationFinished = false
        }
    }

    private fun closeWithSwipe(isLeftSwipe: Boolean) {
        if (isAnimationFinished) {
            if (isLeftSwipe) {
                getChildAt(1).animate()
                        .x(paddingRight.toFloat())
                        .setDuration(75)
                        .setListener(this)
                        .start()
                status = Status.CLOSED
                isAnimationFinished = false
            } else if (!isLeftSwipe) {
                getChildAt(1).animate()
                        .x(paddingLeft.toFloat())
                        .setDuration(75)
                        .setListener(this)
                        .start()
                status = Status.CLOSED
                isAnimationFinished = false
            }
        }
    }

    private fun getIsLeftSwipe() : Boolean =
            swipeDirection == SwipeDirection.LEFT
}
