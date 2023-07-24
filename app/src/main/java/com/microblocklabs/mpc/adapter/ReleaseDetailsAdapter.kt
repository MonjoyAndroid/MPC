package com.microblocklabs.mpc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.model.ReleaseItem

class ReleaseDetailsAdapter(private val mList: List<ReleaseItem>, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        // create new views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder = if (viewType == TYPE_HEADER) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_release_details, parent, false)
                ViewHolderHeader(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_release_details, parent, false)
                ViewHolder(view)
            }

            return holder
        }

        // binds the list items to a view
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (holder.itemViewType == TYPE_HEADER){
                val headerHolder = holder as ViewHolderHeader
                headerHolder.tvDate.text = "Next Release Date"
                headerHolder.tvToken.text = "Tokens"
                headerHolder.tvStatus.text = "Status"
                headerHolder.tvDate.setTextColor(context.resources.getColor(R.color.white))
                headerHolder.tvToken.setTextColor(context.resources.getColor(R.color.white))
                headerHolder.tvStatus.setTextColor(context.resources.getColor(R.color.white))
            }else{
                val itemHolder = holder as ViewHolder
                val itemModel = mList[position-1]
                itemHolder.tvDate.text = itemModel.nextDate
                itemHolder.tvToken.text = itemModel.token
                itemHolder.tvStatus.text = itemModel.status

                if(itemModel.status == "Released"){
                    itemHolder.tvDate.setTextColor(context.resources.getColor(R.color.white))
                    itemHolder.tvToken.setTextColor(context.resources.getColor(R.color.white))
                    itemHolder.tvStatus.setTextColor(context.resources.getColor(R.color.base_primary))
                }else{
                    itemHolder.tvDate.setTextColor(context.resources.getColor(R.color.grey))
                    itemHolder.tvToken.setTextColor(context.resources.getColor(R.color.grey))
                    itemHolder.tvStatus.setTextColor(context.resources.getColor(R.color.grey))
                }
            }

        }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

        // return the number of the items in the list
        override fun getItemCount(): Int {
            return mList.size +1
        }

    // view holder for header view
    class ViewHolderHeader(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvToken: TextView = itemView.findViewById(R.id.tv_token)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    // view holder for item view
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvToken: TextView = itemView.findViewById(R.id.tv_token)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}