package com.example.workingparents.Goback

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workingparents.R
import kotlinx.android.synthetic.main.emergency_item.view.*
import kotlin.collections.ArrayList

class EmergencyAdapter(private val items: ArrayList<EmergencyData>) : RecyclerView.Adapter<EmergencyAdapter.ViewHolder>() {

    private var TAG="Emergency"

    interface OnItemClickListener{
        fun onItemClick(data: EmergencyData, pos : Int)

    }

    interface OnItemClickListener2{
        fun onItemClick(data: EmergencyData, pos : Int)

    }
    private var listener : OnItemClickListener? = null
    private var listener2 : OnItemClickListener2? = null

    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }

    fun setOnItemClickListener2(listener2 : OnItemClickListener2) {
        this.listener2 = listener2
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: EmergencyAdapter.ViewHolder, position: Int) {

        val item = items[position]
        val listener = View.OnClickListener { it ->
            if(position!= RecyclerView.NO_POSITION)
            {
                listener?.onItemClick(item,position)
            }
        }

        val listener2 = View.OnClickListener { it ->
            if(position!= RecyclerView.NO_POSITION)
            {
                listener2?.onItemClick(item,position)
            }
        }

        holder.apply {
            bind(listener, listener2, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.emergency_item, parent, false)
        return EmergencyAdapter.ViewHolder(inflatedView)
    }

    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, listener2: View.OnClickListener, item: EmergencyData) {
            view.emergency_childName.text = item.emergency_childName

            //버튼 클릭 시
            view.emergency_btnCall.setOnClickListener(listener)

            view.emergency_btnMsg.setOnClickListener(listener2)

            }

            }

        }
