package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.microblocklabs.mpc.fragment.FullscreenSupportFragment
import com.microblocklabs.mpc.fragment.WalletBottomSheetFragment
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.interfaces.OnActivityDataReceivedListener
import com.microblocklabs.mpc.interfaces.OnInactivityListener
import com.microblocklabs.mpc.interfaces.OnTokenDataReceivedListener
import com.microblocklabs.mpc.model.BalanceDetails
import com.microblocklabs.mpc.model.NetworkItem
import com.microblocklabs.mpc.model.ReleaseItem
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.VestingDetails
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.VestingDetailsViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CategoryDropdownMenu
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.Constant
import com.microblocklabs.mpc.utility.InactivityTimer
import com.microblocklabs.mpc.utility.NetworkUtils
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ClientInterceptors
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import org.web3j.utils.Convert
import sendTransaction.SendTransaction
import sendTransaction.SendTransactionServiceGrpc
import vestDetails.VestDetails
import vestDetails.VestDetailsServiceGrpc


@Suppress("DEPRECATED_IDENTITY_EQUALS", "SameParameterValue")
class HomeScreenActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, OnInactivityListener, IAlertDialogButtonClickListener {
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
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    private lateinit var vestingDetailsViewModel: VestingDetailsViewModel
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomSheetFragment: WalletBottomSheetFragment
    private var userProfile: List<UserProfile>? = null
    private var walletList: MutableList<WalletDetails>? = mutableListOf()
    private var sharedPartList: List<SharePartDetails>? = null
    private var selectedAccount: WalletDetails? = null
    private var networkList: ArrayList<NetworkItem>? = null
    private var releaseDetailsList: ArrayList<ReleaseItem>? = null
    private var rotationAngle = 0
    private var balanceDetails: BalanceDetails ?=null
    private lateinit var inactivityTimer: InactivityTimer
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]
        vestingDetailsViewModel = ViewModelProvider(this)[VestingDetailsViewModel::class.java]
        iAlertDialogButtonClickListener = this
        inactivityTimer = InactivityTimer(Constant.INACTIVITY_TIMEOUT_MS, this)
        fetchDataFromDatabase()
        populateNetworkList()
        setupTabLayout()
        setupViewPager()
        setupDrawerLayout()

        binding.layoutNetworkAddress.setOnClickListener{
            showNetworkMenu()
        }

        binding.layoutWalletName.setOnClickListener{
            bottomSheetFragment = WalletBottomSheetFragment(this, mpcSharedPref, db, connectionChannel)
            bottomSheetFragment.show(supportFragmentManager, "WalletBottomSheetDialogFragment")
        }


        binding.layoutWalletAddress.setOnClickListener{
            CommonUtils.copyToClipBoard(this, "Wallet address", binding.walletAddress.text.toString())
        }
        binding.sendView.setOnClickListener{
            showSendTransactionScreen("",)
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
        inactivityTimer.start()
//        showMessage("UserInteracted")
    }

    @SuppressLint("SetTextI18n")
    private fun setupDrawerLayout(){
//        imgAccount = binding.navView.getHeaderView(0).findViewById(R.id.img_account)
        imgNavClose = binding.navView.getHeaderView(0).findViewById(R.id.img_nav_close)
        linearNavWalletName = binding.navView.getHeaderView(0).findViewById(R.id.nav_layout_wallet_name)
        txtNavWalletName = binding.navView.getHeaderView(0).findViewById(R.id.nav_wallet_name)
        txtNavWalletAmount = binding.navView.getHeaderView(0).findViewById(R.id.nav_wallet_amount)
        txtNavWalletAddress = binding.navView.getHeaderView(0).findViewById(R.id.nav_wallet_address)
        footerNavAppName = binding.navView.findViewById(R.id.footer_nav_app_name)
        footerNavAppVersion = binding.navView.findViewById(R.id.footer_nav_app_version)

        footerNavAppName.text = CommonUtils.getAppName(this)
        footerNavAppVersion.text = CommonUtils.getAppVersionName(this)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, 0, 0)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        linearNavWalletName.setOnClickListener{

        }
    }

    private fun setupDrawerHeaderData(){
        txtNavWalletName.text = binding.walletName.text
        txtNavWalletAddress.text = binding.walletAddress.text
        if (balanceDetails !=null){
            txtNavWalletAmount.text = "${balanceDetails!!.balance} CIFD"
        }
    }

    private fun showVestingSchedule(){
//        binding.titleVestingCifd.visibility = View.VISIBLE
        binding.vestingView.visibility = View.VISIBLE
    }

    private fun hideVestingSchedule(){
//        binding.titleVestingCifd.visibility = View.GONE
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

    private fun showActivityListScreen() {
        startActivity(Intent(applicationContext, TransactionDetailsActivity::class.java).apply {
            putExtra("selectedWalletAddress", binding.walletAddress.text.toString())
        })

//        val dialogFragment = FullscreenActivityFragment.newInstance(binding.walletAddress.text.toString(), mpcSharedPref, connectionChannel)
//        dialogFragment.show(supportFragmentManager, "FullScreenActivityFragment")
    }

    private fun showSupportScreen() {
        val dialogFragment = FullscreenSupportFragment()
        dialogFragment.show(supportFragmentManager, "FullscreenSupportFragment")
    }

    private fun showSendTransactionScreen(receiverAddress: String) {
        val dialogFragment = FullScreenSendTransactionFragment.newInstance(binding.walletAddress.text.toString(), receiverAddress, balanceDetails!!)
        dialogFragment.show(supportFragmentManager, "FullScreenSendTransactionFragment")
    }

    private fun showReceiveTransactionScreen() {
        val dialogFragment = FullScreenQRCodeFragment.newInstance(binding.walletAddress.text.toString())
        dialogFragment.show(supportFragmentManager, "FullScreenQRCodeFragment")
    }

    private fun fetchDataFromDatabase(){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
//        walletList = db.mWalletDetailsDao()!!.getWalletList()
        sharedPartList = db.mSharedPartDao()!!.getSharedPartDetailsList()

        lifecycleScope.launch {
            walletDetailsViewModel.walletDetailsList.observe(
                this@HomeScreenActivity,
                Observer { walletDetailsList ->
                    walletList?.clear()
                    walletList?.addAll(walletDetailsList)
                    setHomeScreenData(0)
                })
        }


    }

    fun setHomeScreenData(walletPost: Int) {
        if(!userProfile.isNullOrEmpty()){
//            binding.networkAddress.text = "N3tw0rk@ddr3ss2Sh0w"
//            binding.networkAddress.text = mpcSharedPref.getString("idToken")
        }

        if(!walletList.isNullOrEmpty()){
            binding.walletName.text = walletList!![walletPost].accountName
            binding.walletAddress.text = walletList!![walletPost].ethereumAddress

            setupDrawerHeaderData()

            if(walletList!![walletPost].ifUniqueId){
                showVestingSchedule()
            }else{
                hideVestingSchedule()
            }
        }

        if(NetworkUtils.isNetworkConnected(this)){
//            refreshVestingDataByWalletAddress(binding.walletAddress.text.toString())
            refreshUnLockedBalance(binding.walletAddress.text.toString())
        }else{
            CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
        }

//        setVestingDataByAccountDetails(userProfile!![0].email, binding.walletName.text.toString())
    }

    private fun refreshVestingDataByWalletAddress(walletAddress: String) {
        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcSharedPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val vestDetailService = VestDetailsServiceGrpc.newBlockingStub(channelWithHeader)
//        val metaDataKey = CallOptions.Key.create<String>("my_key")
        val requestMessage = VestDetails.VestDetailsRequest.newBuilder()
            .setEmail(userProfile!![0].email)
            .setAddress(walletAddress)
            .build()

        showLoadingDialog()

        Single.fromCallable { vestDetailService.vestDetails(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<VestDetails.VestDetailsResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: VestDetails.VestDetailsResponse) {
                    dismissLoadingDialog()
//                    refreshUnLockedBalance(binding.walletAddress.text.toString())
                    saveVestedDataToLocal(response)
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
                        CommonUtils.functionalAlertDialog(this@HomeScreenActivity, "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(this@HomeScreenActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        CommonUtils.alertDialog(this@HomeScreenActivity, displayMsg)
                    }

                }
            })
    }

    private fun saveVestedDataToLocal(response: VestDetails.VestDetailsResponse) {
        val vestingDetails = VestingDetails(0, response.totalAmount, response.lockedAmount, response.unlockedAmount, response.perMonth,
            response.twoPercentage, response.claimedAmount, response.joinedVestingCycle, response.claimedVestingCycle, response.isUserActive.toBoolean() )

        lifecycleScope.launch {
            vestingDetailsViewModel.deleteVestingDetail()
            vestingDetailsViewModel.insertVestingDetail(vestingDetails)
        }

        lifecycleScope.launch {
            vestingDetailsViewModel.vestingDetailsList.observe(
                this@HomeScreenActivity,
                Observer { vestingDetailsList ->
                    setupVestingDataFromVestingTable(vestingDetailsList)
                })
        }
    }


    private fun setupVestingDataFromVestingTable(vestingDetailsList: List<VestingDetails>){
        if(vestingDetailsList.isNotEmpty()){
            val lockedBal = CommonUtils.convertBigIntegerValueToFractionalFormat(vestingDetailsList[0].lockedAmount)
            val lockedBalUSD = (lockedBal.toBigDecimal() * Constant.tokenValWithUSD).toString()
            val totalBal = CommonUtils.convertBigIntegerValueToFractionalFormat(vestingDetailsList[0].totalAmount)
            val totalBalUSD = (totalBal.toBigDecimal() * Constant.tokenValWithUSD).toString()
            binding.vestedBalanceAmount.text = "${CommonUtils.roundToDecimalPlaces(lockedBal, 6)} CIFD"
            binding.vestedUsdAmount.text = "$ ${CommonUtils.roundToDecimalPlaces(lockedBalUSD, 6)} USD"
            binding.totalBalanceAmount.text = "${CommonUtils.roundToDecimalPlaces(totalBal, 6)} CIFD"
            binding.totalUsdAmount.text = "$ ${CommonUtils.roundToDecimalPlaces(totalBalUSD, 6)} USD"
            val perMonth = CommonUtils.convertBigIntegerValueToFractionalFormat(vestingDetailsList[0].perMonth)
            populateReleaseData(perMonth.toDouble())
            binding.viewPager.currentItem = 0
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setVestingDataByAccountDetails(email: String, accountName: String) {
        selectedAccount = db.mWalletDetailsDao()!!.getWalletDataByAccount(email, accountName)
        binding.vestedBalanceAmount.text = "${selectedAccount!!.RestValue} CIFD"
        binding.vestedUsdAmount.text = "$ ${(selectedAccount!!.RestValue.toBigDecimal() * Constant.tokenValWithUSD)} USD"
        binding.totalBalanceAmount.text = "${selectedAccount!!.Cifd} CIFD"
        binding.totalUsdAmount.text = "$ ${(selectedAccount!!.Cifd.toBigDecimal() * Constant.tokenValWithUSD)} USD"
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
        releaseDetailsList!!.add(ReleaseItem("Nov 16, 2023", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Dec 16, 2023", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Jan 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Feb 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Mar 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Apr 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("May 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Jun 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Jul 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Aug 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Sep 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Oct 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Nov 16, 2024", perMonth.toString(), "Vested"))
        releaseDetailsList!!.add(ReleaseItem("Dec 16, 2024", perMonth.toString(), "Vested"))

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
        var intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                if(CommonUtils.isValidEthereumAddress(intentResult.contents.toString())){
                    showSendTransactionScreen(intentResult.contents.toString())
                    intentResult = null
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
                showActivityListScreen()
            }
            R.id.nav_share -> {
                openChooserToShareWalletAddress()
            }
            R.id.nav_view -> {
                openCifdaqScanWeb()
            }
//            R.id.nav_settings -> {
//                Toast.makeText(this, "Settings feature is coming soon", Toast.LENGTH_SHORT).show()
//            }
            R.id.nav_support -> {
                showSupportScreen()
            }
//            R.id.nav_request -> {
//                Toast.makeText(this, "Request feature is coming soon", Toast.LENGTH_SHORT).show()
//            }
            R.id.nav_delete -> {
                CommonUtils.functionalAlertDialogWithTwoButton(this, "openDeleteWalletScreen", getString(R.string.delete_wallet_upper_camel), getString(R.string.delete_alert), iAlertDialogButtonClickListener!!)
            }
            R.id.nav_logout -> {
                CommonUtils.functionalAlertDialogWithTwoButton(this, "callLogout", getString(R.string.logout), getString(R.string.logout_alert), iAlertDialogButtonClickListener!!)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
//        binding.layoutGlassyBackground.visibility = View.GONE
        return true
    }


    private fun openChooserToShareWalletAddress(){
        val textToShare = "My CIFDAQ Wallet Address: ${binding.walletAddress.text}"
        val msg = "Get your own CIFDAQ wallet at "
        val websiteUrl = "https://cifdaqwallet.com/"
//        val msg = "You can check above wallet address through our website"
//        val websiteUrl = "https://cifdaqscan.io/"

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, "$textToShare\n\n$msg\n$websiteUrl")
        shareIntent.type = "text/plain"

        // Optionally, specify a title for the chooser dialog
        val chooserTitle = "Share my wallet address using..."
        val chooser = Intent.createChooser(shareIntent, chooserTitle)

        // Start the chooser activity
        startActivity(chooser)
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openCifdaqScanWeb(){
        val url = "https://cifdaqscan.io/address/${binding.walletAddress.text}" // Replace with the actual URL

        // Create an Intent to open a web browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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
                    refreshVestingDataByWalletAddress(binding.walletAddress.text.toString())
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
                    if(displayMsg.contains("Unauthorized")){
                        CommonUtils.functionalAlertDialog(this@HomeScreenActivity, "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(this@HomeScreenActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    }else{
                        CommonUtils.alertDialog(this@HomeScreenActivity, displayMsg)
                    }

                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun setupUnLockedBalance(response: Balance.BalanceResponse) {
        val balance = CommonUtils.roundToDecimalPlaces(response.balance, 6)
        balanceDetails = BalanceDetails(balance, response.address)
        binding.txtUnlockedAmount.text = "${balanceDetails!!.balance} CIFD"
        val balanceUSD = balanceDetails!!.balance * Constant.tokenValWithUSD
        binding.usdBalanceAmount.text = "$ ${CommonUtils.roundToDecimalPlaces(balanceUSD.toString(), 6)} USD"
        tokenDataReceivedListener.onTokenDataReceived(this@HomeScreenActivity, balanceDetails!!)
        txtNavWalletAmount.text = "${balanceDetails!!.balance} CIFD"
        setupDrawerHeaderData()

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
                    if(displayMsg.contains("Unauthorized")){
                        CommonUtils.functionalAlertDialog(this@HomeScreenActivity, "unAuthorizedToken", displayMsg, iAlertDialogButtonClickListener!!)
                    }else if(displayMsg.contains("User not found")){
                        val userDeletedMsg = getString(R.string.deleting_local_data_msg)
                        CommonUtils.functionalAlertDialog(this@HomeScreenActivity, "alreadyDeletedWallet", userDeletedMsg, iAlertDialogButtonClickListener!!)
                    } else{
                        showTransactionSuccessDialog("", senderAccount, receiverAccount, tokenAmt)
                    }

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


    fun callLogout(logoutVal: Int) {
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
            titleSuccess.text = getString(R.string.transaction_failed)
            titleSuccess.setTextColor(ContextCompat.getColor(this, R.color.red))

        }else{
            transactionHash.visibility = View.VISIBLE
            imgSuccess.background = resources.getDrawable(R.drawable.icon_success)
            titleSuccess.text = getString(R.string.transaction_successful)
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


    override fun onResume() {
        super.onResume()
        inactivityTimer.start()
    }

    override fun onStop() {
        super.onStop()
        inactivityTimer.stop()
    }
    override fun onInactive() {
        callLogout(0)
    }

    override fun onPositiveButtonClick(callingPurpose: String?) {
        when (callingPurpose) {
            "callLogout" -> callLogout(0)

            "unAuthorizedToken" -> callLogout(0)

            "openDeleteWalletScreen" -> openDeleteWalletScreen()

            "alreadyDeletedWallet" -> callLogout(1)
        }
    }

    private fun openDeleteWalletScreen(){
        startActivity(Intent(this, DeleteWalletActivity::class.java))
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


//    override fun onDestroy() {
//        super.onDestroy()
//        vestingDetailsViewModel.vestingDetailsList.removeObserver(observer)
//    }


}