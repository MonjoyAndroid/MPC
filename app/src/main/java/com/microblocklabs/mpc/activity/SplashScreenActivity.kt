package com.microblocklabs.mpc.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivitySplashScreenBinding
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import resendVerify.ResendVerify
import resendVerify.ResendVerifyServiceGrpc

class SplashScreenActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private var userProfile: List<UserProfile>? = null
    private var walletList: List<WalletDetails>? = null

    private var shouldNavigate = true
    private val delayMillis = 3000L // 3 seconds

    private val navigateRunnable = Runnable {
        if (shouldNavigate) {
            checkingForNextScreen()
            // Animate the loading of new activity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            // Close this activity
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_animation)
        binding.splashImage.startAnimation(slideAnimation)

        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        fetchDataFromDatabase()

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        Handler(Looper.getMainLooper()).postDelayed(navigateRunnable, delayMillis)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        shouldNavigate = false
        super.onBackPressed()
    }

    override fun onDestroy() {
        Handler(Looper.getMainLooper()).removeCallbacks(navigateRunnable)
        super.onDestroy()
    }

    private fun fetchDataFromDatabase(){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
        walletList = db.mWalletDetailsDao()!!.getWalletList()
    }


    private fun checkingForNextScreen() {
        if(userProfile.isNullOrEmpty()){
            val intent = Intent(this, OnboardActivity::class.java)
            startActivity(intent)
        }else{
            if(userProfile!![0].emailVerified){
                if(userProfile!![0].mnemonic.isEmpty()){
                    callSecretPhraseScreen()
                }else{
                    callLoginScreen()
                }
            }else{
                requestForSendOTP(userProfile!![0].email)
                val otpVerificationOpenFor = if(userProfile!![0].mnemonic.isEmpty()){
                    5 // OTP verification screen open for Recover wallet
                }else{
                    0 // OTP verification screen open for Create new wallet
                }
                callOTPVerificationScreen(otpVerificationOpenFor)
            }
        }
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

    private fun callOTPVerificationScreen(otpVerificationCallFor: Int) {
        val intent = Intent(this@SplashScreenActivity, OTPVerificationActivity::class.java).apply {
            putExtra("OTPVerificationOpenFor", otpVerificationCallFor)
        }
        startActivity(intent)
    }

    private fun callSecretPhraseScreen() {
        val intent = Intent(this@SplashScreenActivity, PhraseRecoveryActivity::class.java).apply {
            putExtra("openPhraseFor", 5)
        }
        startActivity(intent)
    }

    private fun callLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}