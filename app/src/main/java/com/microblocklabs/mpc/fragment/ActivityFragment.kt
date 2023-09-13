package com.microblocklabs.mpc.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import aot.armsproject.utils.AppSharedPreferenceManager
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.activity.HomeScreenActivity
import com.microblocklabs.mpc.activity.LoginActivity
import com.microblocklabs.mpc.adapter.ActivityAdapter
import com.microblocklabs.mpc.databinding.FragmentActivityBinding
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.interfaces.OnActivityDataReceivedListener
import com.microblocklabs.mpc.model.ActivityModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
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

class ActivityFragment : Fragment(), OnActivityDataReceivedListener,
    IAlertDialogButtonClickListener {

    private lateinit var binding: FragmentActivityBinding
    private lateinit var activityList: ArrayList<ActivityModel>
    private lateinit var myActivity: HomeScreenActivity
    private lateinit var mpcSharedPref: AppSharedPreferenceManager
    private lateinit var connectionChannel: Channel
    private var senderAccountAddress: String = ""
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myActivity = activity as HomeScreenActivity
        myActivity.setActivityDataListener(this)
        iAlertDialogButtonClickListener = this
        setupData()
    }

    private fun setupData() {
        binding.noToken.text = getString(R.string.no_activity_found)
        if(NetworkUtils.isNetworkConnected(requireContext())){
            showTransactionHistory(100, 0, senderAccountAddress)
        }else{
            CommonUtils.alertDialog(requireContext(), resources.getString(R.string.no_internet))
        }

    }

    private fun populateData(response: ListTransaction.ListTransactionResponse) {
        activityList = ArrayList<ActivityModel>()
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
                CommonUtils.convertBigIntegerValueToFractionalFormat(tokenVal)
//                 Convert.fromWei(tokenVal, Convert.Unit.ETHER).toPlainString()
            }

//            Log.d("TokenValue", tokenValue)

            activityList.add(ActivityModel(activityName, transactionList[i].createdAt, transactionList[i].to,
                transactionList[i].from, tokenValue.toString(), transactionList[i].hash))
        }

        if(activityList.size>0){
            binding.noToken.visibility = View.GONE
            binding.recyclerToken.visibility = View.VISIBLE
        }else{
            binding.noToken.visibility = View.VISIBLE
            binding.recyclerToken.visibility = View.GONE
        }

        val adapter = ActivityAdapter(myActivity,this, activityList)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = false
        binding.recyclerToken.layoutManager = layoutManager
        // Setting the Adapter with the recyclerview
        binding.recyclerToken.adapter = adapter
        binding.recyclerToken.scrollToPosition(0)
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

        myActivity.showLoadingDialog()

        Single.fromCallable { transactionListService.listTransaction(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<ListTransaction.ListTransactionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: ListTransaction.ListTransactionResponse) {
                    myActivity.dismissLoadingDialog()
                    populateData(response)
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    myActivity.dismissLoadingDialog()
                    if(displayMsg.contains("Unauthorized")){
                        CommonUtils.functionalAlertDialog(requireContext(), "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(requireContext(), "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        CommonUtils.alertDialog(requireContext(), displayMsg)
                    }

                }
            })
    }

    override fun onActivityDataReceived(
        context: Context,
        senderAccount: String,
        prefManager: AppSharedPreferenceManager,
        connectionChannel: Channel
    ) {
        this.senderAccountAddress = senderAccount
        this.mpcSharedPref = prefManager
        this. connectionChannel = connectionChannel

    }

    override fun onPositiveButtonClick(callingPurpose: String?) {
        when (callingPurpose) {
            "unAuthorizedToken" -> myActivity.callLogout(0)

            "alreadyDeletedWallet" -> myActivity.callLogout(1)
        }
    }


}