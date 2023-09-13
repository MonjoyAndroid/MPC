package com.microblocklabs.mpc.utility

import android.os.Handler
import com.microblocklabs.mpc.interfaces.OnInactivityListener

class InactivityTimer (private val timeout: Long, private val onInactivityListener: OnInactivityListener)  {

    private val handler = Handler()

    private val inactivityRunnable = Runnable {
        onInactivityListener.onInactive()
    }

    fun start() {
        handler.removeCallbacks(inactivityRunnable)
        handler.postDelayed(inactivityRunnable, timeout)
    }

    fun stop() {
        handler.removeCallbacks(inactivityRunnable)
    }

}