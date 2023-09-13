package com.microblocklabs.mpc.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityCongratulationBinding

class CongratulationActivity : BaseActivity() {
    var openPurposeVal = 0 //Create Wallet = 0, Recover Wallet = 5
    private lateinit var binding: ActivityCongratulationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCongratulationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        openPurposeVal = intent.getIntExtra("openWelcomeScreenFor", 0)
        setWelcomeScreen()

        binding.buttonDone.setOnClickListener{
            navigateToLoginScreen()
        }
    }

    private fun setWelcomeScreen(){
        when(openPurposeVal) {
            0 -> showCongratulationScreen()
            5 -> showWelcomeScreen()
        }

    }

    private fun showWelcomeScreen() {
        binding.titleCongratulation.text = getString(R.string.welcome_back_title)
        binding.descCongratulation.text = getString(R.string.welcome_back_desc)
    }

    private fun showCongratulationScreen() {
        binding.titleCongratulation.text = setCongratulationTitle()
        binding.descCongratulation.text = getString(R.string.congratulation_desc)
    }


    private fun setCongratulationTitle(): SpannableStringBuilder {
        val congratsText = getString(R.string.congratulation_title)

        val spannable = SpannableStringBuilder(congratsText)

        // Apply larger text size to "Congratulations !"
        spannable.setSpan(
            RelativeSizeSpan(1.2f), // Increase the text size by 20%
            0, 16, // Start and end indices for "Congratulations !"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply smaller text size to "Your Crypto Wallet is Ready !"
        spannable.setSpan(
            RelativeSizeSpan(0.8f), // Decrease the text size by 20%
            17, congratsText.length, // Start and end indices for "Your Crypto Wallet is Ready !"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }


    private fun navigateToLoginScreen(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }
}