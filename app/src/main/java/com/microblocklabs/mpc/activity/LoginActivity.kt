package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityLoginBinding
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.Constant
import com.microblocklabs.mpc.utility.NetworkUtils
import io.grpc.CallOptions
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import login.Login
import login.Login.LoginRequest
import login.LoginServiceGrpc


class LoginActivity : BaseActivity(), IAlertDialogButtonClickListener {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    private var userProfile: List<UserProfile>? = null
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iAlertDialogButtonClickListener = this
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]
        fetchDataFromDatabase()
        binding.imgShowHidePass.setOnClickListener {
            if (binding.etPassword.length() > 0)
                showHidePass()
        }

        binding.tvForgotPass.setOnClickListener{
            navigateToPhraseScreen()
        }

        binding.buttonUnlock.setOnClickListener{
            if(NetworkUtils.isNetworkConnected(this)){
                requestForLogin(userProfile!![0].email, binding.etPassword.text.toString())
            }else{
                CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
            }
        }


    }

    private fun fetchDataFromDatabase(){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
    }

    @SuppressLint("SetTextI18n")
    fun showHidePass() {
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


    private fun requestForLogin(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            CommonUtils.alertDialog(this, resources.getString(R.string.enter_password))
//            showWarningMessage(resources.getString(R.string.enter_password))
            return
        }

        val loginService = LoginServiceGrpc.newBlockingStub(connectionChannelWithInterceptorForLogin)
        val metaDataKey = CallOptions.Key.create<String>("my_key")
        val requestMessage = LoginRequest.newBuilder()
            .setEmail(email)
            .setPassword(password)
            .build()

        showLoadingDialog()

        Single.fromCallable { loginService.login(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Login.LoginResponse> {
                override fun onSuccess(response: Login.LoginResponse) {
                    dismissLoadingDialog()
                    if (response.success) {
                        saveLoginData(response)
                    } else {
                        if(response.error.contains("No User Found!!")){
                            val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                            CommonUtils.functionalAlertDialog(this@LoginActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                        }else{
                            CommonUtils.alertDialog(this@LoginActivity, response.error)
                        }

                    }

                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    if(displayMsg.contains("Too Many Requests")){
                        callLockScreen()
                    }else if(displayMsg.contains("No User Found!!")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(this@LoginActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        CommonUtils.alertDialog(this@LoginActivity, displayMsg)
                    }

                }
            })
    }

    private fun callLockScreen(){
        startActivity(Intent(this, TransparentLockScreenActivity::class.java))
    }


    private fun saveLoginData(response: Login.LoginResponse){
        mpcSharedPref.save(Constant.UserID, response.userId)
        mpcSharedPref.save(Constant.SessionToken, response.token)
        startHomeActivity()
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeScreenActivity::class.java))
        finishAffinity()
    }

    private fun navigateToPhraseScreen(){
        startActivity(Intent(applicationContext, PhraseRecoveryActivity::class.java).apply {
            putExtra("openPhraseFor", 2)
        })
    }


    private fun deleteAllDataFromLocal() {
        userProfileViewModel.deleteUserProfile()
        walletDetailsViewModel.deleteWalletDetail()
        sharePartViewModel.deleteSharedPart()
        navigateToSplashScreen()
    }

    private fun navigateToSplashScreen(){
        startActivity(Intent(applicationContext, SplashScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    override fun onPositiveButtonClick(callingPurpose: String?) {
        when (callingPurpose) {

            "alreadyDeletedWallet" -> deleteAllDataFromLocal()
        }
    }


}