package com.jonkim.swipelayout

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.MotionEventCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout

class SwipeLayout :
        FrameLayout,
        View.OnTouchListener{

    enum class SwipeDirection(var value: Int){
        LEFT(0), RIGHT(1);

        companion object {
            fun from(value: Int): SwipeDirection {
                return enumValues<SwipeDirection>().first { it.value == value }
            }
        }
    }

    private lateinit var viewDragHelper : ViewDragHelper

    private lateinit var swipeDirection: SwipeDirection

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
        setOnTouchListener(this)
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
            Log.e("onViewReleased", "xvel = " + xvel.toString() + "yvel = " + yvel.toString())
            super.onViewReleased(releasedChild, xvel, yvel)
            handleOnViewRelease(releasedChild, xvel, yvel)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (swipeDirection == SwipeDirection.LEFT) {
                val leftBound = - getChildAt(0).width + paddingLeft
                val rightBound = paddingRight

                Log.e("clampHorizontal", "dx = " + dx.toString())
                Log.e("horizontalMin", Math.min(Math.max(left, leftBound), rightBound).toString())


                Math.min(Math.max(left, leftBound), rightBound)
            } else if (swipeDirection == SwipeDirection.RIGHT) {
                val leftBound = paddingLeft
                val rightBound = getChildAt(0).width + paddingRight

                Log.e("clampHorizontal", "dx = " + dx.toString())
                Log.e("horizontalMin", Math.min(Math.max(left, leftBound), rightBound).toString())


                Math.min(Math.max(left, leftBound), rightBound)
            } else super.clampViewPositionHorizontal(child, left, dx)

        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.apply {
            viewDragHelper.processTouchEvent(this)
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev?.action
        action?.apply {
            if (this == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                viewDragHelper.cancel()
                return false
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun handleOnViewRelease(releasedChild: View, xvel: Float, yvel: Float) {
        if (swipeDirection == SwipeDirection.LEFT) {
//            viewDragHelper.smoothSlideViewTo(releasedChild, -500,0)

            if (releasedChild.x < -(getChildAt(0).width/2)) {
                releasedChild.x = - getChildAt(0).width + paddingLeft.toFloat()
            } else {
                releasedChild.x = paddingRight.toFloat()
            }
        }
    }

    fun open() {
        //TODO manage when view is already opened
        val animation = TranslateAnimation(0f, -getChildAt(0).width.toFloat(), 0f, 0f)
        animation.duration = 300
        getChildAt(1).startAnimation(animation)
        animation.fillAfter = true
    }

    fun close() {
        //TODO manage when view is already closed
        val animation = TranslateAnimation(-getChildAt(0).width.toFloat(), 0f, 0f, 0f)
        animation.duration = 300
        getChildAt(1).startAnimation(animation)
        animation.fillAfter = true
    }
}
