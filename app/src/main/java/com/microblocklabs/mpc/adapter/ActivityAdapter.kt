package com.microblocklabs.mpc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.RelativeSizeSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.utility.CommonUtils


class ActivityAdapter(private val context: Context, private val fragment: Fragment?, private val mList: List<ActivityModel>) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

//    private val mList: MutableList<ActivityModel> = mutableListOf()

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
            holder.imageView.rotation = 90F
            holder.tvActivityName.text = itemsViewModel.activityName
            holder.tvActivityAddress.text = "From: ${itemsViewModel.activityReceiverAddress}"
        }

        holder.tvActivityAmount.text = "${itemsViewModel.activityAmount} CIFD"
        holder.tvActivityDate.text = CommonUtils.convertDateFormat(itemsViewModel.dateStr)
        val transactionHashStr = itemsViewModel.transactionHash
        val spannableText = SpannableString(transactionHashStr)
        spannableText.setSpan(UnderlineSpan(), 0, spannableText.length, 0)
        val startTruncate = spannableText.length / 3 // Start of the middle portion
        val endTruncate = spannableText.length * 2 / 3 // End of the middle portion
        spannableText.setSpan(
            RelativeSizeSpan(0.8f), // Decrease the text size by 20%
            startTruncate, endTruncate,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.tvTransactionHash.text = spannableText

        holder.tvTransactionHash.movementMethod = LinkMovementMethod.getInstance()
        holder.tvTransactionHash.setOnClickListener {
            openCifdaqScanWeb(spannableText.toString())
        }

        holder.imgCopyTransactionHash.setOnClickListener {
            CommonUtils.copyToClipBoard(context, "Transaction Hash",holder.tvTransactionHash.text.toString())
        }


    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<ActivityModel>) {

//        mList.addAll(newData)
        notifyDataSetChanged()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openCifdaqScanWeb(transactionId: String){
        val url = "https://cifdaqscan.io/tx/${transactionId}" // Replace with the actual URL

        // Create an Intent to open a web browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }




    // Holds the views for adding it to image and text
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