package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityWalletSetupBinding

class WalletSetupActivity : BaseActivity() {
    private lateinit var binding: ActivityWalletSetupBinding
    private var showInfoPop: PopupWindow?= null

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

        binding.buttonRecoverUser.setOnClickListener {
            navigateToRecoverWallet()
        }

        binding.imgInfoRecoverWallet.setOnClickListener {
            showInfoPop = showRecoverWalletSuggestionPopup()
            showInfoPop?.isOutsideTouchable = true
            showInfoPop?.isFocusable = false
//            showInfoPop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showInfoPop?.showAsDropDown(binding.imgInfoRecoverWallet, -760, 10)
        }
    }

    private fun showRecoverWalletSuggestionPopup(): PopupWindow {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.info_message_layout, null)
        view.findViewById<TextView>(R.id.tv_message).text = resources.getString(R.string.recover_wallet_tooltips_msg)
        return PopupWindow(view, 800, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    private fun navigateToRecoverWallet() {
        startActivity(Intent(applicationContext, AccountRecoveryActivity::class.java))
    }
}