package com.microblocklabs.mpc.activity

import android.content.Intent
import android.os.Bundle
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivitySkipToHomeBinding

@Suppress("DEPRECATION")
class SkipToHomeActivity : BaseActivity() {
    private lateinit var binding: ActivitySkipToHomeBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkipToHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.imgArrowBack.setOnClickListener{
            onBackPressed()
        }
        binding.buttonLogin.setOnClickListener{
            navigateToLogin()
        }
        binding.buttonImport.setOnClickListener{
            navigateToImportWallet()
        }
        binding.buttonCreate.setOnClickListener{
            navigateToCreateWallet()
        }
    }


    private fun navigateToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }

    private fun navigateToCreateWallet(){
        val intent = Intent(this, CreateWalletActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }

    private fun navigateToImportWallet(){
        val intent = Intent(this, PhraseRecoveryActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("openPhraseFor", 3)
        startActivity(intent)
        finishAffinity()
    }
}