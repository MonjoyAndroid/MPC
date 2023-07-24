package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.google.android.material.button.MaterialButton
import com.microblocklabs.mpc.R

class WalletSetupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_setup)

//        findViewById<TextView>(R.id.textSkip).setOnClickListener {
//            navigateToHomeScreen()
//        }

        findViewById<MaterialButton>(R.id.buttonCreate).setOnClickListener {
            navigateToCreateWallet()
        }

        findViewById<MaterialButton>(R.id.buttonImport).setOnClickListener {
//            showMessage("Work is under process")
            navigateToImportWallet()
        }
    }

    private fun navigateToHomeScreen(){
        startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
        finish()
    }

    private fun navigateToCreateWallet(){
        startActivity(Intent(applicationContext, CreateWalletActivity::class.java))
//        finish()
    }

    private fun navigateToImportWallet(){
        startActivity(Intent(applicationContext, PhraseRecoveryActivity::class.java).apply {
            putExtra("openPhraseFor", 3)
        })
//        finish()
    }
}