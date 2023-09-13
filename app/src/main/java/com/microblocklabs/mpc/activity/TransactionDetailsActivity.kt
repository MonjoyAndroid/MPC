package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.ActivityAdapter
import com.microblocklabs.mpc.databinding.ActivityLoginBinding
import com.microblocklabs.mpc.databinding.ActivityTransactionDetailsBinding
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ClientInterceptors
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import listTransaction.ListTransaction
import listTransaction.TransactionServiceGrpc
import org.web3j.utils.Convert

class TransactionDetailsActivity : BaseActivity(), IAlertDialogButtonClickListener {

    private lateinit var binding: ActivityTransactionDetailsBinding
    private lateinit var activityList: ArrayList<ActivityModel>
    private lateinit var adapter: ActivityAdapter
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    private var senderAccountAddress = ""
    private val limitCount = 100
    private val skipCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        senderAccountAddress = intent.getStringExtra("selectedWalletAddress")!!
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]

        setRecyclerView()

        showTransactionHistory(limitCount, skipCount, senderAccountAddress)

        binding.imgArrowBack.setOnClickListener {
            finish()
        }

    }

    private fun setRecyclerView() {
        activityList = ArrayList<ActivityModel>()
        adapter = ActivityAdapter(this,null, activityList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = false
        binding.recyclerActivity.layoutManager = layoutManager
        // Setting the Adapter with the recyclerview
        binding.recyclerActivity.adapter = adapter
        binding.recyclerActivity.scrollToPosition(0)


//        binding.recyclerActivity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                val totalItemCount = adapter.itemCount
//                if (totalItemCount - lastVisibleItemPosition <= 3) { // Load more data when user is near the end
//                    showMessage("call for next pagination")
////                    showTransactionHistory(limitCount, skipCount, senderAccountAddress)
//                }
//            }
//        })
    }


    private fun showTransactionHistory(limit: Int, skip: Int, senderAccount: String){
        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcSharedPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val transactionListService = TransactionServiceGrpc.newBlockingStub(channelWithHeader)
        val requestMessage = ListTransaction.ListTransactionRequest.newBuilder()
            .setLimit(limit.toString())
            .setSkip(skip.toString())
            .setMyAddress(senderAccount)
            .build()

        showLoadingDialog()

        Single.fromCallable { transactionListService.listTransaction(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<ListTransaction.ListTransactionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: ListTransaction.ListTransactionResponse) {
                    dismissLoadingDialog()
                    populateData(response)
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    if(displayMsg.contains("Unauthorized")){
                        CommonUtils.functionalAlertDialog(this@TransactionDetailsActivity, "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(this@TransactionDetailsActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        CommonUtils.alertDialog(this@TransactionDetailsActivity, displayMsg)
                    }

                }
            })

    }


    private fun populateData(response: ListTransaction.ListTransactionResponse) {
//        activityList = ArrayList<ActivityModel>()
        val transactionList = response.transactionsList
        for (i in 0 until transactionList.size){
            var activityName = ""
            activityName = if(senderAccountAddress == transactionList[i].from) {
                "Send"
            }else{
                "Receive"
            }
            Log.d("TokenResponseValue", transactionList[i].value.toString())
            val tokenVal = if(transactionList[i].value.isNullOrEmpty()){
                "0"
            }else{
                transactionList[i].value
            }

            val tokenValue = if(!CommonUtils.isWholeNumber(tokenVal.toDouble())){
                CommonUtils.roundToDecimalPlaces(tokenVal, 6)
            }else{
                Convert.fromWei(tokenVal, Convert.Unit.ETHER).toPlainString()
            }

//            Log.d("TokenValue", tokenValue)

            activityList.add(
                ActivityModel(activityName, transactionList[i].createdAt, transactionList[i].to,
                transactionList[i].from, tokenValue.toString(), transactionList[i].hash)
            )
        }

        if(activityList.size>0){
            binding.noActivity.visibility = View.GONE
            binding.recyclerActivity.visibility = View.VISIBLE
        }else{
            binding.noActivity.visibility = View.VISIBLE
            binding.recyclerActivity.visibility = View.GONE
        }

        adapter = ActivityAdapter(this,null, activityList)
        // Setting the Adapter with the recyclerview
        binding.recyclerActivity.adapter = adapter
        binding.recyclerActivity.scrollToPosition(0)
    }

    override fun onPositiveButtonClick(callingPurpose: String?) {
        when (callingPurpose) {
            "unAuthorizedToken" -> callLogout(0)

            "alreadyDeletedWallet" -> callLogout(1)
        }
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