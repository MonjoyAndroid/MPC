package com.microblocklabs.mpc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.model.TokenModel

class ActivityAdapter(private val mList: List<ActivityModel>) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.imageView.setImageResource(itemsViewModel.activityIcon)
        holder.tvActivityName.text = itemsViewModel.activityName
        holder.tvActivityAddress.text = itemsViewModel.activityaddress
        holder.tvActivityAmount.text = itemsViewModel.activityAmount
        holder.tvActivityChanges.text = itemsViewModel.activityChanges
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img_activity)
        val tvActivityName: TextView = itemView.findViewById(R.id.activity_name)
        val tvActivityAddress: TextView = itemView.findViewById(R.id.activity_address)
        val tvActivityAmount: TextView = itemView.findViewById(R.id.activity_amount)
        val tvActivityChanges: TextView = itemView.findViewById(R.id.activity_changes)
    }
}