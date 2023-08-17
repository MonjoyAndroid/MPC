package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityWalletSetupBinding

class WalletSetupActivity : BaseActivity() {
    private lateinit var binding: ActivityWalletSetupBinding


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.descWalletSetup.text = resources.getString(R.string.wallet_setup_desc)
        binding.imgArrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.buttonCreate.setOnClickListener {
            navigateToCreateWallet()
        }

        binding.buttonImport.setOnClickListener {
            navigateToImportWallet()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    private fun navigateToCreateWallet(){
        startActivity(Intent(applicationContext, CreateWalletActivity::class.java))
    }

    private fun navigateToImportWallet(){
        startActivity(Intent(applicationContext, PhraseRecoveryActivity::class.java).apply {
            putExtra("openPhraseFor", 3)
        })
    }
}