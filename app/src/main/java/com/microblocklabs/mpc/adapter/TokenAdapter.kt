package com.microblocklabs.mpc.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.model.TokenModel

class TokenAdapter(private val mList: List<TokenModel>) : RecyclerView.Adapter<TokenAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_token, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.imageView.setImageResource(itemsViewModel.tokenIcon)
        holder.tvCoinName.text = itemsViewModel.tokenName
//        holder.tvCoinVolume.text = itemsViewModel.tokenVolume
//        holder.tvCoinAmount.text = itemsViewModel.tokenAmount
        holder.tvCoinPrice.text = itemsViewModel.tokenPrice
//        holder.tvCoinChanges.text = itemsViewModel.tokenChanges
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img_coin)
        val tvCoinName: TextView = itemView.findViewById(R.id.coin_name)
//        val tvCoinVolume: TextView = itemView.findViewById(R.id.coin_volume)
//        val tvCoinAmount: TextView = itemView.findViewById(R.id.coin_amount)
        val tvCoinPrice: TextView = itemView.findViewById(R.id.coin_price)
//        val tvCoinChanges: TextView = itemView.findViewById(R.id.coin_changes)
    }
}
