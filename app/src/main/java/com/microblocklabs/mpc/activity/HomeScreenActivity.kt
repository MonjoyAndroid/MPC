package com.microblocklabs.mpc.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.microblocklabs.mpc.adapter.CategoryDropdownAdapter.CategorySelectedListener
import com.microblocklabs.mpc.adapter.ReleaseDetailsAdapter
import com.microblocklabs.mpc.adapter.TokenViewPagerAdapter
import com.microblocklabs.mpc.databinding.ActivityHomeScreenBinding
import com.microblocklabs.mpc.fragment.FullScreenQRCodeFragment
import com.microblocklabs.mpc.fragment.FullScreenSendTransactionFragment
import com.microblocklabs.mpc.fragment.WalletBottomSheetFragment
import com.microblocklabs.mpc.model.NetworkItem
import com.microblocklabs.mpc.model.ReleaseItem
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.utility.CategoryDropdownMenu
import com.microblocklabs.mpc.utility.CommonUtils


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class HomeScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomSheetFragment: WalletBottomSheetFragment
    private var userProfile: List<UserProfile>? = null
    private var walletList: List<WalletDetails>? = null
    private var networkList: ArrayList<NetworkItem>? = null
    private var releaseDetailsList: ArrayList<ReleaseItem>? = null
    private var popupWindowNetwork:PopupWindow? = null
    var rotationAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchDataFromDatabase()
        populateNetworkList()
        populateReleaseData()
        setupTabLayout()
        setupViewPager()


        binding.tvNetworkName.setOnClickListener{
            showCategoryMenu()
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
            showMessage("Navigation drawer work is under process")
        }

        binding.imgScanner.setOnClickListener{
            openQRScanner()
//            showMessage("QR code scanner work is under process")
        }

        binding.layoutVestingHeader.setOnClickListener{
            showHideVestingContainer()
        }
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
    }

    private fun populateNetworkList() {
        networkList = ArrayList()
        networkList!!.add(NetworkItem("CIFD Main Network", ""))
        networkList!!.add(NetworkItem("CIFDAQ Obsidian Network", ""))
        networkList!!.add(NetworkItem("Test Network", ""))

        binding.tvNetworkName.text = networkList!![0].networkName

    }

    private fun showCategoryMenu() {
        val menu = CategoryDropdownMenu(this, networkList!!)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = getPxFromDp(200)
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.showAsDropDown(binding.tvNetworkName)
        menu.setCategorySelectedListener(object : CategorySelectedListener {
            override fun onCategorySelected(position: Int, category: NetworkItem?) {
                menu.dismiss()
                binding.tvNetworkName.setText(category!!.networkName)
            }
        })
    }

    //Convert DP to Pixel
    private fun getPxFromDp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    private fun populateReleaseData(){
        releaseDetailsList = ArrayList()
        releaseDetailsList!!.add(ReleaseItem("15 July", "4898 CIFD", "Released"))
        releaseDetailsList!!.add(ReleaseItem("25 July", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("30 July", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("02 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("10 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("20 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("25 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("28 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("30 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("10 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("20 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("25 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("28 AUG", "4898 CIFD", "Yet to release"))
        releaseDetailsList!!.add(ReleaseItem("30 AUG", "4898 CIFD", "Yet to release"))

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


    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                showSendTransactionScreen(intentResult.contents.toString())
//                showMessage("ScanCode: ${intentResult.contents}")
                // if the intentResult is not null we'll set
                // the content and format of scan message
//                messageText.setText(intentResult.contents)
//                messageFormat.setText(intentResult.formatName)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



}