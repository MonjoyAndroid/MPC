package com.microblocklabs.mpc.interfaces

import android.content.Context
import aot.armsproject.utils.AppSharedPreferenceManager
import io.grpc.Channel

interface OnActivityDataReceivedListener {
    fun onActivityDataReceived(context: Context, senderAccount: String,
                       prefManager: AppSharedPreferenceManager, connectionChannel: Channel)
}