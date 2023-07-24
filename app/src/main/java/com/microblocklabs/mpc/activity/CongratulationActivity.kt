package com.microblocklabs.mpc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityCongratulationBinding
import com.microblocklabs.mpc.databinding.ActivityPhaseRecoveryBinding

class CongratulationActivity : BaseActivity() {
    private lateinit var binding: ActivityCongratulationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCongratulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonDone.setOnClickListener{
            navigateToLoginScreen()
        }
    }


    private fun navigateToLoginScreen(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }
}