package com.jonkim.swipelayoutsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

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

        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.adapter = Adapter(getList())

    }

    fun getList(): List<String> {
        val list = ArrayList<String>()
        list.add("First")
        list.add("Second")
        list.add("Third")
        list.add("Fourth")
        list.add("Fifth")
        list.add("Sixth")
        list.add("Seventh")
        return list
    }
}
