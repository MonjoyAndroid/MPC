package com.microblocklabs.mpc.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.interfaces.IAlertDialogButtonClickListener
import dmax.dialog.BuildConfig
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object CommonUtils {
    private val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "^" +
                "(?=.*\\d{1})"+ // at least 1 numeric character
                "(?=.*[a-z]{1})(?=.*[A-Z]{1})"+ // at least 1 lower and 1 upper character
                "(?=.*[!@#\$%^&*{|}?~_=+.-]{1})" +  // at least 1 special character
                "(?=\\S+$)" +  // no white spaces
                ".{8,}" +  // at least 8 characters
                "$"
    )

    fun goToNextScreenWithFinish(cls: Class<*>?, ct: Context) {
        val intent = Intent(ct, cls)
        ct.startActivity(intent)
        (ct as Activity).finish()
    }

    fun isEmailValid(email: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValidate(passwordInput: String?): Boolean {
        return PASSWORD_PATTERN.matcher(passwordInput.toString()).matches()
    }

    fun isOTPValid(otp: String?): Boolean {
        return otp!!.length == 4
    }

    fun isValidEthereumAddress(address: String): Boolean {
        // Ethereum address should be 40 characters long and start with '0x'
        val ethereumAddressRegex = "^0x[a-fA-F0-9]{40}$"
        return address.matches(ethereumAddressRegex.toRegex())
    }

    fun isPositiveBigInteger(value: BigInteger): Boolean {
        return value > BigInteger.ZERO
    }

    fun isWholeNumber(number: Double): Boolean {
        return number % 1 == 0.0
    }

    fun convertFractionalValueToBigIntegerFormat(fractionVal: String): BigInteger {
        return Convert.toWei(fractionVal, Convert.Unit.ETHER).toBigInteger()
    }

    fun convertBigIntegerValueToFractionalFormat(bigIntegerVal: String): String {
        return Convert.fromWei(bigIntegerVal, Convert.Unit.ETHER).toPlainString()
    }

    //Used to avoid multiple click within 1 second interval
    fun isButtonActiveForClick(lastClickTime: Long): Boolean {
        val debounceInterval = 1000L // Set the desired interval in milliseconds
        val currentTime = System.currentTimeMillis()
        return currentTime - lastClickTime >= debounceInterval
    }

    fun goToNextScreenWithoutFinish(cls: Class<*>?, ct: Context) {
        val intent = Intent(ct, cls)
        ct.startActivity(intent)
    }

    fun changeStatusBarColor(statusBarColor: Int, myActivityReference: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = myActivityReference.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
        }
    }

    fun navigateAndClearBackStack(cls: Class<*>?, ct: Context) {
        val intent = Intent(ct, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        ct.startActivity(intent)
        (ct as Activity).finish()
    }

    fun showSnackBar(view: View?, msg: String?) {
        //findViewById(android.R.id.content)
        Snackbar.make(view!!, msg!!, Snackbar.LENGTH_SHORT)
            .show()
    }

    fun setVisibility(visible: View, gone: View) {
        visible.visibility = View.VISIBLE
        gone.visibility = View.GONE
    }

    fun showToastMessage(context: Context,message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun copyToClipBoard(context: Context, typeOfText: String, textToCopy: String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copy To Clipboard",textToCopy)
        clipboard.setPrimaryClip(clip)
        showToastMessage(context, "$typeOfText ${context.resources.getString(R.string.copy_to_clip)}")
    }

    fun roundToDecimalPlaces(balValStr: String, decimalPlaces: Int): BigDecimal {
        val parsedValue = balValStr.toBigDecimal()
        val pattern = "#." + "0".repeat(decimalPlaces)
        val decimalFormat = DecimalFormat(pattern)
        decimalFormat.roundingMode = RoundingMode.HALF_UP
        return decimalFormat.format(parsedValue).toBigDecimal()
    }

    fun convertDecimalUptoFourDigits(decimalVal: Double): Double{
        val df = DecimalFormat("#.####")
        df.roundingMode = RoundingMode.CEILING
        val roundoff = df.format(decimalVal.toDouble())
        return roundoff.toDouble()
    }

    fun convertDecimalUptoSixDigits(decimalVal: Double): Double{
        val df = DecimalFormat("#.######")
        df.roundingMode = RoundingMode.DOWN
        val roundoff = df.format(decimalVal.toDouble())
        return roundoff.toDouble()
    }

    fun getAppName(context: Context): String? {
        var applicationInfo: ApplicationInfo? = null
        try {
            applicationInfo = context.packageManager.getApplicationInfo(context.applicationInfo.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("TAG", "The package with the given name cannot be found on the system.")
        }
        return (if (applicationInfo != null) context.packageManager.getApplicationLabel(applicationInfo).toString() else "Unknown")
    }

    fun getAppVersionName(context: Context): String? {
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = context.packageManager.getPackageInfo(context.applicationInfo.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("TAG", "The package with the given name cannot be found on the system.")
        }
        return (if (packageInfo != null) packageInfo.versionName else "Unknown")
        }

    fun getCurrentDate(format: String?): String? {
        val c31 = Calendar.getInstance()
        val sdf31 = SimpleDateFormat(format, Locale.ENGLISH)
        return sdf31.format(c31.time)
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeSimple(date: Date?): String? {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        return dateFormat.format(date!!)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDateFormat(inputDateString: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)", Locale.ENGLISH) // Input format
        val outputFormat = SimpleDateFormat("dd MMM yyyy h:mm:ss a", Locale.ENGLISH) // Desired output format

        val date: Date = inputFormat.parse(inputDateString) as Date
        return outputFormat.format(date)
    }

    fun alertDialog(context: Context?, msg: String?) {
        var message = msg
        message = message?.replace("\r".toRegex(), "\n") ?: ""
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_alert_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val text = dialog.findViewById<TextView>(R.id.txtDisplayName)
        text.text = message
        val okButton = dialog.findViewById<TextView>(R.id.button_ok)
        okButton.setOnClickListener { dialog.dismiss() }
    }

    fun alertDialogWithErrorMsg(context: Context?, msg: String?) {
        var message = msg
        message = message?.replace("\r".toRegex(), "\n") ?: ""
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_alert_dialog_with_error_msg)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val text = dialog.findViewById<TextView>(R.id.txtDisplayName)
        text.text = message
        val okButton = dialog.findViewById<TextView>(R.id.button_ok)
        okButton.setOnClickListener { dialog.dismiss() }
    }

    fun functionalAlertDialog(context: Context?, callingPurpose:String?, msg: String?, iAlertDialogButtonClickListener: IAlertDialogButtonClickListener) {
        var message = msg
        message = message?.replace("\r".toRegex(), "\n") ?: ""
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_alert_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val text = dialog.findViewById<TextView>(R.id.txtDisplayName)
        text.text = message
        val okButton = dialog.findViewById<TextView>(R.id.button_ok)
        okButton.setOnClickListener {
            dialog.dismiss()
            iAlertDialogButtonClickListener.onPositiveButtonClick(callingPurpose)
        }
    }

    fun functionalAlertDialogWithTwoButton(context: Context?, callingPurpose:String?, titleStr :String?, msg: String?, iAlertDialogButtonClickListener: IAlertDialogButtonClickListener) {
        var message = msg
        message = message?.replace("\r".toRegex(), "\n") ?: ""
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_alert_with_two_button)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val title = dialog.findViewById<TextView>(R.id.title)
        title.text = titleStr
        val text = dialog.findViewById<TextView>(R.id.txtDisplayName)
        text.text = message
        val noButton = dialog.findViewById<TextView>(R.id.button_no)
        noButton.setOnClickListener { dialog.dismiss() }
        val yesButton = dialog.findViewById<TextView>(R.id.button_yes)
        yesButton.setOnClickListener {
            dialog.dismiss()
            iAlertDialogButtonClickListener.onPositiveButtonClick(callingPurpose)
        }
    }


}