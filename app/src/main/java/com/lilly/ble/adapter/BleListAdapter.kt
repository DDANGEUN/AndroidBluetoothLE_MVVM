package com.lilly.ble.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lilly.ble.R
import kotlinx.android.synthetic.main.rv_ble_item.view.*

class BleListAdapter
    : RecyclerView.Adapter<BleListAdapter.BleViewHolder>(){

    lateinit var mContext: Context
    private var items: ArrayList<BluetoothDevice>? = ArrayList()
    var connectItems: Int? = null
    private lateinit var itemClickListner: ItemClickListener
    lateinit var itemView:View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        mContext = parent.context
        itemView = LayoutInflater.from(mContext).inflate(R.layout.rv_ble_item, parent, false)
        return BleViewHolder(itemView)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
        holder.bind(items?.get(position))
        holder.itemView.btn_connect.setOnClickListener {
            itemClickListner.onClick(it, position, items?.get(position))
        }
        if(connectItems!=null){
            if(connectItems==position){
                holder.itemView.btn_connect.text = "disconnect"
            }
        }
    }
    override fun getItemCount(): Int {
        return items?.size?:0
    }
    fun setItem(item: ArrayList<BluetoothDevice>?){

        if(item==null) return
        items = item
        notifyDataSetChanged()
    }


    inner class BleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(currentDevice: BluetoothDevice?) {
            itemView.ble_name.text = currentDevice?.name
            itemView.ble_address.text = currentDevice?.address

        }
    }
    interface ItemClickListener {
        fun onClick(view: View, position: Int,device: BluetoothDevice?)
    }
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

}