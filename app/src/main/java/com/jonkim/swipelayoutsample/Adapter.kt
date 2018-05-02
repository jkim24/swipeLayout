package com.jonkim.swipelayoutsample

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jonkim.swipelayout.SwipeLayout
import java.lang.IllegalArgumentException

class Adapter(val list : List<Any>, val context: Context, var listener : Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.swipe_item -> return ViewHolder(LayoutInflater.from(context).inflate(R.layout.swipe_item, parent, false), listener)
            R.layout.second_swipe_item -> return SecondViewHolder(LayoutInflater.from(context).inflate(R.layout.second_swipe_item, parent, false), listener)
            R.layout.third_swipe_item -> return ThirdViewHolder(LayoutInflater.from(context).inflate(R.layout.third_swipe_item, parent, false), listener)
            R.layout.fourth_swipe_item -> return FourthViewHolder(LayoutInflater.from(context).inflate(R.layout.fourth_swipe_item, parent, false), listener)
            else -> throw IllegalArgumentException("No viewHolder specified for "+ viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (list[position]) {
            is Adapter.ViewHolder.First -> return R.layout.swipe_item
            is Adapter.SecondViewHolder.Second -> return R.layout.second_swipe_item
            is Adapter.ThirdViewHolder.Third -> return R.layout.third_swipe_item
            is Adapter.FourthViewHolder.Fourth -> return R.layout.fourth_swipe_item
            else -> throw IllegalArgumentException("No Layout Specified for "+list[position])
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(list[position] as ViewHolder.First)
            is SecondViewHolder -> holder.bind(list[position] as SecondViewHolder.Second)
            is ThirdViewHolder -> holder.bind(list[position] as ThirdViewHolder.Third)
            is FourthViewHolder -> holder.bind(list[position] as FourthViewHolder.Fourth)
        }
    }

    class ViewHolder(itemView: View, val listener: Listener) : RecyclerView.ViewHolder(itemView) {
        var swipeLayout : SwipeLayout = itemView.findViewById(R.id.swipe)
        var topView = itemView.findViewById<View>(R.id.topView)
        var bottomView = itemView.findViewById<View>(R.id.bottomView)
        fun bind(item: First) {
            if (item.swiped) swipeLayout.open(false)
            else swipeLayout.close(true)

            topView.setOnClickListener {
                listener.onClick("TopView Clicked")
            }
            bottomView.setOnClickListener {
                listener.onClick("BottomView Clicked")
            }

        }
        class First() {
            var swiped = false
        }
    }

    class SecondViewHolder(itemView: View, val listener: Listener) : RecyclerView.ViewHolder(itemView){
        var swipeLayout : SwipeLayout = itemView.findViewById(R.id.secondSwipe)
        var topView = itemView.findViewById<View>(R.id.topView)
        var bottomView = itemView.findViewById<View>(R.id.bottomView)

        fun bind(item: Second) {
            if (item.swiped) swipeLayout.open(false)
            else swipeLayout.close(true)

            topView.setOnClickListener {
                listener.onClick("TopView Clicked")
            }
            bottomView.setOnClickListener {
                listener.onClick("BottomView Clicked")
            }
        }

        class Second() {
            var swiped = false
        }
    }

    class ThirdViewHolder(itemView: View, val listener: Listener) : RecyclerView.ViewHolder(itemView) {
        var swipeLayout : SwipeLayout = itemView.findViewById(R.id.thirdSwipe)
        var topView = itemView.findViewById<View>(R.id.topView)
        var bottomView = itemView.findViewById<View>(R.id.bottomView)

        fun bind(item: Third) {
            if (item.swiped) swipeLayout.open(false)
            else swipeLayout.close(true)

            topView.setOnClickListener {
                listener.onClick("TopView Clicked")
            }
            bottomView.setOnClickListener {
                listener.onClick("BottomView Clicked")
            }
        }
        class Third() {
            var swiped = false
        }
    }

    class FourthViewHolder(itemView: View, val listener: Listener) : RecyclerView.ViewHolder(itemView) {
        var swipeLayout : SwipeLayout = itemView.findViewById(R.id.fourthSwipe)
        var topView = itemView.findViewById<View>(R.id.topView)
        var bottomView = itemView.findViewById<View>(R.id.bottomView)

        fun bind(item: Fourth) {
            if (item.swiped) swipeLayout.open(false)
            else swipeLayout.close(true)

            topView.setOnClickListener {
                listener.onClick("TopView Clicked")
            }
            bottomView.setOnClickListener {
                listener.onClick("BottomView Clicked")
            }
        }
        class Fourth() {
            var swiped = false
        }
    }

    interface Listener {
        fun onClick(message : String)
    }
}