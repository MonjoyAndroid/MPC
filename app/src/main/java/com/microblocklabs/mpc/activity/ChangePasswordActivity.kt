package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private var showInfoPop: PopupWindow?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgInfoPass.setOnClickListener {
            if(showInfoPop==null){
                showInfoPop = showPassSuggestionPopup()
                showInfoPop?.isOutsideTouchable = false
                showInfoPop?.isFocusable = false
//            showInfoPop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                showInfoPop?.showAsDropDown(binding.imgInfoPass, -760, 10)
                binding.imgInfoPass.background = resources.getDrawable(R.drawable.info_icon)
            }else{
                dismissPassSuggestionPopup()
            }
        }

        binding.tvShowPass.setOnClickListener {
            if (binding.etPassword.length() > 0)
                showHidePass("ShowPass")
        }

        binding.tvShowConfirmPass.setOnClickListener {
            if (binding.etPassword.length() > 0)
                showHidePass("ShowConfirmPass")
        }
    }

    private fun showPassSuggestionPopup(): PopupWindow {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.info_message_layout, null)
        view.findViewById<TextView>(R.id.tv_message).text = resources.getString(R.string.pass_suggestion_msg)
        return PopupWindow(view, 800, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun dismissPassSuggestionPopup() {
        showInfoPop?.let {
            if(it.isShowing){
                it.dismiss()
                binding.imgInfoPass.background = resources.getDrawable(R.drawable.info_icon_white)
            }
            showInfoPop = null
        }

    }

    private fun showHidePass(showHideFor: String) {
        when(showHideFor) {
            "ShowPass" -> {
                if (binding.tvShowPass.text == "Show") {
                    binding.tvShowPass.text = "Hide"
                    //Show Password
                    binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.tvShowPass.text = "Show"
                    //Hide Password
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etPassword.setSelection(binding.etPassword.length())
            }
            "ShowConfirmPass" -> {
                if (binding.tvShowConfirmPass.text == "Show") {
                    binding.tvShowConfirmPass.text = "Hide"
                    //Show Password
                    binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.tvShowConfirmPass.text = "Show"
                    //Hide Password
                    binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
            }
        }
    }
}