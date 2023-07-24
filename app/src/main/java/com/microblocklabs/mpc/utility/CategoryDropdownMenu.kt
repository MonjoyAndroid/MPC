package com.microblocklabs.mpc.utility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.CategoryDropdownAdapter
import com.microblocklabs.mpc.adapter.CategoryDropdownAdapter.CategorySelectedListener
import com.microblocklabs.mpc.model.NetworkItem


class CategoryDropdownMenu(context: Context, networkList: List<NetworkItem>) : PopupWindow(context) {
    private var networkList: List<NetworkItem>
    private val context: Context
    private var rvCategory: RecyclerView? = null
    private var dropdownAdapter: CategoryDropdownAdapter? = null

    init {
        this.context = context
        this.networkList = networkList
        setupView()
    }

    fun setCategorySelectedListener(categorySelectedListener: CategorySelectedListener?) {
        dropdownAdapter!!.setCategorySelectedListener(categorySelectedListener)
    }

    private fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.popup_menu, null)
        rvCategory = view.findViewById<RecyclerView>(R.id.recycler_network)
        rvCategory!!.setHasFixedSize(true)
        rvCategory!!.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        rvCategory!!.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dropdownAdapter = CategoryDropdownAdapter(networkList)
        rvCategory!!.setAdapter(dropdownAdapter)
        contentView = view
    }
}