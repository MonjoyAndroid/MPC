package com.microblocklabs.mpc.activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import cognito.Cognito
import cognito.CognitoServiceGrpc
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.PhraseRecyclerViewAdapter
import com.microblocklabs.mpc.databinding.ActivityPhaseRecoveryBinding
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import com.microblocklabs.mpc.model.PhraseData
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
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
import recoverUser.RecoverUser
import recoverUser.RecoverUserServiceGrpc
import java.util.Random
import kotlin.math.ceil


class PhraseRecoveryActivity : BaseActivity(), IAlertDialogButtonClickListener {
    private lateinit var binding: ActivityPhaseRecoveryBinding
    var openPurposeVal = 0 //Create Wallet = 0, Regular Login = 1, Forgot Password = 2, Import Wallet = 3, Delete Wallet = 4, Recover Wallet = 5
    private val recyclerDataArrayList = ArrayList<PhraseData>()
    private lateinit var userProfile: List<UserProfile>
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel
    private var iAlertDialogButtonClickListener: IAlertDialogButtonClickListener? = null
    private var finalResponse: Any? = null
    private var walletPass = ""
    private var privateKey = ""
    private var verifiedMnemonicPhrase = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhaseRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        openPurposeVal = intent.getIntExtra("openPhraseFor", 0)
        userProfile = db.mUserProfileDao()!!.getUserProfile()
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]
        iAlertDialogButtonClickListener = this

        when(openPurposeVal) {
            0 -> showPhraseScreenForCreateWallet()
            2 -> showPhraseScreenForForgotPassword()
            3 -> showPhraseScreenForImportWallet()
            4 -> showPhraseScreenForDeleteWallet()
            5 -> showPhraseScreenForRecoverWallet()
        }

        binding.imgArrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgCopySecretKey.setOnClickListener{
            CommonUtils.copyToClipBoard(this, "Seed Phrases", userProfile[0].mnemonic)
        }

        binding.txtCopyToClipSeedPhrase.setOnClickListener{
            CommonUtils.copyToClipBoard(this, "Seed Phrases", userProfile[0].mnemonic)
        }

        binding.imgCopyPrivateKey.setOnClickListener{
            CommonUtils.copyToClipBoard(this, "Private key", privateKey)
        }

        binding.txtCopyToClipPrivateKey.setOnClickListener{
            CommonUtils.copyToClipBoard(this, "Private key", privateKey)
        }

        binding.buttonNext.setOnClickListener{

            when(openPurposeVal) {
                0 -> showCongratulationScreen()
                2 -> verifyPhrase()
                3 -> verifyPhrase()
                4 -> CommonUtils.functionalAlertDialogWithTwoButton(this, "openConfirmDeleteWalletScreen",
                    getString(R.string.confirm_delete_wallet), getString(R.string.confirm_delete_alert), iAlertDialogButtonClickListener!!)
                5 -> verifyPhrase()
            }

        }
    }


    private fun verifyPhrase(){
        var enteredPhraseList = ArrayList<String>()
        for (i in 0 until recyclerDataArrayList.size ) {
            val view = binding.recyclerPhrase.getChildAt(i)
            val phraseEditText = view!!.findViewById(R.id.et_phrase) as EditText
            val phrase = phraseEditText.text.toString().trim()
            enteredPhraseList.add(phrase)
        }

        val nMonicString = convertListToSpaceSeparatedString(enteredPhraseList)
        if(NetworkUtils.isNetworkConnected(this)){
            when(openPurposeVal) {
                2 -> getUserDetailsByMnemonicPhrase(nMonicString)
                3 -> getUserDetailsByMnemonicPhrase(nMonicString)
                5 -> recoverUserDetailsByMnemonicPhrase(userProfile[0].email,nMonicString)
            }

        }else{
            CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
        }

    }

    private fun convertListToSpaceSeparatedString(enteredPhraseList: ArrayList<String>): String{
        val result = StringBuilder()
        for (string in enteredPhraseList) {
            result.append(string)
            result.append(" ")
        }
        return if (result.isNotEmpty()) result.substring(0, result.length - 1) else ""
    }

    private fun showPhraseScreenForForgotPassword() {
        binding.imgArrowBack.visibility = View.VISIBLE
        binding.layoutPrivateKey.visibility = View.GONE
        binding.txtCopyToClipSeedPhrase.visibility = View.GONE
        binding.imgCopySecretKey.visibility = View.GONE
        prepareDataForEnterPhrase()
    }

    private fun showPhraseScreenForImportWallet() {
        binding.imgArrowBack.visibility = View.VISIBLE
        binding.layoutPrivateKey.visibility = View.GONE
        binding.txtCopyToClipSeedPhrase.visibility = View.GONE
        binding.imgCopySecretKey.visibility = View.GONE
        prepareDataForEnterPhrase()
    }

    private fun showPhraseScreenForCreateWallet() {
        binding.imgArrowBack.visibility = View.GONE
        binding.layoutPrivateKey.visibility = View.GONE
        //binding.titleSecretRecovery.text = resources.getString(R.string.secret_recovery)
        binding.descSecretRecovery.text = resources.getString(R.string.secret_recovery_desc)
        binding.txtCopyToClipSeedPhrase.visibility = View.VISIBLE
        binding.imgCopySecretKey.visibility = View.VISIBLE
        prepareDataForShowPhrase()
    }

    private fun showPhraseScreenForDeleteWallet() {
        privateKey = intent.getStringExtra("privateKey")!!
        walletPass = intent.getStringExtra("password")!!
        binding.imgArrowBack.visibility = View.VISIBLE
        binding.layoutPrivateKey.visibility = View.VISIBLE
        binding.txtCopyToClipPrivateKey.text = privateKey
        //binding.titleSecretRecovery.text = resources.getString(R.string.secret_recovery)
        binding.descSecretRecovery.text = resources.getString(R.string.secret_recovery_delete_wallet_desc)
        binding.txtCopyToClipSeedPhrase.visibility = View.VISIBLE
        binding.imgCopySecretKey.visibility = View.VISIBLE
        binding.buttonNext.text = getText(R.string.delete)
        prepareDataForShowPhrase()
    }

    private fun showPhraseScreenForRecoverWallet() {
        binding.imgArrowBack.visibility = View.GONE
        binding.titleSecretRecovery.text = resources.getString(R.string.old_secret_recovery)
        binding.descSecretRecovery.text = resources.getString(R.string.enter_secret_recovery_desc)
        binding.layoutPrivateKey.visibility = View.GONE
        binding.txtCopyToClipSeedPhrase.visibility = View.GONE
        binding.imgCopySecretKey.visibility = View.GONE
        prepareDataForEnterPhrase()
    }

    private fun showCongratulationScreen() {
        startActivity(Intent(this, CongratulationActivity::class.java).apply {
            putExtra("openWelcomeScreenFor", openPurposeVal)
        })
        finish()
    }

    private fun showChangePasswordScreen() {
        startActivity(Intent(this, ChangePasswordActivity::class.java))
        finish()
    }

    private fun requestForSendOTP(email: String) {
        val cognitoService = CognitoServiceGrpc.newBlockingStub(connectionChannel)
        val requestMessage = Cognito.ForgotPasswordRequest.newBuilder()
            .setEmail(email)
            .build()

        showLoadingDialog()

        Single.fromCallable { cognitoService.forgotPassword(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Cognito.ForgotPasswordResponse> {
                override fun onSuccess(response: Cognito.ForgotPasswordResponse) {
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@PhraseRecoveryActivity, response.message)
                    showChangePasswordScreen()
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@PhraseRecoveryActivity, displayMsg)
                }
            })
    }

    private fun showLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finishAffinity()
    }

    private fun prepareDataForShowPhrase(){
        val isEditMode = false
        var phraseList: List<String> = ArrayList<String>()
        if(userProfile.isNotEmpty()){
            phraseList = userProfile[0].mnemonic.split(" ").map { it.trim() }
        }


//        val nmonic= "dawn erosion foster torch wait almost venue diary fantasy slow abandon whisper"
//        val phraseList = nmonic.split(" ").map { it.trim() }


        for(i in phraseList.indices){
            recyclerDataArrayList.add(PhraseData((i+1),phraseList[i]))
        }

//        // created new array list..
//        recyclerDataArrayList.add(PhraseData("DSA"))
//        recyclerDataArrayList.add(PhraseData("JAVA"))
//        recyclerDataArrayList.add(PhraseData("C++"))
//        recyclerDataArrayList.add(PhraseData("Python"))
//        recyclerDataArrayList.add(PhraseData("Node Js"))
//        recyclerDataArrayList.add(PhraseData("ANDROID"))
//        recyclerDataArrayList.add(PhraseData("IOS"))
//        recyclerDataArrayList.add(PhraseData("CSS"))
//        recyclerDataArrayList.add(PhraseData("HTML"))
//        recyclerDataArrayList.add(PhraseData("React Js"))
//        recyclerDataArrayList.add(PhraseData("Flutter"))
//        recyclerDataArrayList.add(PhraseData("Web3"))

        setRecyclerView(isEditMode)
    }

    private fun prepareDataForEnterPhrase(){
        val isEditMode = true
//        binding.titleSecretRecovery.text = resources.getString(R.string.enter_phrase_title)
        binding.descSecretRecovery.text = resources.getString(R.string.enter_secret_recovery_desc)
        binding.txtCopyToClipSeedPhrase.visibility = View.GONE

        val max = 12
        val weight = 4
        val weightedNum = 8
        val min = 1
        val tempList: MutableList<PhraseData> = ArrayList()
        var shuffledList: MutableList<PhraseData> = ArrayList()

        for (k in min..max) {
            val phraseData = PhraseData(k, "")
            tempList.add(phraseData)
            recyclerDataArrayList.add(phraseData)
        }
        tempList.shuffle()
//        shuffledList.addAll()
        print(tempList.shuffle().toString()  +" ")



//        val min = 1
//        val max = 12
////        val random: Int = Random().nextInt(max - min + 1) + min
//        generateRandomNumbers(min, max)

        // created new array list..
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))
//        recyclerDataArrayList.add(PhraseData(""))

        setRecyclerView(isEditMode)
    }

    private fun setRecyclerView(isEditMode: Boolean){
        val adapter = PhraseRecyclerViewAdapter(
            recyclerDataArrayList,
            this,
            isEditMode
        )
        val layoutManager = object : GridLayoutManager(this, 3) {
            override fun canScrollVertically(): Boolean {
                return false
            }

            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        binding.recyclerPhrase.layoutManager = layoutManager
        binding.recyclerPhrase.adapter = adapter
    }

    private fun weightedRandom(minValue: Int, maxValue: Int, weightedNum: Int, weight: Int): Int {
        val random = Random()
        var randomVal = random.nextInt(maxValue - minValue + weight + 1) + minValue
        if (randomVal > maxValue) {
            randomVal = weightedNum
        }
        return randomVal
    }

    private fun generateRandomNumbers(min: Int, max: Int) {
        // min & max will be changed as per your requirement. In my case, I've taken min = 2 & max = 32
        val randomNumberCount = 12
        var dif = max - min
        if (dif < randomNumberCount * 3) {
            dif = randomNumberCount * 3
        }
        val margin = ceil((dif.toFloat() / randomNumberCount).toDouble()).toInt()
        val randomNumberList: MutableList<Int> = ArrayList()
        val random = Random()
        for (i in 0 until randomNumberCount) {
            val range = margin * i + min // 2, 5, 8
            var randomNum = random.nextInt(margin)
            if (randomNum == 0) {
                randomNum = 1
            }
            val number = randomNum + range
            randomNumberList.add(number)
        }
        randomNumberList.sort()
        Log.i("generateRandomNumbers", "RandomNumberList: $randomNumberList")
    }


    private fun recoverUserDetailsByMnemonicPhrase(email: String, mnemonicPhrase: String) {
        if (mnemonicPhrase.trim().isEmpty()) {
            CommonUtils.alertDialog(this, resources.getString(R.string.please_enter_phrase))
            return
        }

        val recoverUserService = RecoverUserServiceGrpc.newBlockingStub(connectionChannel)
        val requestMessage = RecoverUser.RecoverUserByMnemonicRequest.newBuilder()
            .setEmail(email)
            .setMnemonic(mnemonicPhrase)
            .build()

        showLoadingDialog()

        Single.fromCallable { recoverUserService.recoverUserByMnemonic(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<RecoverUser.RecoverUserByMnemonicResponse> {
                override fun onSuccess(response: RecoverUser.RecoverUserByMnemonicResponse) {
                    dismissLoadingDialog()
                    if(!response.success){
                        CommonUtils.alertDialog(this@PhraseRecoveryActivity, response.message)
                    }else{
                        finalResponse = response
                        verifiedMnemonicPhrase = mnemonicPhrase
                        CommonUtils.functionalAlertDialog(this@PhraseRecoveryActivity, openPurposeVal.toString(),
                            resources.getString(R.string.phrase_validate_successfully), iAlertDialogButtonClickListener!!)
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
                    CommonUtils.alertDialog(this@PhraseRecoveryActivity, displayMsg)
                }
            })
    }


    private fun getUserDetailsByMnemonicPhrase(mnemonicPhrase: String) {
        if (mnemonicPhrase.trim().isEmpty()) {
            CommonUtils.alertDialog(this, resources.getString(R.string.please_enter_phrase))
            return
        }

        val cognitoService = CognitoServiceGrpc.newBlockingStub(connectionChannel)
        val requestMessage = Cognito.FindUserByMnemonicPhraseRequest.newBuilder()
            .setMnemonicPhrase(mnemonicPhrase)
            .build()

        showLoadingDialog()

        Single.fromCallable { cognitoService.findUserByMnemonicPhrase(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Cognito.FindUserByMnemonicPhraseResponse> {
                override fun onSuccess(response: Cognito.FindUserByMnemonicPhraseResponse) {
                    dismissLoadingDialog()
                    finalResponse = response
                    CommonUtils.functionalAlertDialog(this@PhraseRecoveryActivity, openPurposeVal.toString(),
                    resources.getString(R.string.phrase_validate_successfully), iAlertDialogButtonClickListener!!)

                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    val displayMsg =if(e.message.toString().contains(":")){
                        e.message.toString().substring(e.message.toString().lastIndexOf(":") + 1)
                    }else{
                        e.message.toString()
                    }
                    dismissLoadingDialog()
                    CommonUtils.alertDialog(this@PhraseRecoveryActivity, displayMsg)
                }
            })
    }

    private fun checkEmailValidityByPhrase(response : Cognito.FindUserByMnemonicPhraseResponse){
        userProfile = db.mUserProfileDao()!!.getUserProfile()
        if(response.email == userProfile[0].email){
            if(NetworkUtils.isNetworkConnected(this)){
                requestForSendOTP(userProfile[0].email)
            }else{
                CommonUtils.alertDialog(this, resources.getString(R.string.no_internet))
            }
        }else{
            CommonUtils.alertDialog(this, resources.getString(R.string.user_not_found_by_phrase_msg))
        }

    }


    private fun saveUserData(response : Cognito.FindUserByMnemonicPhraseResponse) {
        val userProfile = UserProfile(response.email, response.emailVerified, response.phoneNumber, response.phoneVerified, response.mnemonic)
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

        showLoginScreen()
    }

    private fun requestForDeleteWallet(email: String, password: String) {
//        if (email.isEmpty() or password.isEmpty()) {
//            CommonUtils.alertDialog(this, resources.getString(R.string.enter_password))
//            return
//        }

        val interceptor: ClientInterceptor = GrpcClientRequestInterceptor(mpcSharedPref)
        val channelWithHeader: Channel = ClientInterceptors.intercept(connectionChannel, interceptor)
        val deleteWalletService = DeleteWalletServiceGrpc.newBlockingStub(channelWithHeader)

        val requestMessage = DeleteWallet.DeleteWalletRequest.newBuilder()
            .setEmail(email)
            .setPassword(password)
            .build()

        showLoadingDialog()

        Single.fromCallable { deleteWalletService.deleteWallet(requestMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<DeleteWallet.DeleteWalletResponse> {
                override fun onSuccess(response: DeleteWallet.DeleteWalletResponse) {
                    dismissLoadingDialog()
                    if (response.success) {
                        deleteAllDataFromLocal()
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
                    showErrorMessage(displayMsg)
                }
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
            "2" -> checkEmailValidityByPhrase(finalResponse as Cognito.FindUserByMnemonicPhraseResponse)
            "3" -> saveUserData(finalResponse as Cognito.FindUserByMnemonicPhraseResponse)
            "openConfirmDeleteWalletScreen" -> requestForDeleteWallet(userProfile[0].email,walletPass) //replacing the value 4
            "5" -> saveUserDataForRecoverWallet(finalResponse as RecoverUser.RecoverUserByMnemonicResponse)
        }
        finalResponse = null
    }

    private fun saveUserDataForRecoverWallet(response: RecoverUser.RecoverUserByMnemonicResponse) {
        val userProfile = UserProfile(response.email, response.emailVerified, response.phoneNumber, response.phoneNumberVerified, verifiedMnemonicPhrase)
        userProfileViewModel.updateUserProfile(userProfile)

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

        showCongratulationScreen()
    }

}