package com.microblocklabs.mpc.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import aot.armsproject.utils.AppSharedPreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.activity.HomeScreenActivity
import com.microblocklabs.mpc.adapter.AccountAdapter
import com.microblocklabs.mpc.databinding.FragmentWalletBottomSheetBinding
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.interfaces.OnBottomSheetItemClickListener
import com.microblocklabs.mpc.room.MPCDatabase
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
import createAcc.CreateAcc
import createAcc.CreateAccServiceGrpc
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import recoverUser.RecoverUser
import vestDetails.VestDetails
import vestDetails.VestDetailsServiceGrpc
import java.util.Random


class WalletBottomSheetFragment(activity: Activity, mpcPref: AppSharedPreferenceManager, db: MPCDatabase, connectionChannel: ManagedChannel)
    : BottomSheetDialogFragment(), OnBottomSheetItemClickListener, IAlertDialogButtonClickListener {

    private lateinit var binding: FragmentWalletBottomSheetBinding
    private var mpcPref: AppSharedPreferenceManager
    private var db: MPCDatabase
    private var connectionChannel: ManagedChannel
    private var userProfile: List<UserProfile>? = null
    private var walletList: MutableList<WalletDetails>? = mutableListOf()
    private var sharedPartList: List<SharePartDetails>? = null
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    private var activity: HomeScreenActivity
    private lateinit var onBottomSheetItemClickListener: OnBottomSheetItemClickListener
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null

    init {
        this.activity = activity as HomeScreenActivity
        this.mpcPref = mpcPref
        this.db = db
        this.connectionChannel = connectionChannel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]
        onBottomSheetItemClickListener = this
        iAlertDialogButtonClickListener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        fetchDataFromDatabase()

    }

    private fun fetchDataFromDatabase(){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
//        walletList = db.mWalletDetailsDao()!!.getWalletList()
        sharedPartList = db.mSharedPartDao()!!.getSharedPartDetailsList()

        setupScreen()
    }

    private fun setupScreen() {

        lifecycleScope.launch {
            walletDetailsViewModel.walletDetailsList.observe(
                this@WalletBottomSheetFragment,
                Observer { walletDetailsList ->
                    walletList?.clear()
                    walletList?.addAll(walletDetailsList)
                    setupWalletDataFromWalletTable(walletDetailsList)
                })
        }

        binding.imgCross.setOnClickListener {
            dismiss()
        }

        binding.buttonCreateNewAccount.setOnClickListener {
            showCreateAccountScreen()
        }

        binding.buttonCreateAccount.setOnClickListener {
            showCreateAccountScreen()
        }

        binding.buttonCancel.setOnClickListener {
            binding.etAccountName.setText("")
            if(walletList!!.size>1){
                showAccountListScreen()
            }else{
                showNoMoreAccountScreen()
            }
        }

        binding.buttonCreate.setOnClickListener {
            if(binding.etAccountName.text.isEmpty()){
                CommonUtils.alertDialog(activity, resources.getString(R.string.hint_enter_account_name))
            }else{
                if(NetworkUtils.isNetworkConnected(activity)){
                    callApiToAddAccount(binding.etAccountName.text.toString(), "", userProfile!![0].mnemonic)
                }else{
                    CommonUtils.alertDialog(activity, resources.getString(R.string.no_internet))
                }
            }

//            saveWalletDataIntoLocal("dummy@ddressEthereum ${generateRandomNumber()}", 2, binding.etAccountName.text.toString(), "PubKeyABCD")
        }

    }

    private fun setupWalletDataFromWalletTable(walletDetailsList: List<WalletDetails>?) {
        val adapter = AccountAdapter(activity, walletDetailsList!!, onBottomSheetItemClickListener)
        binding.recyclerAccount.layoutManager = LinearLayoutManager(requireContext())
        // Setting the Adapter with the recyclerview
        binding.recyclerAccount.adapter = adapter

        if(walletDetailsList.size>1){
            showAccountListScreen()
        }else{
            showNoMoreAccountScreen()
        }
    }

    private fun showNoMoreAccountScreen(){
        binding.titleAccount.text = requireActivity().getString(R.string.add_account)
        binding.layoutCreateAccount.visibility = View.GONE
        binding.layoutNoMoreAccount.visibility = View.VISIBLE
        binding.layoutAllAccount.visibility = View.GONE
    }

    private fun showAccountListScreen(){
        binding.titleAccount.text = requireActivity().getString(R.string.account)
        binding.layoutCreateAccount.visibility = View.GONE
        binding.layoutNoMoreAccount.visibility = View.GONE
        binding.layoutAllAccount.visibility = View.VISIBLE
    }

    private fun showCreateAccountScreen(){
        binding.titleAccount.text = requireActivity().getString(R.string.add_account)
        binding.layoutCreateAccount.visibility = View.VISIBLE
        binding.layoutNoMoreAccount.visibility = View.GONE
        binding.layoutAllAccount.visibility = View.GONE
    }

    private fun callApiToAddAccount(accountName: String, uniqueId: String, mNeomonic: String) {
        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val createAccountService = CreateAccServiceGrpc.newBlockingStub(channelWithHeader)
        val requestMessage = CreateAcc.CreateAccRequest.newBuilder()
            .setAccountName(accountName)
            .setUniqueId(uniqueId)
            .setMnemonic(mNeomonic)
            .build()

        activity.showLoadingDialog()

        Single.fromCallable { createAccountService.createAcc(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<CreateAcc.CreateAccResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: CreateAcc.CreateAccResponse) {
                    activity.dismissLoadingDialog()
                    saveWalletDataIntoLocal(response)
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    activity.dismissLoadingDialog()
                    if(displayMsg.contains("Unauthorized")){
                        CommonUtils.functionalAlertDialog(activity, "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(activity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        CommonUtils.alertDialog(activity, displayMsg)
                    }

                }
            })
    }


    private fun saveWalletDataIntoLocal(createAccResponse: CreateAcc.CreateAccResponse) {
        val walletDetails = WalletDetails(
            createAccResponse.ethereumAddress,
            userProfile!![0].email,
            createAccResponse.accountCount,
            createAccResponse.accountName,
            createAccResponse.publickey,
            createAccResponse.twoPercent,
            createAccResponse.restValue,
            createAccResponse.perMonth,
            createAccResponse.cifd,
            createAccResponse.uniqueId,
            createAccResponse.ifUniqueId
        )

        val sharedPart = createAccResponse.sharepart
        val sharePartDetails = SharePartDetails(createAccResponse.ethereumAddress, sharedPart.x, sharedPart.y)

        lifecycleScope.launch {
            walletDetailsViewModel.insertWalletDetail(walletDetails)
            sharePartViewModel.insertSharedPart(sharePartDetails)
        }

        binding.etAccountName.setText("")
        setupScreen()

    }

//    private fun saveWalletDataIntoLocal(ethereumAddress: String, accountCount: Int, accountName: String, publickey: String) {
//            val walletDetails = WalletDetails(
//                ethereumAddress,
//                userProfile!![0].email,
//                accountCount,
//                accountName,
//                publickey,
//                0.00,
//                0.00,
//                0.00,
//                0.00,
//                "",
//                false
//            )
//
////            val sharedPart = createAccResponse.sharepart
////            val sharePartDetails = SharePartDetails(createAccResponse.ethereumAddress, sharedPart.x, sharedPart.y)
////            walletDetailsViewModel.insertWalletDetail(walletDetails)
////            sharePartViewModel.insertSharedPart(sharePartDetails)
//
//        lifecycleScope.launch {
//            walletDetailsViewModel.insertWalletDetail(walletDetails)
//        }
//        binding.etAccountName.setText("")
//        setupScreen()
//
//    }

    fun generateRandomNumber(): Int {
        val random = Random()
        return random.nextInt()
    }

    override fun onItemDismiss(position: Int) {
        dismiss()
    }

    override fun onPositiveButtonClick(callingPurpose: String?) {
        when (callingPurpose) {
            "unAuthorizedToken" -> activity.callLogout(0)

            "alreadyDeletedWallet" -> activity.callLogout(1)
        }
    }

}