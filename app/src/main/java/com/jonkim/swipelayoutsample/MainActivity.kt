package com.jonkim.swipelayoutsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.swipe_item.*
import java.util.*

class MainActivity :
        AppCompatActivity(),
        Adapter.Listener{

    lateinit var adapter : Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        open.setOnClickListener {
//            swipe.open()
//        }
//        close.setOnClickListener {
//            swipe.close()
//        }
//        bottomView.setOnClickListener {
//            Toast.makeText(this, "Bottom View Clicked", Toast.LENGTH_SHORT).show()
//        }
//        topView.setOnClickListener {
//            Toast.makeText(this, "Top View Clicked", Toast.LENGTH_SHORT).show()
//        }
//        secondOpen.setOnClickListener {
//            secondSwipe.open()
//        }
//        secondClose.setOnClickListener {
//            secondSwipe.close()
//        }
//        secondBottomView.setOnClickListener {
//            Toast.makeText(this, "Bottom View Clicked", Toast.LENGTH_SHORT).show()
//        }
//        secondTopView.setOnClickListener {
//            Toast.makeText(this, "Top View Clicked", Toast.LENGTH_SHORT).show()
//        }

        adapter = Adapter(getList(), this, this)
        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.adapter = adapter

        secondOpen.setOnClickListener {
            openItems()
        }
        secondClose.setOnClickListener {
            closeItems()
        }

    }

    fun getList(): List<Any> {
        val list = ArrayList<Any>()
        list.add(Adapter.ViewHolder.First())
        list.add(Adapter.SecondViewHolder.Second())
        list.add(Adapter.ThirdViewHolder.Third())
        list.add(Adapter.FourthViewHolder.Fourth())
        list.add(Adapter.ViewHolder.First())
        list.add(Adapter.SecondViewHolder.Second())
        list.add(Adapter.ThirdViewHolder.Third())
        list.add(Adapter.FourthViewHolder.Fourth())
        return list
    }

    fun openItems() {
        for (i in adapter.list) {
            if (i is Adapter.ViewHolder.First) {
                i.swiped = true
            } else if (i is Adapter.SecondViewHolder.Second) {
                i.swiped = true
            } else if (i is Adapter.ThirdViewHolder.Third) {
                i.swiped = true
            } else if (i is Adapter.FourthViewHolder.Fourth) {
                i.swiped = true
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun closeItems() {
        for (i in adapter.list) {
            if (i is Adapter.ViewHolder.First) {
                i.swiped = false
            } else if (i is Adapter.SecondViewHolder.Second) {
                i.swiped = false
            } else if (i is Adapter.ThirdViewHolder.Third) {
                i.swiped = false
            } else if (i is Adapter.FourthViewHolder.Fourth) {
                i.swiped = false
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(message: String) {
        Toast.makeText(this, message,Toast.LENGTH_SHORT).show()
    }
}
