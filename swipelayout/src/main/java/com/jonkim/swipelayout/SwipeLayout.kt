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

    enum class State(var value: Int) {
        OPEN(0), CLOSED(1);

        companion object {
            fun from(value: Int): State {
                return enumValues<State>().first { it.value == value }
            }
        }
    }

    private lateinit var viewDragHelper : ViewDragHelper
    private lateinit var swipeDirection : SwipeDirection
    private lateinit var gestureDetector : GestureDetectorCompat
    private lateinit var topView : View
    private lateinit var bottomView : View
    private var state : State = State.CLOSED
    private var swipeDuration : Long = 150
    private var topViewXOffSet : Int = 0
    private var topViewYOffSet : Int = 0
    private var dragDistance : Float = 0f
    private var lastX = 0f
    private var isScrolling = false
    private var mIsOpenBeforeInit = false

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
    //TODO MANAGE VIEWHOLDER RESET
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)
        topView.offsetLeftAndRight(topViewXOffSet)
        topView.offsetTopAndBottom(topViewYOffSet)
        if (mIsOpenBeforeInit) {
            Log.e("onLayout", "mIsOpenBeforeInit = TRUE" + mIsOpenBeforeInit)
            openWithoutAnimation()
        } else {
            Log.e("onLayout", "mIsOpenBeforeInit = FALSE" + mIsOpenBeforeInit)
            closeWithoutAnimation()
        }
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
                xvel < -300 -> less@ {
                    openWithSwipe(true)
                    return@less
                }
                xvel > 300 -> greater@{
                    closeWithSwipe(true)
                    return@greater
                }
                releasedChild.x < bottomView.width.div(2.0f).unaryMinus() -> lesserPosition@{
                    openWithSwipe(true)
                    return@lesserPosition
                }
                releasedChild.x > bottomView.width.div(2.0f).unaryMinus() -> greaterPosition@{
                    closeWithSwipe(true)
                    return@greaterPosition
                }
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

    private fun computeSurfaceLayoutArea(open: Boolean): Rect {
        var l = paddingLeft
        if (open) {
            if (swipeDirection == SwipeDirection.RIGHT)
                l = paddingLeft + dragDistance.toInt()
            else if (swipeDirection == SwipeDirection.LEFT)
                l = paddingLeft - dragDistance.toInt()
        }
        return Rect(l, paddingTop, l + measuredWidth, measuredHeight)
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
        state = State.OPEN
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
        state = State.CLOSED
    }

    fun openWithoutAnimation() {
        mIsOpenBeforeInit = true
        if (getIsLeftSwipe()) {
            val rect = computeSurfaceLayoutArea(true)
            topView.layout(rect.left, rect.top, rect.right, rect.bottom)
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            topView.x = bottomView.width.toFloat()
        }
        ViewCompat.postInvalidateOnAnimation(this)
        state = State.OPEN
    }

    fun closeWithoutAnimation() {
        mIsOpenBeforeInit = false
        if (getIsLeftSwipe()) {
            val rect = computeSurfaceLayoutArea(false)
            topView.layout(rect.left, rect.top, rect.right, rect.bottom)
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            topView.x = paddingLeft.toFloat()
        }
        ViewCompat.postInvalidateOnAnimation(this)
        state = State.CLOSED
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
        state = State.OPEN
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
        state = State.CLOSED
    }

    private fun getIsLeftSwipe() : Boolean =
            swipeDirection == SwipeDirection.LEFT

    fun isOpen() : Boolean = state == State.OPEN
}