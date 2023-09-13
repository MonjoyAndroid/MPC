package com.microblocklabs.mpc.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.button.MaterialButton
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.ActivityCreateWalletBinding
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
import com.microblocklabs.mpc.utility.UpperCaseTextWatcher
import com.shockwave.pdfium.PdfDocument
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import signup.SignUpServiceGrpc
import signup.Signup
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CreateWalletActivity : BaseActivity(), OnPageChangeListener, OnLoadCompleteListener {
    private lateinit var binding: ActivityCreateWalletBinding
    private var showInfoPop: PopupWindow?= null
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    var openPurposeVal = 0 //Create Wallet = 0, Regular Login = 1, Forgot Password = 2, Import Wallet = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]
        val textWatcher = UpperCaseTextWatcher(binding.etUniqueId)
        binding.etUniqueId.addTextChangedListener(textWatcher)

        binding.imgArrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgInfoUniqueId.setOnClickListener {
            showInfoPop = showInfoPopup()
            showInfoPop?.isOutsideTouchable = true
            showInfoPop?.isFocusable = false
//            showInfoPop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showInfoPop?.showAsDropDown(binding.imgInfoUniqueId, -760, 10)
        }

        binding.imgInfoPass.setOnClickListener {
            showInfoPop = showPassSuggestionPopup()
            showInfoPop?.isOutsideTouchable = true
            showInfoPop?.isFocusable = false
//            showInfoPop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showInfoPop?.showAsDropDown(binding.imgInfoPass, -760, 10)
        }

        binding.imgShowHidePass.setOnClickListener {
            if (binding.etPassword.length() > 0)
                showHidePass("ShowPass")
        }

        binding.imgShowHideConfirmPass.setOnClickListener {
            if (binding.etConfirmPassword.length() > 0)
                showHidePass("ShowConfirmPass")
        }

        binding.tvTermsConditions.setOnClickListener {
            openTermsAndConditionsPopup()
        }

        binding.buttonVerify.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val phoneWithoutCountryCode = binding.etMobile.text.toString()
            val uniqueID = binding.etUniqueId.text.toString()

            checkCredentialsValidation(email, phoneWithoutCountryCode, password, confirmPassword, uniqueID)
//            navigateToOTPVerificationScreen()
        }

        // set click event using setOnCountryChangeListener for countryName, countryCode, countryCodeName, countryFlagImage and more
        binding.countyCodePicker.setOnCountryChangeListener {

            val countryName = binding.countyCodePicker.selectedCountryName
            val countryCode = binding.countyCodePicker.selectedCountryCode
            val countryCodeName = binding.countyCodePicker.selectedCountryNameCode
            val countryCodeWithPlus = binding.countyCodePicker.selectedCountryCodeWithPlus
            val countryFlagImage = binding.countyCodePicker.selectedCountryFlagResourceId

            // set selected flag in image view
//            countryFlag.setImageResource(countryFlagImage)
//            Toast.makeText(this, "$countryName, $countryCode, $countryCodeName, $countryCodeWithPlus", Toast.LENGTH_SHORT).show()

        }

        binding.etEmail.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

        binding.etPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

//        binding.etUniqueId.addTextChangedListener(object: TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            @SuppressLint("UseCompatLoadingForDrawables")
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val text = s.toString().uppercase(Locale.ROOT)
//                binding.etUniqueId.setText(text)
//                binding.etUniqueId.setSelection(text.length)
//            }
//        })
    }

    private fun showHidePass(showHideFor: String) {
        when(showHideFor) {
            "ShowPass" -> {
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
            "ShowConfirmPass" -> {
                if (binding.imgShowHideConfirmPass.contentDescription == "Show") {
                    binding.imgShowHideConfirmPass.contentDescription = "Hide"
                    binding.imgShowHideConfirmPass.setBackgroundResource(R.drawable.icon_show)
                    binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.imgShowHideConfirmPass.contentDescription = "Show"
                    binding.imgShowHideConfirmPass.setBackgroundResource(R.drawable.icon_hide)
                    binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkCredentialsValidation(email: String, phoneWithoutCountryCode: String, password: String, confirmPassword: String, uniqueID: String){
        var phone = if(phoneWithoutCountryCode.isEmpty()){
            ""
        }else{
            binding.countyCodePicker.selectedCountryCodeWithPlus +phoneWithoutCountryCode
        }

        if(email.isEmpty() or password.isEmpty() or confirmPassword.isEmpty()){
            if(email.isEmpty() and password.isEmpty() and confirmPassword.isEmpty()){
                binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
            }else{
                if(email.isEmpty() and password.isEmpty()){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                }else if(email.isEmpty() and confirmPassword.isEmpty()){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                }else if (password.isEmpty() and confirmPassword.isEmpty()){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                }else{
                    if (email.isEmpty()){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    }else if(password.isEmpty()){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    }else if(confirmPassword.isEmpty()){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    }
                }
            }
            return
        }else if(!CommonUtils.isEmailValid(email) or !CommonUtils.isPasswordValidate(password) or (password != confirmPassword)){
            if(!CommonUtils.isEmailValid(email) and  !CommonUtils.isPasswordValidate(password) and (password != confirmPassword)){
                binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                CommonUtils.alertDialogWithErrorMsg(this, resources.getString(R.string.email_password_confirm_pass_wrong))
            }else{
                if(!CommonUtils.isEmailValid(email) and  !CommonUtils.isPasswordValidate(password)){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    CommonUtils.alertDialogWithErrorMsg(this, resources.getString(R.string.email_password_wrong))
                }else if(!CommonUtils.isPasswordValidate(password) and (password != confirmPassword)){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    CommonUtils.alertDialogWithErrorMsg(this, resources.getString(R.string.pass_wrong_confirm_pass_not_match))
                }else if(!CommonUtils.isEmailValid(email) and (password != confirmPassword)){
                    binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                    binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                    binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                    CommonUtils.alertDialogWithErrorMsg(this, resources.getString(R.string.email_wrong_confirm_pass_not_match))
                }else{
                    if(!CommonUtils.isEmailValid(email)){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        CommonUtils.alertDialog(this, resources.getString(R.string.email_not_valid))
                    } else if(!CommonUtils.isPasswordValidate(password)){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
                        CommonUtils.alertDialogWithErrorMsg(this, resources.getString(R.string.pass_correction_msg))
                    }else if(password != confirmPassword){
                        binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
                        binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                        binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_red)
                        CommonUtils.alertDialog(this, resources.getString(R.string.pass_confirm_pass_not_matching))
                    }
                }
            }
            return
        }else{
            binding.etEmail.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            binding.rlConfirmPasswordBox.background = resources.getDrawable(R.drawable.bg_border_grey)
            if(binding.checkBoxTerms.isChecked){
                if(NetworkUtils.isNetworkConnected(this)){
                    requestForRegisterUser(email, phone, password, uniqueID)
                }else{
                    CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
                }
            }else{
                showMessage(getString(R.string.terms_condition_agree_msg))
            }


        }
    }

    private fun showPassSuggestionPopup(): PopupWindow {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.info_message_layout, null)
        view.findViewById<TextView>(R.id.tv_message).text = resources.getString(R.string.pass_suggestion_msg)
        return PopupWindow(view, 800, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun dismissPassSuggestionPopup() {
        showInfoPop?.let {
            if(it.isShowing){
                it.dismiss()
                binding.imgInfoPass.background = resources.getDrawable(R.drawable.info_icon_white)
                binding.imgInfoUniqueId.background = resources.getDrawable(R.drawable.info_icon_white)
            }
            showInfoPop = null
        }

    }

    private fun showInfoPopup(): PopupWindow {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.info_message_layout, null)
        view.findViewById<TextView>(R.id.tv_message).text = resources.getString(R.string.unique_id_suggestion_msg)
        return PopupWindow(view, 800, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun dismissShowInfoPopup() {
        showInfoPop?.let {
            if(it.isShowing){
                it.dismiss()
                binding.imgInfoPass.background = resources.getDrawable(R.drawable.info_icon_white)
                binding.imgInfoUniqueId.background = resources.getDrawable(R.drawable.info_icon_white)
            }
            showInfoPop = null
        }

    }

    private fun requestForRegisterUser(email: String, phone: String, password: String, uniqueID: String) {
        
        val signUpService = SignUpServiceGrpc.newBlockingStub(connectionChannel)

        val requestMessage = Signup.SignUpRequest.newBuilder()
            .setEmail(email)
            .setPhoneNumber(phone)
            .setPassword(password)
            .setUniqueId(uniqueID)
            .build()

        showLoadingDialog()

        Single.fromCallable { signUpService.signUpUser(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Signup.SignUpResponse> {
                override fun onSuccess(response: Signup.SignUpResponse) {
                    dismissLoadingDialog()
//                    Log.d("MyResponse", response.toString())
                    saveUserData(response)


                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@CreateWalletActivity, displayMsg)
                }
            })
    }

    private fun saveUserData(response : Signup.SignUpResponse) {
        val userProfile = UserProfile(response.email, response.emailVerified, response.phoneNumber, response.phoneNumberVerified, response.mnemonic)
        userProfileViewModel.insertUserProfile(userProfile)

        val walletList = response.walletList
        for (i in 0 until walletList.size){
            val walletDetails = WalletDetails(
                walletList[i].ethereumAddress,
                response.email,
                walletList[i].accountCount,
                walletList[i].accountName,
                walletList[i].publickey,
                walletList[i].twoPercent,
                walletList[i].restValue,
                walletList[i].perMonth,
                walletList[i].cifd,
                walletList[i].uniqueId,
                walletList[i].ifUniqueId
            )

            val sharedPart = walletList[i].sharepart
            val sharePartDetails = SharePartDetails(walletList[i].ethereumAddress, sharedPart.x, sharedPart.y)
            walletDetailsViewModel.insertWalletDetail(walletDetails)
            sharePartViewModel.insertSharedPart(sharePartDetails)
        }

        navigateToOTPVerificationScreen()
    }

    private fun navigateToOTPVerificationScreen() {
        startActivity(Intent(this, OTPVerificationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("OTPVerificationOpenFor", 0)
        })
        finishAffinity()
    }



    @SuppressLint("MissingInflatedId", "SetJavaScriptEnabled")
    private fun openTermsAndConditionsPopup(){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_dialog_pdfview)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val btnDecline = dialog.findViewById<MaterialButton>(R.id.button_decline)
        btnDecline.setOnClickListener {
            binding.checkBoxTerms.isChecked = false
            dialog.dismiss()
        }
        val btnAccept = dialog.findViewById<MaterialButton>(R.id.button_accept)
        btnAccept.setOnClickListener {
            binding.checkBoxTerms.isChecked = true
            dialog.dismiss()
        }


//        val pdfView = dialog.findViewById<PDFView>(R.id.pdfView)
//        val assetManager = this.assets
//        val inputStream = assetManager.open("terms.pdf")
//        pdfView.fromStream(inputStream).load()


//        pdfView.fromAsset("covidfullvaccination.pdf")
//            .defaultPage(0)
//            .enableSwipe(true)
//            .swipeHorizontal(false)
//            .enableAnnotationRendering(true)
//            .scrollHandle(DefaultScrollHandle(this))
//            .onPageError { page, _ ->
//                showErrorMessage("Error at page: $page")
//            }
//            .load()


//        val pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf" // Replace with your PDF URL
//        val pdfUri = Uri.parse(pdfUrl)
//        // Load PDF from the URL
//        pdfView.fromUri(pdfUri)
//            .load()


        val webView = dialog.findViewById<WebView>(R.id.webView)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.setInitialScale(100)
//        webView.requestFocus()
        webView.settings.javaScriptEnabled = true

        val myPdfUrl = "https://cifdaqwallet.com/assets/pdf/cifdaqwallet_terms_conditions.pdf"
//        val myPdfUrl = "https://cifdaqscan.io/assets/cifdaqwallet_terms_conditions.pdf"
//        val myPdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        val url = "https://drive.google.com/viewerng/viewer?embedded=true&url=$myPdfUrl"
        webView.loadUrl(url)

    }

    private fun getAssetFromFolder(): String {
        val assetManager = applicationContext.assets
        val inputStream: InputStream = assetManager.open("terms.pdf")
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, len)
        }
        val pdfByteArray = byteArrayOutputStream.toByteArray()
        val charset = Charsets.UTF_8 // Specify the character encoding you want to use
        val convertedString = String(pdfByteArray, charset)
        return convertedString
    }


    private fun getPdfNameFromAssets(): String {
        return "file:///android_asset/terms.pdf"
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        TODO("Not yet implemented")
    }

    override fun loadComplete(nbPages: Int) {
//        val meta: PdfDocument.Meta = pdfView.getDocumentMeta()
//        printBookmarksTree(pdfView.getTableOfContents(), "-")
    }
}