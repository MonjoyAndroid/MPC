package com.microblocklabs.mpc.utility

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.microblocklabs.mpc.R
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

    fun copyToClipBoard(context: Context, textToCopy: String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copy To Clipboard",textToCopy)
        clipboard.setPrimaryClip(clip)
        showToastMessage(context, context.resources.getString(R.string.copy_to_clip))
    }

}