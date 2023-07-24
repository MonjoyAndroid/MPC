package com.microblocklabs.mpc.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.ActivityAdapter
import com.microblocklabs.mpc.adapter.TokenAdapter
import com.microblocklabs.mpc.databinding.FragmentActivityBinding
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.model.TokenModel

class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    private lateinit var activityList: ArrayList<ActivityModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupData()
    }

    private fun setupData() {
        binding.noToken.text = getString(R.string.no_token_found)
        populateData()
        if(activityList.size>0){
            binding.noToken.visibility = View.GONE
            binding.recyclerToken.visibility = View.VISIBLE
        }else{
            binding.noToken.visibility = View.VISIBLE
            binding.recyclerToken.visibility = View.GONE
        }


    }

    private fun populateData() {
        activityList = ArrayList<ActivityModel>()
        activityList.add(ActivityModel(R.drawable.send_token_icon, "Send", "July 5, To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
        activityList.add(ActivityModel(R.drawable.send_token_icon, "Send", "July 5 To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
        activityList.add(ActivityModel(R.drawable.send_token_icon, "Send", "July 5 To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
        activityList.add(ActivityModel(R.drawable.send_token_icon, "Send", "July 5, To: $0xd92aDe2F40....09433....005e8A", "-10 CIFD", "-10 CIFD"))
        val adapter = ActivityAdapter(activityList)
        binding.recyclerToken.layoutManager = LinearLayoutManager(requireContext())
        // Setting the Adapter with the recyclerview
        binding.recyclerToken.adapter = adapter
    }

}