package com.microblocklabs.mpc.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.microblocklabs.mpc.fragment.ActivityFragment
import com.microblocklabs.mpc.fragment.NFTFragment
import com.microblocklabs.mpc.fragment.TokenFragment

class TokenViewPagerAdapter(fm: FragmentManager, var tabCount: Int) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TokenFragment()
            1 -> NFTFragment()
            2 -> ActivityFragment()
            else -> TokenFragment()
        }
    }

    override fun getCount(): Int {
        return tabCount
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Tokens"
            1 -> "NFTs"
            2 -> "Activity"
            else -> "Tokens"
        }
    }
}
