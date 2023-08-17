package com.microblocklabs.mpc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.fragment.ActivityFragment
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.utility.CommonUtils


class ActivityAdapter(private val context: Context, private val fragment: Fragment, private val mList: List<ActivityModel>) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        if(itemsViewModel.activityName == "Send"){
            holder.imageView.setImageResource(R.drawable.send_token_icon)
            holder.tvActivityName.text = itemsViewModel.activityName
            holder.tvActivityAddress.text = "To: ${itemsViewModel.activitySenderAddress}"
        }else{
            holder.imageView.setImageResource(R.drawable.receive_token_icon)
            holder.tvActivityName.text = itemsViewModel.activityName
            holder.tvActivityAddress.text = "From: ${itemsViewModel.activityReceiverAddress}"
        }
        holder.tvActivityAmount.text = "- ${itemsViewModel.activityAmount} CIFD"
        holder.tvActivityDate.text = CommonUtils.convertDateFormat(itemsViewModel.dateStr)
        val transactionHashStr = itemsViewModel.transactionHash
        val spannableText = SpannableString(transactionHashStr)
        spannableText.setSpan(UnderlineSpan(), 0, spannableText.length, 0)
        holder.tvTransactionHash.text = spannableText

        holder.tvTransactionHash.movementMethod = LinkMovementMethod.getInstance();
        holder.tvTransactionHash.setOnClickListener {
            (fragment as ActivityFragment?)!!.openCifdaqScanWeb()
        }

        holder.imgCopyTransactionHash.setOnClickListener {
            CommonUtils.copyToClipBoard(context, holder.tvTransactionHash.text.toString())
        }


    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    @Suppress("DEPRECATION")
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img_activity)
        val tvActivityName: TextView = itemView.findViewById(R.id.activity_name)
        val tvActivityAddress: TextView = itemView.findViewById(R.id.activity_address)
        val tvActivityAmount: TextView = itemView.findViewById(R.id.activity_amount)
        val tvActivityDate: TextView = itemView.findViewById(R.id.activity_date)
        val tvTransactionHash: TextView = itemView.findViewById(R.id.transaction_hash)
        val imgCopyTransactionHash: ImageView = itemView.findViewById(R.id.img_copy_trans_hash)
    }
}