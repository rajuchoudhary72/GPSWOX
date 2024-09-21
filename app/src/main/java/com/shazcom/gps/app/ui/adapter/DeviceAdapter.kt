package com.shazcom.gps.app.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.DeviceData
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.utils.collapse
import com.shazcom.gps.app.utils.expand
import kotlinx.android.synthetic.main.empty_layout.view.*
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceAdapter(deviceList: List<DeviceData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    val EMPTY_VIEW_TYPE = 0
    val NORMAL_VIEW_TYPE = 1

    private var dataItems: MutableList<DeviceData>? = null
    private var tempList: MutableList<DeviceData>? = null

    init {
        dataItems?.clear()
        dataItems = deviceList as MutableList<DeviceData>

        tempList?.clear()
        tempList = deviceList
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataItems?.size == 0) EMPTY_VIEW_TYPE else NORMAL_VIEW_TYPE
    }


    class DeviceAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.headerTxt
        val childItems = view.childItems
    }

    class DeviceAdapterEmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emptyTxt = view.emptyText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == EMPTY_VIEW_TYPE) {
            Log.e("View", "Empty")
            return DeviceAdapterEmptyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.empty_adapter_layout,
                    parent,
                    false
                )
            )
        } else {
            Log.e("View", "Normal")
            return DeviceAdapterViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_device,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        if (dataItems?.size!! > 0) {
            return dataItems?.size!!
        } else {
            return 1
        }
    }

    override fun onBindViewHolder(holderItem: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == NORMAL_VIEW_TYPE) {

            val item = dataItems?.get(position)
            val holder = holderItem as DeviceAdapterViewHolder

            holder.title.text = "${item?.title} (${item?.items?.size})"
            holder.title.setOnClickListener {
                if (holder.childItems.height == 0) {
                    expand(holder.childItems)
                    holder.title.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_remove_black_24dp,
                        0
                    )
                } else {
                    collapse(holder.childItems)
                    holder.title.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_add_black_24dp,
                        0
                    )
                }
            }

            holder.childItems.apply {
                layoutManager = LinearLayoutManager(holder.title.context)
                adapter = DeviceChildAdapter(item?.items!!)
            }
        } else {
            return
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {

                val charString = charSequence.toString()
                val filterResults = FilterResults()

                if (charString.isEmpty()) {
                    filterResults.count = tempList?.count()!!
                    filterResults.values = tempList
                } else {
                    val resultList = ArrayList<DeviceData>()
                    tempList?.forEach { it ->
                        val itemList = ArrayList<Items>()
                        val deviceItem = it.copy()
                        deviceItem.items.forEach { mItem ->
                            if (mItem.name?.contains(
                                    charString,
                                    true
                                )!! || mItem.icon_color?.equals(charString, true)!!
                            ) {
                                itemList.add(mItem)
                            }
                        }

                        if(itemList.size > 0) {
                            deviceItem.items = itemList
                            resultList.add(deviceItem)
                        }
                    }

                    filterResults.values = resultList
                }

                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                dataItems = filterResults.values as MutableList<DeviceData>?
                Log.e("size", "${dataItems?.size!!}")
                notifyDataSetChanged()
            }
        }
    }

    fun updateItems(data: List<DeviceData>) {
        tempList?.clear()
        tempList?.addAll(data as MutableList<DeviceData>)

        dataItems?.clear()
        dataItems?.addAll(data as MutableList<DeviceData>)
    }
}