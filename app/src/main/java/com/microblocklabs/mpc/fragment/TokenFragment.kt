package com.microblocklabs.mpc.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.TokenAdapter
import com.microblocklabs.mpc.databinding.ActivityHomeScreenBinding
import com.microblocklabs.mpc.databinding.FragmentTokenBinding
import com.microblocklabs.mpc.model.TokenModel

class TokenFragment : Fragment() {
    private lateinit var binding: FragmentTokenBinding
    private lateinit var tokenList: ArrayList<TokenModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTokenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupData()
    }

    private fun setupData() {
        binding.noToken.text = getString(R.string.no_token_found)
        populateData()
        if(tokenList.size>0){
            binding.noToken.visibility = View.GONE
            binding.recyclerToken.visibility = View.VISIBLE
        }else{
            binding.noToken.visibility = View.VISIBLE
            binding.recyclerToken.visibility = View.GONE
        }


    }

    private fun populateData() {
        tokenList = ArrayList<TokenModel>()
        tokenList.add(TokenModel(R.drawable.cifdaq_green, "CIFD TOKEN", "0.267", "$27650.30", "$3226.90", "+3.45%"))
        tokenList.add(TokenModel(R.drawable.bitcoin, "Bitcoin", "0.267", "$27650.30", "$3226.90", "+3.45%"))
        tokenList.add(TokenModel(R.drawable.ethereum1, "Ethereum", "0.267", "$27650.30", "$3226.90", "+3.45%"))
        tokenList.add(TokenModel(R.drawable.binance, "Binance Coin", "0.267", "$27650.30", "$3226.90", "+3.45%"))

        val adapter = TokenAdapter(tokenList)
        binding.recyclerToken.layoutManager = LinearLayoutManager(requireContext())
        // Setting the Adapter with the recyclerview
        binding.recyclerToken.adapter = adapter
    }


}