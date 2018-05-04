# swipeLayout

### Goal:
In the last project we were developing for a client, we needed a swipe layout that would be able to swipe open with user gestures but also programmatically. While we found a few libraries that could do both, none could do both well. My goal was to make a swipe library that does both well and one that offers users some useful customization options.

### How it works:
SwipeLayout uses a [FrameLayout](https://developer.android.com/reference/android/widget/FrameLayout) to hold the two child views and the [ViewDragHelper](https://developer.android.com/reference/android/support/v4/widget/ViewDragHelper) class to manage the swipe behavior.

## Getting started

### Requirements:
//TODO
### Dependencies:
//TODO
### How to use:
1. Create two child views. A top view that's first shown to the user and a bottom view that will show once the layout is swiped.
    - The height of both views should be the same.
    - When creating the bottom view, set the view's position to the side you want the swipe to begin from (layout_gravity="end" should be set if you want the layout to swipe left).
    ```bash
    <RelativeLayout 
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_gravity="end"
        android:layout_width="wrap_content" android:layout_height="80dp">

        <TextView
            android:id="@+id/bottomView"
            android:text="BottomView"
            android:background="@color/colorAccent"
            android:layout_width="60dp"
            android:layout_height="80dp" />

    </RelativeLayout>
    ```



2. Create the swipeLayout with the 2 child views.
    - The first child view in the SwipeLayout will be the bottom view and the second view will be the top view.
    - You can specify swipe direction, drag sensitivity and minimum drag velocity through the xml attributes. It's not required as it'll default to the values you see below. 
```bash
<com.jonkim.swipelayout.SwipeLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/swipe"
    android:layout_alignParentTop="true"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clipToPadding="false"
    android:clipChildren="false"
    app:swipeDirection="left"
    app:dragSensitivity="1.0"
    app:minDragVelocity="300">

    <include
        android:id="@+id/bottomView"
        layout="@layout/bottom_list_item"/>

    <include
        android:id="@+id/topView"
        layout="@layout/top_list_item"/>

</com.jonkim.swipelayout.SwipeLayout>
```