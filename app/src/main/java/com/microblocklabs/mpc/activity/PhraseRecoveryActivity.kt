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
import com.microblocklabs.mpc.model.PhraseData
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails
import com.microblocklabs.mpc.room.viewmodel.SharePartViewModel
import com.microblocklabs.mpc.room.viewmodel.UserProfileViewModel
import com.microblocklabs.mpc.room.viewmodel.WalletDetailsViewModel
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.NetworkUtils
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Random
import kotlin.math.ceil


class PhraseRecoveryActivity : BaseActivity() {
    private lateinit var binding: ActivityPhaseRecoveryBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    var openPurposeVal = 0 //Create Wallet = 0, Regular Login = 1, Forgot Password = 2, Import Wallet = 3
    private val recyclerDataArrayList = ArrayList<PhraseData>()
    private lateinit var userProfile: List<UserProfile>
    private lateinit var walletDetailsViewModel: WalletDetailsViewModel
    private lateinit var sharePartViewModel: SharePartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhaseRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        openPurposeVal = intent.getIntExtra("openPhraseFor", 0)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        walletDetailsViewModel = ViewModelProvider(this)[WalletDetailsViewModel::class.java]
        sharePartViewModel = ViewModelProvider(this)[SharePartViewModel::class.java]
        when(openPurposeVal) {
            0 -> showPhraseScreenForCreateWallet()
            2 -> showPhraseScreenForForgotPassword()
            3 -> showPhraseScreenForImportWallet()
        }

        binding.imgArrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.txtCopyToClip.setOnClickListener{
            CommonUtils.copyToClipBoard(this, userProfile[0].mnemonic)
        }

        binding.buttonNext.setOnClickListener{

//            showChangePasswordScreen()
            when(openPurposeVal) {
                0 -> showCongratulationScreen()
                2 -> verifyPhrase()
                3 -> verifyPhrase()
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
            getUserDetailsByMnemonicPhrase(nMonicString)
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
        return if (result.length > 0) result.substring(0, result.length - 1) else ""
    }

    private fun showPhraseScreenForForgotPassword() {
        binding.imgArrowBack.visibility = View.VISIBLE
        prepareDataForEnterPhrase()
    }

    private fun showPhraseScreenForImportWallet() {
        binding.imgArrowBack.visibility = View.VISIBLE
        prepareDataForEnterPhrase()
    }

    private fun showPhraseScreenForCreateWallet() {
        binding.imgArrowBack.visibility = View.GONE
        prepareDataForShowPhrase()
    }

    private fun showCongratulationScreen() {
        startActivity(Intent(this, CongratulationActivity::class.java))
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
//                    Log.d("MyResponse", response.toString())
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
//        binding.titleSecretRecovery.text = resources.getString(R.string.secret_recovery)
        binding.descSecretRecovery.text = resources.getString(R.string.secret_recovery_desc)
        binding.txtCopyToClip.visibility = View.VISIBLE
        var phraseList: List<String> = ArrayList<String>()
        userProfile = db.mUserProfileDao()!!.getUserProfile()
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
        binding.txtCopyToClip.visibility = View.GONE

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
                    Log.d("MyResponse", response.toString())
                    when(openPurposeVal) {
                        2 -> checkEmailValidityByPhrase(response)
                        3 -> saveUserData(response)
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
                walletList[i].twoPercent.toDouble(),
                walletList[i].restValue.toDouble(),
                walletList[i].perMonth.toDouble(),
                walletList[i].cifd.toDouble(),
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

}