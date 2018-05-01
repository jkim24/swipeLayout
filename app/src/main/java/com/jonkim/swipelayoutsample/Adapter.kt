package com.jonkim.swipelayoutsample

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jonkim.swipelayout.SwipeLayout

class Adapter(val list : List<String>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.swipe_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = list[position]
        if (holder.swipeLayout.isOpen()) {
            Log.e("onBind", "triedToClose")
            holder.swipeLayout.closeWithoutAnimation()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var swipeLayout : SwipeLayout = itemView.findViewById(R.id.swipe)
        var textView : TextView = itemView.findViewById(R.id.mainLayoutTv)
    }
}