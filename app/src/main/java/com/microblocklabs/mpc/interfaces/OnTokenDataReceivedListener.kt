package com.microblocklabs.mpc.interfaces

import android.content.Context
import com.microblocklabs.mpc.model.BalanceDetails


interface OnTokenDataReceivedListener {

    fun onTokenDataReceived(context: Context, balanceDetails: BalanceDetails)
}