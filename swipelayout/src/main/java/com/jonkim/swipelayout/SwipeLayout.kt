package com.jonkim.swipelayout

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class SwipeLayout :
        ViewGroup{

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
    private lateinit var gestureDetector : GestureDetectorCompat
    private lateinit var topView : View
    private lateinit var bottomView : View
    private var swipeDuration : Long = 150
    private var topViewXOffSet : Int = 0
    private var topViewYOffSet : Int = 0
    private var dragDistance : Float = 0f
    private var lastX = 0f
    private var isScrolling = false
    private var mIsOpenBeforeInit = false
    private val mRectMainClose = Rect()
    private val mRectMainOpen = Rect()
    private val mRectSecClose = Rect()
    private val mRectSecOpen = Rect()

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
        viewDragHelper = ViewDragHelper.create(this, 1.5f,dragHelperCallback)
        gestureDetector = GestureDetectorCompat(context, gestureListener)
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        var hasDisallowed = false

        override fun onDown(e: MotionEvent?): Boolean {
            isScrolling = false
            hasDisallowed = false
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            isScrolling = true
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            isScrolling = true

            if (parent != null) parent.requestDisallowInterceptTouchEvent(true)

            return false
        }
    }

    private val dragHelperCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            viewDragHelper.captureChildView(topView, pointerId)
            return false
        }

        override fun getViewHorizontalDragRange(child: View): Int = getChildAt(0).width

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            ViewCompat.postInvalidateOnAnimation(this@SwipeLayout)
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
                    if (left < -(getChildAt(0).width)) {
                        return - getChildAt(0).width
                    }
                    return left

                }
                SwipeDirection.RIGHT -> {
                    if (left < paddingLeft) {
                        return paddingLeft
                    }
                    if (left > paddingLeft + bottomView.width) {
                        return paddingLeft + bottomView.width
                    }
                    return left
                }
                else -> return super.clampViewPositionHorizontal(child, left, dx)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount >= 2) {
            bottomView = getChildAt(0)
            topView = getChildAt(1)
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            topViewXOffSet = topView.left
            topViewYOffSet = topView.top
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)

            var left = 0
            var right = 0
            var top = 0
            var bottom = 0

            val minLeft = paddingLeft
            val maxRight = Math.max(r - paddingRight - l, 0)
            val minTop = paddingTop
            val maxBottom = Math.max(b - paddingBottom - t, 0)

            var measuredChildHeight = child.measuredHeight
            var measuredChildWidth = child.measuredWidth

            // need to take account if child size is match_parent
            val childParams = child.layoutParams
            var matchParentHeight = false
            var matchParentWidth = false

            if (childParams != null) {
                matchParentHeight = childParams.height == ViewGroup.LayoutParams.MATCH_PARENT
                matchParentWidth = childParams.width == ViewGroup.LayoutParams.MATCH_PARENT
            }

            if (matchParentHeight) {
                measuredChildHeight = maxBottom - minTop
                childParams!!.height = measuredChildHeight
            }

            if (matchParentWidth) {
                measuredChildWidth = maxRight - minLeft
                childParams!!.width = measuredChildWidth
            }

            when (swipeDirection) {
                SwipeDirection.LEFT -> {
                    left = Math.max(r - measuredChildWidth - paddingRight - l, minLeft)
                    top = Math.min(paddingTop, maxBottom)
                    right = Math.max(r - paddingRight - l, minLeft)
                    bottom = Math.min(measuredChildHeight + paddingTop, maxBottom)
                }

                SwipeDirection.RIGHT -> {
                    left = Math.min(paddingLeft, maxRight)
                    top = Math.min(paddingTop, maxBottom)
                    right = Math.min(measuredChildWidth + paddingLeft, maxRight)
                    bottom = Math.min(measuredChildHeight + paddingTop, maxBottom)
                }
            }

            child.layout(left, top, right, bottom)
        }
        initRects()

        if (mIsOpenBeforeInit) {
            openWithoutAnimation()
        } else {
            closeWithoutAnimation()
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount < 2) {
            throw RuntimeException("Layout must have two children")
        }

        val params = layoutParams

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        var desiredWidth = 0
        var desiredHeight = 0

        // first find the largest child
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }
        // create new measure spec using the largest child width
        val newWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(desiredWidth, widthMode)
        val newHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(desiredHeight, heightMode)

        val measuredWidth = View.MeasureSpec.getSize(newWidthMeasureSpec)
        val measuredHeight = View.MeasureSpec.getSize(newHeightMeasureSpec)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childParams = child.layoutParams

            if (childParams != null) {
                if (childParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.minimumHeight = measuredHeight
                }

                if (childParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.minimumWidth = measuredWidth
                }
            }

            measureChild(child, newWidthMeasureSpec, newHeightMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }

        // taking accounts of padding
        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        // adjust desired width
        if (widthMode == View.MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth
        } else {
            if (params.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth
            }

            if (widthMode == View.MeasureSpec.AT_MOST) {
                desiredWidth = if (desiredWidth > measuredWidth) measuredWidth else desiredWidth
            }
        }

        // adjust desired height
        if (heightMode == View.MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight
        } else {
            if (params.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight
            }

            if (heightMode == View.MeasureSpec.AT_MOST) {
                desiredHeight = if (desiredHeight > measuredHeight) measuredHeight else desiredHeight
            }
        }

        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            viewDragHelper.processTouchEvent(this)
            gestureDetector.onTouchEvent(this)
            return true
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        viewDragHelper.processTouchEvent(ev!!)
        gestureDetector.onTouchEvent(ev)
        accumulateDragDist(ev)

        val couldBecomeClick = couldBecomeClick(ev)
        val settling = viewDragHelper.viewDragState == ViewDragHelper.STATE_SETTLING
        val idleAfterScrolled = viewDragHelper.viewDragState == ViewDragHelper.STATE_IDLE && isScrolling

        lastX = ev.x
        return !couldBecomeClick && (settling || idleAfterScrolled)
    }

    private fun initRects() {
        // close position of main view
        mRectMainClose.set(
                topView.left,
                topView.top,
                topView.right,
                topView.bottom
        )

        // close position of secondary view
        mRectSecClose.set(
                bottomView.left,
                bottomView.top,
                bottomView.right,
                bottomView.bottom
        )

        // open position of the main view
        mRectMainOpen.set(
                getMainOpenLeft(),
                getMainOpenTop(),
                getMainOpenLeft() + topView.width,
                getMainOpenTop() + topView.height
        )

        // open position of the secondary view
        mRectSecOpen.set(
                getSecOpenLeft(),
                getSecOpenTop(),
                getSecOpenLeft() + bottomView.width,
                getSecOpenTop() + bottomView.height
        )
    }

    private fun getMainOpenLeft(): Int {
        when (swipeDirection) {
            SwipeDirection.LEFT -> return mRectMainClose.left + bottomView.width

            SwipeDirection.RIGHT -> return mRectMainClose.left - bottomView.width


            else -> return 0
        }
    }

    private fun getMainOpenTop(): Int {
        when (swipeDirection) {
            SwipeDirection.LEFT -> return mRectMainClose.top

            SwipeDirection.RIGHT -> return mRectMainClose.top


            else -> return 0
        }
    }

    private fun getSecOpenLeft(): Int {
        return mRectSecClose.left
    }

    private fun getSecOpenTop(): Int {
        return mRectSecClose.top
    }

    private fun couldBecomeClick(ev: MotionEvent): Boolean {
        return isInMainView(ev) && !shouldInitiateADrag()
    }

    private fun isInMainView(ev: MotionEvent): Boolean {
        val x = ev.x
        val y = ev.y

        val withinVertical = topView.top <= y && y <= topView.bottom
        val withinHorizontal = topView.left <= x && x <= topView.right
        return withinVertical && withinHorizontal
    }

    private fun shouldInitiateADrag(): Boolean {
        val minDistToInitiateDrag = viewDragHelper.touchSlop.toFloat()
        return dragDistance >= minDistToInitiateDrag
    }

    private fun accumulateDragDist(ev: MotionEvent) {
        val action = ev.action
        if (action == MotionEvent.ACTION_DOWN) {
            dragDistance = 0f
            return
        }

        val dragged = Math.abs(ev.x - lastX)

        dragDistance += dragged
    }

    private fun handleOnViewRelease(releasedChild: View, xvel: Float) {
        if (swipeDirection == SwipeDirection.LEFT) {
            when {
                xvel < -300 -> openWithSwipe(true)
                xvel > 300 -> closeWithSwipe(true)
                releasedChild.x < bottomView.width.div(2.0f).unaryMinus() -> openWithSwipe(true)
                releasedChild.x > bottomView.width.div(2.0f).unaryMinus() -> closeWithSwipe(true)
            }
        } else if (swipeDirection == SwipeDirection.RIGHT) {
            when {
                xvel > 300 -> openWithSwipe(false)
                xvel < -300 -> closeWithSwipe(false)
                releasedChild.x > bottomView.width.div(2.0f) -> openWithSwipe(false)
                releasedChild.x < bottomView.width.div(2.0f) -> closeWithSwipe(false)
            }
        }
    }

    fun open() {
        if (getIsLeftSwipe()) {
            if (viewDragHelper.smoothSlideViewTo(topView, bottomView.width.unaryMinus(), paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            if (viewDragHelper.smoothSlideViewTo(topView, bottomView.width + paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    fun close() {
        if (getIsLeftSwipe()) {
            if (viewDragHelper.smoothSlideViewTo(topView, paddingRight, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            if (viewDragHelper.smoothSlideViewTo(topView, paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    private fun openWithoutAnimation() {
        if (getIsLeftSwipe()) {
            topView.translationX = bottomView.width.unaryMinus().toFloat()
        } else {
            topView.translationX = bottomView.width.toFloat()
        }
    }

    private fun closeWithoutAnimation() {
        if (getIsLeftSwipe()) {
            topView.translationX = paddingRight.toFloat()
        } else {
            topView.translationX = paddingLeft.toFloat()
        }
    }

    private fun openWithSwipe(isLeftSwipe: Boolean) {
        mIsOpenBeforeInit = true
        if (isLeftSwipe) {
            if (viewDragHelper.settleCapturedViewAt(bottomView.width.unaryMinus(), paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else if (!isLeftSwipe) {
            if (viewDragHelper.settleCapturedViewAt(bottomView.width + paddingLeft, paddingTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    private fun closeWithSwipe(isLeftSwipe: Boolean) {
        mIsOpenBeforeInit = false
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