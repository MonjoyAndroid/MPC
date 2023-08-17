package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cifd.Balance
import cifd.BalanceServiceGrpc
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.CategoryDropdownAdapter.CategorySelectedListener
import com.microblocklabs.mpc.adapter.ReleaseDetailsAdapter
import com.microblocklabs.mpc.adapter.TokenViewPagerAdapter
import com.microblocklabs.mpc.databinding.ActivityHomeScreenBinding
import com.microblocklabs.mpc.fragment.FullScreenQRCodeFragment
import com.microblocklabs.mpc.fragment.FullScreenSendTransactionFragment
import com.microblocklabs.mpc.fragment.WalletBottomSheetFragment
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.OnActivityDataReceivedListener
import com.microblocklabs.mpc.interfaces.OnTokenDataReceivedListener
import com.microblocklabs.mpc.model.BalanceDetails
import com.microblocklabs.mpc.model.NetworkItem
import com.microblocklabs.mpc.model.ReleaseItem
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.utility.CategoryDropdownMenu
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
import jp.wasabeef.blurry.Blurry
import org.web3j.utils.Convert
import sendTransaction.SendTransaction
import sendTransaction.SendTransactionServiceGrpc


@Suppress("DEPRECATED_IDENTITY_EQUALS", "SameParameterValue")
class HomeScreenActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var activityDataListener: OnActivityDataReceivedListener
    private lateinit var tokenDataReceivedListener: OnTokenDataReceivedListener
    private lateinit var imgAccount: ImageView
    private lateinit var imgNavClose: ImageView
    private lateinit var linearNavWalletName: LinearLayout
    private lateinit var txtNavWalletName: TextView
    private lateinit var txtNavWalletAmount: TextView
    private lateinit var txtNavWalletAddress: TextView
    private lateinit var footerNavAppName: TextView
    private lateinit var footerNavAppVersion: TextView

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomSheetFragment: WalletBottomSheetFragment
    private var userProfile: List<UserProfile>? = null
    private var walletList: List<WalletDetails>? = null
    private var sharedPartList: List<SharePartDetails>? = null
    private var selectedAccount: WalletDetails? = null
    private var networkList: ArrayList<NetworkItem>? = null
    private var releaseDetailsList: ArrayList<ReleaseItem>? = null
    private var rotationAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchDataFromDatabase()
        populateNetworkList()
        setupTabLayout()
        setupViewPager()
        setupDrawerLayout()
        if(walletList!![0].ifUniqueId){
            showVestingSchedule()
        }else{
            hideVestingSchedule()
        }

        binding.tvNetworkName.setOnClickListener{
            showNetworkMenu()
//            CommonUtils.copyToClipBoard(this, binding.networkAddress.text.toString())
        }

        binding.walletName.setOnClickListener{
            bottomSheetFragment = WalletBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, "WalletBottomSheetDialogFragment")
//            showMessage("create and show wallet work is under process")
        }


        binding.layoutWalletAddress.setOnClickListener{
            CommonUtils.copyToClipBoard(this, binding.walletAddress.text.toString())
        }
        binding.sendView.setOnClickListener{
            showSendTransactionScreen("")
        }

        binding.recieveView.setOnClickListener{
            showReceiveTransactionScreen()
        }

        binding.imgHamburger.setOnClickListener{
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.openDrawer(GravityCompat.END)
//                binding.layoutGlassyBackground.visibility = View.VISIBLE
            } else {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
//                binding.layoutGlassyBackground.visibility = View.GONE
            }
        }

        imgNavClose.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
//            binding.layoutGlassyBackground.visibility = View.GONE
        }

        binding.imgScanner.setOnClickListener{
            openQRScanner()
        }

        binding.layoutVestingHeader.setOnClickListener{
            showHideVestingContainer()
        }

        binding.imgRefresh.setOnClickListener{
            if(NetworkUtils.isNetworkConnected(this)){
                refreshUnLockedBalance(binding.walletAddress.text.toString())
            }else{
                CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
            }

        }
    }


    override fun onUserInteraction() {
        super.onUserInteraction()
    }

    private fun setupDrawerLayout(){
        imgAccount = binding.navView.getHeaderView(0).findViewById(R.id.img_account)
        imgNavClose = binding.navView.getHeaderView(0).findViewById(R.id.img_nav_close)
        linearNavWalletName = binding.navView.getHeaderView(0).findViewById(R.id.nav_layout_wallet_name)
        txtNavWalletName = binding.navView.getHeaderView(0).findViewById(R.id.nav_wallet_name)
        txtNavWalletAmount = binding.navView.getHeaderView(0).findViewById(R.id.nav_wallet_amount)
        txtNavWalletAddress = binding.navView.getHeaderView(0).findViewById(R.id.nav_wallet_address)
        footerNavAppName = binding.navView.findViewById(R.id.footer_nav_app_name)
        footerNavAppVersion = binding.navView.findViewById(R.id.footer_nav_app_version)

        txtNavWalletName.text = walletList!![0].accountName
        txtNavWalletAddress.text = walletList!![0].ethereumAddress
        footerNavAppName.text = CommonUtils.getAppName(this)
        footerNavAppVersion.text = CommonUtils.getAppVersionName()

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, 0, 0)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        linearNavWalletName.setOnClickListener{

        }
    }

    private fun showVestingSchedule(){
        binding.titleVestingCifd.visibility = View.VISIBLE
        binding.vestingView.visibility = View.VISIBLE
    }

    private fun hideVestingSchedule(){
        binding.titleVestingCifd.visibility = View.GONE
        binding.vestingView.visibility = View.GONE
    }

    private fun showHideVestingContainer() {

        rotationAngle = if (rotationAngle === 0) 180 else 0 //toggle
        binding.imgArrowVestingDropdown.animate().rotation(rotationAngle.toFloat()).setDuration(500).start()
        if(binding.layoutVestingContainer.visibility == View.VISIBLE){
            binding.layoutVestingContainer.visibility = View.GONE
        } else {
            binding.layoutVestingContainer.visibility = View.VISIBLE
        }
    }

    private fun showSendTransactionScreen(receiverAddress: String) {
        val dialogFragment = FullScreenSendTransactionFragment.newInstance(binding.walletAddress.text.toString(), receiverAddress)
        dialogFragment.show(supportFragmentManager, "FullScreenSendTransactionFragment")
    }

    private fun showReceiveTransactionScreen() {
        val dialogFragment = FullScreenQRCodeFragment.newInstance(binding.walletAddress.text.toString())
        dialogFragment.show(supportFragmentManager, "FullScreenQRCodeFragment")
    }

    private fun fetchDataFromDatabase(){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
        walletList = db.mWalletDetailsDao()!!.getWalletList()
        sharedPartList = db.mSharedPartDao()!!.getSharedPartDetailsList()

        setHomeScreenData()
    }

    private fun setHomeScreenData() {
        if(!userProfile.isNullOrEmpty()){
//            binding.networkAddress.text = "N3tw0rk@ddr3ss2Sh0w"
//            binding.networkAddress.text = mpcSharedPref.getString("idToken")
        }

        if(!walletList.isNullOrEmpty()){
            binding.walletName.text = walletList!![0].accountName
            binding.walletAddress.text = walletList!![0].ethereumAddress
        }

        if(NetworkUtils.isNetworkConnected(this)){
            refreshUnLockedBalance(binding.walletAddress.text.toString())
        }else{
            CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
        }

        setVestingDataByAccountDetails(userProfile!![0].email, binding.walletName.text.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun setVestingDataByAccountDetails(email: String, accountName: String) {
        selectedAccount = db.mWalletDetailsDao()!!.getWalletDataByAccount(email, accountName)
        binding.vestedBalanceAmount.text = "${selectedAccount!!.RestValue} CIFD"
        binding.vestedUsdAmount.text = "$ ${(selectedAccount!!.RestValue * 0.8)} USD"
        binding.totalBalanceAmount.text = "${selectedAccount!!.Cifd} CIFD"
        binding.totalUsdAmount.text = "$ ${(selectedAccount!!.Cifd * 0.8)} USD"
        populateReleaseData(selectedAccount!!.PerMonth)
    }

    private fun populateNetworkList() {
        networkList = ArrayList()
        networkList!!.add(NetworkItem("CIFDAQ Obsidian Network", ""))
        networkList!!.add(NetworkItem("CIFD Main Network", ""))
        networkList!!.add(NetworkItem("Test Network", ""))

        binding.tvNetworkName.text = networkList!![0].networkName

    }

    private fun showNetworkMenu() {
        val menu = CategoryDropdownMenu(this, networkList!!)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = getPxFromDp(200)
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.showAsDropDown(binding.tvNetworkName)
        menu.setCategorySelectedListener(object : CategorySelectedListener {
            override fun onCategorySelected(position: Int, category: NetworkItem?) {
                menu.dismiss()
                binding.tvNetworkName.text = category!!.networkName
            }
        })
    }

    //Convert DP to Pixel
    private fun getPxFromDp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    private fun populateReleaseData(perMonth: Double){
        releaseDetailsList = ArrayList()
        releaseDetailsList!!.add(ReleaseItem("16 Nov 2023", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Dec 2023", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Jan 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Feb 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Mar 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Apr 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 May 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Jun 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Jul 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Aug 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Sep 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Oct 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Nov 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("16 Dec 2024", perMonth.toString(), "Vested"))

        setupReleaseRecyclerView()
    }

    private fun setupReleaseRecyclerView() {
        val adapter = releaseDetailsList?.let {
            ReleaseDetailsAdapter(
                it,
                this
            )
        }
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerRelease.layoutManager = layoutManager
        binding.recyclerRelease.adapter = adapter
    }

    private fun setupViewPager(){
        binding.viewPager.apply {
            adapter = TokenViewPagerAdapter(supportFragmentManager, binding.tabLayout.tabCount)
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        }
    }

    private fun setupTabLayout(){
        binding.tabLayout.apply {
            addTab(this.newTab().setText("Tokens"))
            addTab(this.newTab().setText("NFTs"))
            addTab(this.newTab().setText("Activity"))

            // tabGravity = TabLayout.GRAVITY_FILL

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let {
                        binding.viewPager.currentItem = it
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }

    }


    private fun openQRScanner(){
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setPrompt("Scan a QR Code")
        intentIntegrator.setOrientationLocked(false)
        intentIntegrator.initiateScan()
    }



    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                if(CommonUtils.isValidEthereumAddress(intentResult.contents.toString())){
                    showSendTransactionScreen(intentResult.contents.toString())
                }else{
                    CommonUtils.alertDialog(this, "Not a valid account address")
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_activity -> {
                Toast.makeText(this, "Activity clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_share -> {
                Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_view -> {
                Toast.makeText(this, "View clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_support -> {
                Toast.makeText(this, "Support clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_request -> {
                Toast.makeText(this, "Request clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_delete -> {
                Toast.makeText(this, "Delete wallet clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                callLogout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
//        binding.layoutGlassyBackground.visibility = View.GONE
        return true
    }

    private fun refreshUnLockedBalance(accountAddress: String){
        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcSharedPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val balanceService = BalanceServiceGrpc.newBlockingStub(channelWithHeader)
//        val metaDataKey = CallOptions.Key.create<String>("my_key")
        val requestMessage = Balance.BalanceRequest.newBuilder()
            .setAddress(accountAddress)
            .build()

        showLoadingDialog()

        Single.fromCallable { balanceService.getBalance(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Balance.BalanceResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: Balance.BalanceResponse) {
                    dismissLoadingDialog()
                    setupUnLockedBalance(response)
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@HomeScreenActivity, displayMsg)
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun setupUnLockedBalance(response: Balance.BalanceResponse) {
        val balanceDetails = BalanceDetails(response.balance.toDouble(), response.address)
//        binding.txtUnlockedAmount.text = "${balanceDetails.balance} CIFD"
        binding.txtUnlockedAmount.text = "${CommonUtils.convertDecimalUptoSixDigits(balanceDetails.balance)} CIFD"
        binding.usdBalanceAmount.text = "$ ${CommonUtils.convertDecimalUptoFourDigits(balanceDetails.balance) * 0.8} USD"
        tokenDataReceivedListener.onTokenDataReceived(this@HomeScreenActivity, balanceDetails )

    }

    fun callTransaction(senderAccount: String, receiverAccount: String, tokenAmt: String){
        if(NetworkUtils.isNetworkConnected(this)){
            sendTransaction(userProfile!![0].email,senderAccount,receiverAccount, tokenAmt)
        }else{
            CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
        }

    }


    private fun sendTransaction(email: String, senderAccount: String, receiverAccount: String, tokenAmt: String){
        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcSharedPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val sendTransactionService = SendTransactionServiceGrpc.newBlockingStub(channelWithHeader)

//        web3.utils.toWei(tokenAmt,"ether");

        val share = SendTransaction.Share.newBuilder()
            .setX(sharedPartList!![0].x)
            .setY(sharedPartList!![0].y)
            .build()

        val requestMessage = SendTransaction.TransactionRequest.newBuilder()
            .setEmail(email)
            .setSenderAddress(senderAccount)
            .setReceiverAddress(receiverAccount)
            .setToken(tokenAmt)
            .setSharepart(share)
            .build()

        Log.d("Transaction amount", tokenAmt)
        showLoadingDialog()

        Single.fromCallable { sendTransactionService.sendTransaction(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<SendTransaction.TransactionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: SendTransaction.TransactionResponse) {
                    dismissLoadingDialog()
//                    Log.d("Transaction Response", response.toString())
                    finishSendTransactionFragment()
                    showTransactionSuccessDialog(response.transactionHash, response.from, response.to, response.value)

                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    showTransactionSuccessDialog("", senderAccount, receiverAccount, tokenAmt)
                }
            })
    }

    fun finishSendTransactionFragment(){
        val prev: Fragment? = supportFragmentManager.findFragmentByTag("FullScreenSendTransactionFragment")
        if (prev != null) {
            val df: DialogFragment = prev as DialogFragment
            df.dismiss()
        }
    }

    fun setTokenDataListener(tokenDataReceivedListener: OnTokenDataReceivedListener){
        this.tokenDataReceivedListener = tokenDataReceivedListener
    }

    fun setActivityDataListener(activityDataListener: OnActivityDataReceivedListener){
        this.activityDataListener = activityDataListener
        activityDataListener.onActivityDataReceived(this, binding.walletAddress.text.toString(), mpcSharedPref, connectionChannel )
    }

    private fun callLogout() {
//        showBlurEffect()
        mpcSharedPref.clear()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finishAffinity()
    }

    fun showBlurEffect(){
        Blurry.with(this)
            .radius(10)
            .sampling(8)
            .color(Color.argb(66, 255, 0, 255))
            .async()
            .animate(500)
            .onto(binding.layoutGlassyBackground)
    }

    fun hideBlurEffect(){

    }


    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun showTransactionSuccessDialog(transHash: String, fromAccount: String, toAccount: String, amount: String,){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setWindowAnimations(R.style.AnimationForDialog)
        dialog.setContentView(R.layout.success_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val transactionHash = dialog.findViewById<TextView>(R.id.transaction_hash)
        val senderAccount = dialog.findViewById<TextView>(R.id.from_account)
        val receiverAccount = dialog.findViewById<TextView>(R.id.to_account)
        val transactionAmount = dialog.findViewById<TextView>(R.id.transaction_amount)
        val imgSuccess = dialog.findViewById<ImageView>(R.id.img_success)
        val titleSuccess = dialog.findViewById<TextView>(R.id.title_success)


        if(transHash.isEmpty()){
            transactionHash.visibility = View.GONE
            imgSuccess.background = resources.getDrawable(R.drawable.icon_failed)
            titleSuccess.text = "Failed"
            titleSuccess.setTextColor(ContextCompat.getColor(this, R.color.red))

        }else{
            transactionHash.visibility = View.VISIBLE
            imgSuccess.background = resources.getDrawable(R.drawable.icon_success)
            titleSuccess.text = "Success"
            titleSuccess.setTextColor(ContextCompat.getColor(this, R.color.base_primary))
        }

        transactionHash.text = "Transaction Id: $transHash"
        senderAccount.text = "From: $fromAccount"
        receiverAccount.text = "To: $toAccount"

        val tokenValue = Convert.fromWei(amount, Convert.Unit.ETHER).toPlainString()
        transactionAmount.text = "Amount: $tokenValue CIFD"



        val btnDone = dialog.findViewById<Button>(R.id.button_done)
        btnDone.setOnClickListener {
            dialog.dismiss()
            if(transHash.isNotEmpty()){
                if(NetworkUtils.isNetworkConnected(this)){
                    refreshUnLockedBalance(binding.walletAddress.text.toString())
                }else{
                    CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
                }

            }
        }
    }


}