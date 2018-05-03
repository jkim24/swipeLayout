# swipeLayout


### How to use:
1. Create two child views. A top view that's first shown to the user and a bottom view that will show once the layout is swiped.
    - When creating the bottom view, set the view's position to the side you want the swipe to begin.
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


2. Create the swipeLayout with the 2 child views
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