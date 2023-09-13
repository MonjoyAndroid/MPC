package com.microblocklabs.mpc.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import aot.armsproject.utils.AppSharedPreferenceManager
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.FragmentFullscreenSupportBinding
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.Constant
import io.grpc.ManagedChannel


class FullscreenSupportFragment() : DialogFragment() {
    private lateinit var binding: FragmentFullscreenSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFullscreenSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgArrowBack.setOnClickListener {
            dismiss()
        }

        binding.tvEmailId.setOnClickListener {
            sendEmail(binding.tvEmailId.text.toString())
        }

        binding.urlTermsConditions.setOnClickListener {

            openWebsite(Constant.termsConditionsUrl)
        }

        binding.urlPrivacyPolicy.setOnClickListener {
            openWebsite(Constant.privacyPolicyUrl)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openWebsite(url: String){
        // Create an Intent to open a web browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setDataAndType(Uri.parse(url), "application/pdf")
        startActivity(intent)
    }


    private fun sendEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$emailAddress")
        startActivity(intent)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme){
            @Suppress("DEPRECATION")
            override fun onBackPressed() {
                super.onBackPressed()
                dismiss()
            }

        }
    }

}