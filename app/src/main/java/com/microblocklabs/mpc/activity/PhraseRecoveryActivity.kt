package com.microblocklabs.mpc.activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import signup.SignUpServiceGrpc
import signup.Signup
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
            val phrase = phraseEditText.text.toString()
            enteredPhraseList.add(phrase)
        }

        val nMonicString = convertListToSpaceSeparatedString(enteredPhraseList)
        getUserDetailsByMnemonicPhrase(nMonicString)
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
//        finish()
    }

    private fun showLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finishAffinity()
    }

    private fun prepareDataForShowPhrase(){
        val isEditMode = false
        binding.titleSecretRecovery.text = resources.getString(R.string.secret_recovery)
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
        binding.titleSecretRecovery.text = resources.getString(R.string.enter_phrase_title)
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

//        val signUpService = SignUpServiceGrpc.newBlockingStub(connectionChannel)
//
//        val requestMessage = Signup.SignUpRequest.newBuilder()
//            .setEmail(email)
//            .setPhoneNumber(phone)
//            .setPassword(password)
//            .setUniqueId(uniqueID)
//            .build()
//
//        showLoadingDialog()
//
//        Single.fromCallable { signUpService.signUpUser(requestMessage) }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : SingleObserver<Signup.SignUpResponse> {
//                override fun onSuccess(response: Signup.SignUpResponse) {
//                    dismissLoadingDialog()
//
//                    Log.d("MyResponse", response.toString())
//
//                    saveUserData(response)
//                }
//
//                override fun onSubscribe(d: Disposable) {}
//
//                override fun onError(e: Throwable) {
//                    dismissLoadingDialog()
//                    showErrorMessage("Error:  ${e.message}")
//                }
//            })
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

        when(openPurposeVal) {
            2 -> showChangePasswordScreen()
            3 -> showLoginScreen()
        }
    }

}