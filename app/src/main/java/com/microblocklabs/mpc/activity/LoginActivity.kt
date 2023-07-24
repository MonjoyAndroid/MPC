package com.microblocklabs.mpc.activity

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.databinding.ActivityLoginBinding
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.utility.Constant
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import login.Login
import login.Login.LoginRequest
import login.LoginServiceGrpc


class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private var userProfile: List<UserProfile>? = null
    private var openFor = 0 //Create Wallet = 0, Regular Login = 1, Forgot Password = 2, Import Wallet = 3


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        fetchDataFromDatabase()
        binding.tvShowPass.setOnClickListener {
            if (binding.etPassword.length() > 0)
                showHidePass()
        }

        binding.tvForgotPass.setOnClickListener{
            showMessage("Work is under process")
        }

        binding.buttonUnlock.setOnClickListener{
            requestForLogin(userProfile!![0].email, binding.etPassword.text.toString())
        }


    }

    private fun fetchDataFromDatabase(){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
    }

    @SuppressLint("SetTextI18n")
    fun showHidePass() {
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

    private fun requestForLogin(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            showWarningMessage(resources.getString(com.microblocklabs.mpc.R.string.enter_password))
            return
        }

        val loginService = LoginServiceGrpc.newBlockingStub(connectionChannel)

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
                        showErrorMessage(response.error)
                    }

                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    dismissLoadingDialog()
                    showErrorMessage("Error:  ${e.message}")
                }
            })
    }

    private fun saveLoginData(response: Login.LoginResponse){
        mpcSharedPref.save(Constant.UserID, response.userId)
        startHomeActivity()
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeScreenActivity::class.java))
        finishAffinity()
    }
//
//    private fun startSignUpActivity() {
//        startActivity(Intent(this, SignUpActivity::class.java))
//    }
//
//    private fun getDataFromServer() {
//        val channel = OkHttpChannelBuilder.forAddress("192.168.0.102", 8080)
//            .usePlaintext()
//            .build()
//        val stub = HelloServiceGrpc.newBlockingStub(channel)
//
//        val requestMessage = HelloRequest.newBuilder()
//            .setName("Monjoy")
//            .setAge(34)
////            .setSentiment(Sentiment.HAPPY)
//            .build()
//
//        Single.fromCallable { stub.greet(requestMessage) }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : SingleObserver<HelloResponse> {
//                override fun onSuccess(t: HelloResponse) {
//
//                }
//
//                override fun onSubscribe(d: Disposable) {
//                    Log.d("Monjoy", "on      ====/        subscribe")
//                }
//
//                override fun onError(e: Throwable) {
//
//                }
//            })
//    }
}