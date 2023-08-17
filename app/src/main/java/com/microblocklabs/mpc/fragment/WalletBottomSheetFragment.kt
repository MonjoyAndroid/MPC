package com.microblocklabs.mpc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.AccountAdapter
import com.microblocklabs.mpc.adapter.ActivityAdapter
import com.microblocklabs.mpc.databinding.FragmentWalletBottomSheetBinding
import com.microblocklabs.mpc.model.AccountModel
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.utility.CommonUtils


class WalletBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentWalletBottomSheetBinding
    private lateinit var accountList: ArrayList<AccountModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
//        val touchOutsideView = dialog!!.window!!
//            .getDecorView()
//        touchOutsideView.setOnClickListener(null)
        setupScreen(view)
    }

    private fun setupScreen(view: View) {
        populateData()
        if(accountList.size>0){
            binding.layoutNoMoreAccount.visibility = View.GONE
            binding.layoutAllAccount.visibility = View.VISIBLE
        }else{
            binding.layoutNoMoreAccount.visibility = View.VISIBLE
            binding.layoutAllAccount.visibility = View.GONE
        }

        binding.imgCross.setOnClickListener {
            dismiss()
        }

        binding.buttonCreate.setOnClickListener {
            CommonUtils.showToastMessage(requireContext(), "Create wallet work is under process.")
        }
    }

    private fun populateData() {
        accountList = ArrayList<AccountModel>()
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5, To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5 To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5 To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5, To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5, To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5 To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5 To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
//        accountList.add(AccountModel(R.drawable.send_token_icon, "Send", "July 5, To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))

        val adapter = AccountAdapter(accountList)
        binding.recyclerAccount.layoutManager = LinearLayoutManager(requireContext())
        // Setting the Adapter with the recyclerview
        binding.recyclerAccount.adapter = adapter
    }

}