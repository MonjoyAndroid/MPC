package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityOtpverificationBinding
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
import confirmUser.ConfirmUser
import confirmUser.ConfirmUserServiceGrpc
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import resendVerify.ResendVerify
import resendVerify.ResendVerifyServiceGrpc


class OTPVerificationActivity : BaseActivity() {
    private lateinit var binding: ActivityOtpverificationBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private var userProfile: List<UserProfile>? = null
    var openPurposeVal = 0 //Create Wallet = 0, Regular Login = 1, Forgot Password = 2, Import Wallet = 3, Recover Wallet = 5

    var startMiliSeconds = 60000L * 3
    lateinit var countdownTimer: CountDownTimer
    var isRunning: Boolean = false;
    var timeInMiliSeconds = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        userProfile = db.mUserProfileDao()!!.getUserProfile()
        setOtpTimer()
        openPurposeVal = intent.getIntExtra("OTPVerificationOpenFor", 0)
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
            if(NetworkUtils.isNetworkConnected(this)){
                requestForSendOTP(binding.etEmail.text.toString())
            }else{
                CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
            }
        }

        binding.buttonVerify.setOnClickListener{
            if(NetworkUtils.isNetworkConnected(this)){
                requestForVerifyOTP(binding.etEmail.text.toString(), binding.etEmailOtp.text.toString()) //For Email otp
//                requestForVerifyOTP(binding.etMobile.text.toString(), binding.etMobOtp.text.toString()) //For mobile otp
            }else{
                CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
            }

//            setNavigationValueForNextScreen(true)
        }

        CommonUtils.alertDialog(this, resources.getString(R.string.otp_send_msg))

        binding.etEmailOtp.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etEmailOtp.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })
    }


    private fun setNavigationValueForNextScreen(isVerified: Boolean){

        if(isVerified){
            when(openPurposeVal) {
                0 -> navigateToPhraseScreen(openPurposeVal)
                1 -> navigateToHomeScreen()
                2 -> navigateToPhraseScreen(openPurposeVal)
                5 -> navigateToPhraseScreen(openPurposeVal)
            }
        }else{
            binding.etEmailOtp.background = ContextCompat.getDrawable(applicationContext, R.drawable.bg_border_red)
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
                binding.tvOtpTimer.visibility = View.GONE
                CommonUtils.alertDialog(this@OTPVerificationActivity, resources.getString(R.string.otp_expired_msg))
            }
        }
        countdownTimer.start()
        isRunning = true
        binding.tvOtpTimer.visibility = View.VISIBLE
    }

    private fun resetOtpTimer(){
        timeInMiliSeconds = startMiliSeconds
        countdownTimer.cancel()
        isRunning = false
        binding.tvOtpTimer.visibility = View.GONE
        setOtpTimer()
        updateTimerUI()
    }

    private fun updateTimerUI() {
        val minute = (timeInMiliSeconds / 1000) / 60
        val seconds = (timeInMiliSeconds / 1000) % 60
        binding.tvOtpTimer.text = "${resources.getString(R.string.otp_timer)} $minute:$seconds"
    }


    override fun onDestroy() {
        super.onDestroy()
        countdownTimer.cancel()
        isRunning = false
        binding.tvOtpTimer.visibility = View.GONE
    }

    private fun requestForSendOTP(email: String) {
        val resendVerifyService = ResendVerifyServiceGrpc.newBlockingStub(connectionChannel)
        val requestMessage = ResendVerify.VerificationRequest.newBuilder()
            .setEmail(email)
            .build()

//        showLoadingDialog()

        Single.fromCallable { resendVerifyService.resendVerify(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<ResendVerify.VerificationResponse> {
                override fun onSuccess(response: ResendVerify.VerificationResponse) {
                    if(isRunning){
                        resetOtpTimer()
                    }else{
                        setOtpTimer()
                    }
                    CommonUtils.alertDialog(this@OTPVerificationActivity, resources.getString(R.string.otp_send_msg))
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@OTPVerificationActivity, displayMsg)
                }
            })
    }

    private fun requestForVerifyOTP(email: String, otp: String) {
        if (email.isEmpty() or otp.isEmpty()) {
            binding.etEmailOtp.background = ContextCompat.getDrawable(applicationContext, R.drawable.bg_border_red)
            CommonUtils.alertDialog(this, resources.getString(R.string.please_enter_otp))
            return
        }

        if(!isRunning){
            CommonUtils.alertDialog(this, resources.getString(R.string.otp_expired_msg))
            return
        }

        val confirmUsrService = ConfirmUserServiceGrpc.newBlockingStub(connectionChannel)

        val requestMessage = ConfirmUser.ConfirmUserRequest.newBuilder()
            .setEmail(email)
            .setOtp(otp)
            .build()

        showLoadingDialog()

        Single.fromCallable { confirmUsrService.confirmUser(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<ConfirmUser.ConfirmUserResponse> {
                override fun onSuccess(response: ConfirmUser.ConfirmUserResponse) {
                    dismissLoadingDialog()
                    val isVerified = response.message.equals("OTP verified successfully")

                    if (openPurposeVal == 0 || openPurposeVal == 5) {
                        userProfileViewModel.updateUserConfirmation(userProfile!![0].email, true)
                    }
                    setNavigationValueForNextScreen(isVerified)

                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@OTPVerificationActivity, displayMsg)
                    setNavigationValueForNextScreen(false)
                }
            })
    }

    private fun navigateToHomeScreen() {
        startActivity(Intent(this, HomeScreenActivity::class.java))
        finish()
    }

    private fun navigateToPhraseScreen(openPurposeVal: Int) {
        startActivity(Intent(this, PhraseRecoveryActivity::class.java).apply {
            putExtra("openPhraseFor", openPurposeVal)
        })
        finish()
    }
}