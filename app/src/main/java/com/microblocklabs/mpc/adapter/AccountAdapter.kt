package com.microblocklabs.mpc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.activity.HomeScreenActivity
import com.microblocklabs.mpc.interfaces.OnBottomSheetItemClickListener
import com.microblocklabs.mpc.model.AccountModel
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.room.entity.WalletDetails

class AccountAdapter(private val activity: Activity, private val mList: List<WalletDetails>, private val listener: OnBottomSheetItemClickListener) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(itemsViewModel.accountIcon)
        holder.tvAccountName.text = itemsViewModel.accountName
        holder.tvAccountAddress.text = itemsViewModel.ethereumAddress
//        holder.tvAccountAmount.text = itemsViewModel.accountAmount
//        holder.tvAccountCoin.text = itemsViewModel.accountCoin

        holder.layoutAccountItem.setOnClickListener {
            listener.onItemDismiss(position)
            (activity as HomeScreenActivity).setHomeScreenData(position)
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val layoutAccountItem: ConstraintLayout = itemView.findViewById(R.id.layout_account_item)
        val imageView: ImageView = itemView.findViewById(R.id.img_account)
        val tvAccountName: TextView = itemView.findViewById(R.id.account_name)
        val tvAccountAddress: TextView = itemView.findViewById(R.id.account_address)
        val tvAccountAmount: TextView = itemView.findViewById(R.id.account_amount)
        val tvAccountCoin: TextView = itemView.findViewById(R.id.account_coin)
    }
}