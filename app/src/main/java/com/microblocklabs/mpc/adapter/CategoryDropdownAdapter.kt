package com.microblocklabs.mpc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.model.NetworkItem


class CategoryDropdownAdapter(categories: List<NetworkItem>) :
    RecyclerView.Adapter<CategoryDropdownAdapter.CategoryViewHolder?>() {
    private val categories: List<NetworkItem>
    private var categorySelectedListener: CategorySelectedListener? = null

    init {
        this.categories = categories
    }

    fun setCategorySelectedListener(categorySelectedListener: CategorySelectedListener?) {
        this.categorySelectedListener = categorySelectedListener
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_popup, parent, false)
        )
    }

    override fun onBindViewHolder(@NonNull holder: CategoryViewHolder, position: Int) {
        val category: NetworkItem = categories[position]
//        holder.ivIcon.setImageResource(category.iconRes)
        holder.tvCategory.setText(category.networkName)
        holder.itemView.setOnClickListener {
            if (categorySelectedListener != null) {
                categorySelectedListener!!.onCategorySelected(position, category)
            }
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCategory: AppCompatTextView

        init {
            tvCategory = itemView.findViewById<AppCompatTextView>(R.id.tv_item_name)
        }
    }

    interface CategorySelectedListener {
        fun onCategorySelected(position: Int, category: NetworkItem?)
    }
}