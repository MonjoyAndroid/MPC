package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityOtpverificationBinding
import com.microblocklabs.mpc.databinding.ActivityTransparentLockScreenBinding

class TransparentLockScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityTransparentLockScreenBinding
    lateinit var countdownTimer: CountDownTimer
    var startMiliSeconds = 60000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransparentLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lockScreenFor30Seconds()
    }


        private fun lockScreenFor30Seconds() {
        // Show a lock screen overlay or disable interactions
        // For example, you can use a DialogFragment or custom view

        // Start a 30-second countdown timer
            countdownTimer = object : CountDownTimer(startMiliSeconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update UI with remaining time if needed
                updateUI(millisUntilFinished)
            }

            override fun onFinish() {
                // Reset login attempts and dismiss the lock screen overlay
//                loginAttempts = 0
                resetTimer()
            }
        }.start()
    }

    private fun resetTimer() {
        // Cancel the timer if it's running
        countdownTimer.cancel()
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(timeInMiliSeconds: Long){
//        val minute = (timeInMiliSeconds / 1000) / 60
        val second = (timeInMiliSeconds / 1000) % 60
        binding.txtMsg.text = resources.getString(R.string.too_many_wrong_attempts)
        binding.tvLockTimer.text = "Wait time: $second seconds"
    }

    override fun onBackPressed() {
    }
}