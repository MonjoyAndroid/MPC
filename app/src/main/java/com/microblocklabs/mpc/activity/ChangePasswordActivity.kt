package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import cognito.Cognito
import cognito.CognitoServiceGrpc
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityChangePasswordBinding
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.utility.CommonUtils
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ChangePasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private var showInfoPop: PopupWindow?= null
    private var userProfile: List<UserProfile>? = null

    var startMiliSeconds = 60000L * 3
    lateinit var countdownTimer: CountDownTimer
    var isRunning: Boolean = false;
    var timeInMiliSeconds = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfile = db.mUserProfileDao()!!.getUserProfile()
        setOtpTimer()

        if(userProfile.isNullOrEmpty()){
            binding.etEmail.setText("")
        }else{
            if(userProfile!!.size>0 && userProfile!![0].email.isNotEmpty()) {
                binding.etEmail.setText(userProfile!![0].email)
            }else{
                binding.etEmail.setText("")
            }
        }

        binding.tvSendOtp.setOnClickListener{
            if(isRunning){
                resetOtpTimer()
            }else{
                setOtpTimer()
            }
            requestForSendOTP(binding.etEmail.text.toString())
        }

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
            val emailOTP = binding.etEmailOtp.text.toString()
            checkCredentialsValidation(email, emailOTP, password, confirmPassword)
//            setNavigationValueForNextScreen(true)
        }
        showWarningMessage(resources.getString(R.string.otp_send_msg))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkCredentialsValidation(email: String, emailOtp: String, password: String, confirmPassword: String){

        if(emailOtp.isEmpty() or password.isEmpty() or confirmPassword.isEmpty()){
            if(emailOtp.isEmpty() and password.isEmpty() and confirmPassword.isEmpty()){
                binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            }else{
                if(emailOtp.isEmpty() and password.isEmpty()){
                    binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                }else if(emailOtp.isEmpty() and confirmPassword.isEmpty()){
                    binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                }else if (password.isEmpty() and confirmPassword.isEmpty()){
                    binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                }else{
                    if (emailOtp.isEmpty()){
                        binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    }else if(password.isEmpty()){
                        binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    }else if(confirmPassword.isEmpty()){
                        binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    }
                }
            }
            return
        }else if(!CommonUtils.isPasswordValidate(password)){
            binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            showWarningMessage("Password is not valid")
            return
        }else if(password != confirmPassword){
            binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            showWarningMessage("Password and Confirm Password are not matching")
            return
        }else{
            binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
//            showMessage("Success fully Done")
            requestForChangePassword(email, emailOtp, password)
        }
    }

    private fun setOtpTimer(){
        countdownTimer = object : CountDownTimer(startMiliSeconds, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                timeInMiliSeconds = millisUntilFinished
                updateTimerUI()
            }

            override fun onFinish() {
                countdownTimer.cancel()
                isRunning = false
                showMessage(resources.getString(R.string.otp_expired_msg))
            }
        }
        countdownTimer.start()
        isRunning = true
    }

    private fun resetOtpTimer(){
        timeInMiliSeconds = startMiliSeconds
        countdownTimer.cancel()
        isRunning = false
        setOtpTimer()
        updateTimerUI()
    }

    private fun updateTimerUI() {
        val minute = (timeInMiliSeconds / 1000) / 60
        val seconds = (timeInMiliSeconds / 1000) % 60
        binding.tvOtpTimer.text = "${resources.getString(R.string.otp_timer)} $minute:$seconds"
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
                if(binding.imgShowHidePass.contentDescription == "Show"){
                    //Show Password
                    binding.imgShowHidePass.contentDescription = "Hide"
                    binding.imgShowHidePass.setBackgroundResource(R.drawable.icon_hide)
                    binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }else{
                    //Hide Password
                    binding.imgShowHidePass.contentDescription = "Show"
                    binding.imgShowHidePass.setBackgroundResource(R.drawable.icon_show)
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etPassword.setSelection(binding.etPassword.length())
            }
            "ShowConfirmPass" -> {
                if (binding.imgShowHideConfirmPass.contentDescription == "Show") {
                    binding.imgShowHideConfirmPass.contentDescription = "Hide"
                    binding.imgShowHideConfirmPass.setBackgroundResource(R.drawable.icon_hide)
                    binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.imgShowHideConfirmPass.contentDescription = "Show"
                    binding.imgShowHideConfirmPass.setBackgroundResource(R.drawable.icon_show)
                    binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer.cancel()
        isRunning = false
    }

    private fun requestForSendOTP(email: String) {
        val cognitoService = CognitoServiceGrpc.newBlockingStub(connectionChannel)
        val requestMessage = Cognito.ForgotPasswordRequest.newBuilder()
            .setEmail(email)
            .build()

        showLoadingDialog()

        Single.fromCallable { cognitoService.forgotPassword(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Cognito.ForgotPasswordResponse> {
                override fun onSuccess(response: Cognito.ForgotPasswordResponse) {
                    dismissLoadingDialog()
                    showMessage(response.message)
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    showErrorMessage(displayMsg)
                }
            })
    }

    private fun requestForChangePassword(email: String, emailOtp: String, password: String) {
        if(!isRunning){
            showWarningMessage(resources.getString(R.string.otp_expired_msg))
            return
        }

        val cognitoService = CognitoServiceGrpc.newBlockingStub(connectionChannel)
        val requestMessage = Cognito.ConfirmForgotPasswordRequest.newBuilder()
            .setEmail(email)
            .setNewPassword(password)
            .setVerificationCode(emailOtp)
            .build()

        showLoadingDialog()

        Single.fromCallable { cognitoService.confirmForgotPassword(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Cognito.ConfirmForgotPasswordResponse> {
                override fun onSuccess(response: Cognito.ConfirmForgotPasswordResponse) {
                    dismissLoadingDialog()
                    showMessage(response.message)
                    showLoginScreen()
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    showErrorMessage(displayMsg)
                }
            })
    }

    private fun showLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finishAffinity()
    }
}