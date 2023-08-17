package com.microblocklabs.mpc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.model.NetworkItem
import com.microblocklabs.mpc.utility.CommonUtils


class CategoryDropdownAdapter(context: Context, categories: List<NetworkItem>) :
    RecyclerView.Adapter<CategoryDropdownAdapter.CategoryViewHolder?>() {
    private val categories: List<NetworkItem>
    private val context: Context
    private var categorySelectedListener: CategorySelectedListener? = null

    init {
        this.categories = categories
        this.context = context
    }

    fun setCategorySelectedListener(categorySelectedListener: CategorySelectedListener?) {
        this.categorySelectedListener = categorySelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_popup, parent, false)
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category: NetworkItem = categories[position]
//        holder.ivIcon.setImageResource(category.iconRes)
        holder.tvCategory.text = category.networkName
//        if(position== 0){
//            holder.layoutNetworkMenu.background = context.resources.getDrawable(R.color.card_color)
//        }else{
//            holder.layoutNetworkMenu.background = context.resources.getDrawable(R.color.grey)
//        }
        holder.itemView.setOnClickListener {
            if (categorySelectedListener != null) {
                if(position== 0){
                    categorySelectedListener!!.onCategorySelected(position, category)
                }else{
                    CommonUtils.showToastMessage(context, "${category.networkName} is not available now. It is coming soon.")
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCategory: AppCompatTextView
        var layoutNetworkMenu: ConstraintLayout


        init {
            layoutNetworkMenu = itemView.findViewById<ConstraintLayout>(R.id.layout_network_menu_row)
            tvCategory = itemView.findViewById<AppCompatTextView>(R.id.tv_item_name)
        }
    }

    interface CategorySelectedListener {
        fun onCategorySelected(position: Int, category: NetworkItem?)
    }
}