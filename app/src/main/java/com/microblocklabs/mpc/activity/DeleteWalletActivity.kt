package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.ViewModelProvider
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityDeleteWalletBinding
import com.microblocklabs.mpc.databinding.ActivityPhaseRecoveryBinding
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
import deleteWallet.DeleteWallet
import deleteWallet.DeleteWalletServiceGrpc
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ClientInterceptors
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.security.PrivateKey

class DeleteWalletActivity : BaseActivity() , IAlertDialogButtonClickListener{
    private lateinit var binding: ActivityDeleteWalletBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    private var userProfile: List<UserProfile>? = null
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteWalletBinding.inflate(layoutInflater)
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

        binding.buttonDelete.setOnClickListener{
            if(NetworkUtils.isNetworkConnected(this)){
//                navigateToPhraseScreen("JoySreeR@m")
                requestForGetPrivateKey(userProfile!![0].email, binding.etPassword.text.toString())
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

    private fun requestForGetPrivateKey(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            CommonUtils.alertDialog(this, resources.getString(R.string.enter_password))
            return
        }

        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcSharedPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val deleteWalletService = DeleteWalletServiceGrpc.newBlockingStub(channelWithHeader)

        val requestMessage = DeleteWallet.DeleteWalletConfirmRequest.newBuilder()
            .setEmail(email)
            .setPassword(password)
            .build()

        showLoadingDialog()

        Single.fromCallable { deleteWalletService.deleteWalletConfirm(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<DeleteWallet.DeleteWalletConfirmResponse> {
                override fun onSuccess(response: DeleteWallet.DeleteWalletConfirmResponse) {
                    dismissLoadingDialog()
                    if (response.success) {
                        navigateToPhraseScreen(response, password)
                    } else {
                        showErrorMessage(response.error)
                    }
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg = if (e.message.toString().contains(":")) {
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    } else {
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    if(displayMsg.contains("Unauthorized")){
                        CommonUtils.functionalAlertDialog(this@DeleteWalletActivity, "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(this@DeleteWalletActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        showErrorMessage(displayMsg)
                    }

                }
            })
    }


    private fun navigateToPhraseScreen(response: DeleteWallet.DeleteWalletConfirmResponse, password: String){
        val privateKey = response.privatekey
        startActivity(Intent(applicationContext, PhraseRecoveryActivity::class.java).apply {
            putExtra("openPhraseFor", 4)
            putExtra("privateKey", privateKey)
            putExtra("password", password)
        })
        finish()
    }

    private fun callLogout(logoutVal: Int) {
        if(logoutVal == 0){
            mpcSharedPref.clear()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finishAffinity()
        }else if(logoutVal == 1){
            deleteAllDataFromLocal()
        }

    }

    override fun onPositiveButtonClick(callingPurpose: String?) {
        when (callingPurpose) {
            "unAuthorizedToken" -> callLogout(0)

            "alreadyDeletedWallet" -> callLogout(1)
        }
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

}