package com.jonkim.swipelayoutsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        open.setOnClickListener {
            swipe.open()
        }

        close.setOnClickListener {
            swipe.close()
        }

        bottomView.setOnClickListener {
            Toast.makeText(this, "Bottom View Clicked", Toast.LENGTH_SHORT).show()
        }

        topView.setOnClickListener {
            Toast.makeText(this, "Top View Clicked", Toast.LENGTH_SHORT).show()
        }


        secondOpen.setOnClickListener {
            secondSwipe.open()
        }

        secondClose.setOnClickListener {
            secondSwipe.close()
        }

        secondBottomView.setOnClickListener {
            Toast.makeText(this, "Bottom View Clicked", Toast.LENGTH_SHORT).show()
        }

        secondTopView.setOnClickListener {
            Toast.makeText(this, "Top View Clicked", Toast.LENGTH_SHORT).show()
        }

        thirdOpen.setOnClickListener {
            thirdSwipe.open()
        }

        thirdClose.setOnClickListener {
            thirdSwipe.close()
        }

        thirdTopView.setOnClickListener {
            Toast.makeText(this, "Pink View Clicked", Toast.LENGTH_SHORT).show()
        }

        thirdBottomView.setOnClickListener {
            Toast.makeText(this, "Blue View Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
