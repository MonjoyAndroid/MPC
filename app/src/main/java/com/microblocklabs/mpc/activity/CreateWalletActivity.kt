package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityCreateWalletBinding
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import signup.SignUpServiceGrpc
import signup.Signup

class CreateWalletActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateWalletBinding
    private var showInfoPop: PopupWindow?= null
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    var openPurposeVal = 0 //Create Wallet = 0, Regular Login = 1, Forgot Password = 2, Import Wallet = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]

        binding.imgArrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgInfoUniqueId.setOnClickListener {
            showInfoPop = showInfoPopup()
            showInfoPop?.isOutsideTouchable = true
            showInfoPop?.isFocusable = false
//            showInfoPop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showInfoPop?.showAsDropDown(binding.imgInfoUniqueId, -760, 10)
        }

        binding.imgInfoPass.setOnClickListener {
            showInfoPop = showPassSuggestionPopup()
            showInfoPop?.isOutsideTouchable = true
            showInfoPop?.isFocusable = false
//            showInfoPop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showInfoPop?.showAsDropDown(binding.imgInfoPass, -760, 10)
        }

        binding.imgShowHidePass.setOnClickListener {
            if (binding.etPassword.length() > 0)
                showHidePass("ShowPass")
        }

        binding.imgShowHideConfirmPass.setOnClickListener {
            if (binding.etConfirmPassword.length() > 0)
                showHidePass("ShowConfirmPass")
        }

        binding.buttonVerify.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val phoneWithoutCountryCode = binding.etMobile.text.toString()
            val uniqueID = binding.etUniqueId.text.toString()

            checkCredentialsValidation(email, phoneWithoutCountryCode, password, confirmPassword, uniqueID)
//            navigateToOTPVerificationScreen()
        }

        // set click event using setOnCountryChangeListener for countryName, countryCode, countryCodeName, countryFlagImage and more
        binding.countyCodePicker.setOnCountryChangeListener {

            val countryName = binding.countyCodePicker.selectedCountryName
            val countryCode = binding.countyCodePicker.selectedCountryCode
            val countryCodeName = binding.countyCodePicker.selectedCountryNameCode
            val countryCodeWithPlus = binding.countyCodePicker.selectedCountryCodeWithPlus
            val countryFlagImage = binding.countyCodePicker.selectedCountryFlagResourceId

            // set selected flag in image view
//            countryFlag.setImageResource(countryFlagImage)
//            Toast.makeText(this, "$countryName, $countryCode, $countryCodeName, $countryCodeWithPlus", Toast.LENGTH_SHORT).show()

        }

        binding.etEmail.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

        binding.etPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })
    }

    private fun showHidePass(showHideFor: String) {
        when(showHideFor) {
            "ShowPass" -> {
                if(binding.imgShowHidePass.contentDescription == "Show"){
                    //Show Password
                    binding.imgShowHidePass.contentDescription = "Hide"
                    binding.imgShowHidePass.setBackgroundResource(R.drawable.icon_show)
                    binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }else{
                    //Hide Password
                    binding.imgShowHidePass.contentDescription = "Show"
                    binding.imgShowHidePass.setBackgroundResource(R.drawable.icon_hide)
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etPassword.setSelection(binding.etPassword.length())
            }
            "ShowConfirmPass" -> {
                if (binding.imgShowHideConfirmPass.contentDescription == "Show") {
                    binding.imgShowHideConfirmPass.contentDescription = "Hide"
                    binding.imgShowHideConfirmPass.setBackgroundResource(R.drawable.icon_show)
                    binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.imgShowHideConfirmPass.contentDescription = "Show"
                    binding.imgShowHideConfirmPass.setBackgroundResource(R.drawable.icon_hide)
                    binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkCredentialsValidation(email: String, phoneWithoutCountryCode: String, password: String, confirmPassword: String, uniqueID: String){
        var phone = if(phoneWithoutCountryCode.isEmpty()){
            ""
        }else{
            binding.countyCodePicker.selectedCountryCodeWithPlus +phoneWithoutCountryCode
        }

        if(email.isEmpty() or password.isEmpty() or confirmPassword.isEmpty()){
            if(email.isEmpty() and password.isEmpty() and confirmPassword.isEmpty()){
                binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            }else{
                if(email.isEmpty() and password.isEmpty()){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                }else if(email.isEmpty() and confirmPassword.isEmpty()){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                }else if (password.isEmpty() and confirmPassword.isEmpty()){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                }else{
                    if (email.isEmpty()){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    }else if(password.isEmpty()){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    }else if(confirmPassword.isEmpty()){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    }
                }
            }
            return
        }else if(!CommonUtils.isEmailValid(email)){
            binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            CommonUtils.alertDialog(this, "Invalid Email id")
            return
        }
//        else if(phoneWithoutCountryCode.length<10){
//            binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
//            binding.rlMobileBox.background = resources.getDrawable(R.drawable.bg_border_red)
//            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
//            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
//            showWarningMessage("Invalid Mobile number")
//            return
//        }
        else if(!CommonUtils.isPasswordValidate(password)){
            binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            CommonUtils.alertDialog(this, "Password is not valid")
            return
        }else if(password != confirmPassword){
            binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            CommonUtils.alertDialog(this, "Password and Confirm Password are not matching")
            return
        }else{
            binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            if(NetworkUtils.isNetworkConnected(this)){
                requestForRegisterUser(email, phone, password, uniqueID)
            }else{
                CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
            }

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
                binding.imgInfoUniqueId.background = resources.getDrawable(R.drawable.info_icon_white)
            }
            showInfoPop = null
        }

    }

    private fun showInfoPopup(): PopupWindow {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.info_message_layout, null)
        view.findViewById<TextView>(R.id.tv_message).text = resources.getString(R.string.unique_id_suggestion_msg)
        return PopupWindow(view, 800, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun dismissShowInfoPopup() {
        showInfoPop?.let {
            if(it.isShowing){
                it.dismiss()
                binding.imgInfoPass.background = resources.getDrawable(R.drawable.info_icon_white)
                binding.imgInfoUniqueId.background = resources.getDrawable(R.drawable.info_icon_white)
            }
            showInfoPop = null
        }

    }

    private fun requestForRegisterUser(email: String, phone: String, password: String, uniqueID: String) {
        
        val signUpService = SignUpServiceGrpc.newBlockingStub(connectionChannel)

        val requestMessage = Signup.SignUpRequest.newBuilder()
            .setEmail(email)
            .setPhoneNumber(phone)
            .setPassword(password)
            .setUniqueId(uniqueID)
            .build()

        showLoadingDialog()

        Single.fromCallable { signUpService.signUpUser(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Signup.SignUpResponse> {
                override fun onSuccess(response: Signup.SignUpResponse) {
                    dismissLoadingDialog()
//                    Log.d("MyResponse", response.toString())
                    saveUserData(response)


                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@CreateWalletActivity, displayMsg)
                }
            })
    }

    private fun saveUserData(response : Signup.SignUpResponse) {
        val userProfile = UserProfile(response.email, response.emailVerified, response.phoneNumber, response.phoneNumberVerified, response.mnemonic)
        userProfileViewModel.insertUserProfile(userProfile)

        val walletList = response.walletList
        for (i in 0 until walletList.size){
            val walletDetails = WalletDetails(
                walletList[i].ethereumAddress,
                response.email,
                walletList[i].accountCount,
                walletList[i].accountName,
                walletList[i].publickey,
                walletList[i].twoPercent,
                walletList[i].restValue,
                walletList[i].perMonth,
                walletList[i].cifd,
                walletList[i].uniqueId,
                walletList[i].ifUniqueId
            )

            val sharedPart = walletList[i].sharepart
            val sharePartDetails = SharePartDetails(walletList[i].ethereumAddress, sharedPart.x, sharedPart.y)
            walletDetailsViewModel.insertWalletDetail(walletDetails)
            sharePartViewModel.insertSharedPart(sharePartDetails)
        }

        navigateToOTPVerificationScreen()
    }

    private fun navigateToOTPVerificationScreen() {
        startActivity(Intent(this, OTPVerificationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("OTPVerificationOpenFor", 0)
        })
        finishAffinity()
    }
}