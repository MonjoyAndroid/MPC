package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.databinding.ActivitySplashBinding
import com.microblocklabs.mpc.room.MPCDatabase
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

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private var userProfile: List<UserProfile>? = null
    private var walletList: List<WalletDetails>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        fetchDataFromDatabase()
//        addDemoData()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            checkingForNextScreen()
            // Animate the loading of new activity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            // Close this activity
            finish()

        }, 3000)
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
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }else{
                requestForSendOTP(userProfile!![0].email)
                callOTPVerificationScreen()
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
                    dismissLoadingDialog()
                    showErrorMessage("Error:  ${e.message}")
                }
            })
    }

    private fun callOTPVerificationScreen() {
        val intent = Intent(this@SplashActivity, OTPVerificationActivity::class.java).apply {
            putExtra("OTPVerificationOpenFor", 0)
        }
        startActivity(intent)
    }

    private fun addDemoData() {
//        val userProfile = UserProfile("1111", true, "abcd@gmail.com", "9089765431", "asd asdf asdfg werr n")
//        userProfileViewModel.insertUserProfile(userProfile)

//        val walletList = response.walletList
//        for (i in 0 until walletList.size){
//            val walletDetails = WalletDetails(
//                walletList[i].ethereumAddress,
//                response.userid,
//                walletList[i].accountCount,
//                walletList[i].accountName,
//                walletList[i].publickey,
//                walletList[i].sharepart
//            )
//
//            walletDetailsViewModel.insertWalletDetail(walletDetails)
//        }

    }
}