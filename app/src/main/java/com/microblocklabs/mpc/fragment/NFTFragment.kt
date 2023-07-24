package com.microblocklabs.mpc.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.FragmentNFTBinding
import com.microblocklabs.mpc.databinding.FragmentTokenBinding

class NFTFragment : Fragment() {
    private lateinit var binding: FragmentNFTBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNFTBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupData()
    }

    private fun setupData() {
        binding.noNft.text = getString(R.string.no_nft_found)
//        populateData()
//        if(tokenList.size>0){
//            binding.noToken.visibility = View.GONE
//            binding.recyclerToken.visibility = View.VISIBLE
//        }else{
//            binding.noToken.visibility = View.VISIBLE
//            binding.recyclerToken.visibility = View.GONE
//        }


    }

}