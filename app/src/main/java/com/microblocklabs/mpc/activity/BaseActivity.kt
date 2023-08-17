package com.microblocklabs.mpc.activity

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import aot.armsproject.utils.AppSharedPreferenceManager
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.interceptor.GrpcClientRequestInterceptor
import com.microblocklabs.mpc.interceptor.GrpcClientResponseInterceptor
import com.microblocklabs.mpc.room.MPCDatabase
import dmax.dialog.SpotsDialog
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlin.properties.Delegates


open class BaseActivity : AppCompatActivity() {
    private var dialog: SpotsDialog by Delegates.notNull()
    lateinit var mpcSharedPref: AppSharedPreferenceManager
    lateinit var db: MPCDatabase


    val connectionChannel: ManagedChannel by lazy{

//        OkHttpChannelBuilder.forAddress("54.145.255.3", 50051) // Test
//            .usePlaintext()
//            .build()

        OkHttpChannelBuilder.forAddress("34.201.39.208", 50051) // Production
            .usePlaintext()
            .build()
    }

    val connectionChannelWithInterceptor  = ManagedChannelBuilder.forAddress("34.201.39.208", 50051)
//     val connectionChannelWithInterceptor  = ManagedChannelBuilder.forAddress("54.145.255.3", 50051)
        .usePlaintext()
        .build()!!

    val connectionChannelWithInterceptorForLogin  = ManagedChannelBuilder.forAddress("34.201.39.208", 50051)
//    val connectionChannelWithInterceptorForLogin  = ManagedChannelBuilder.forAddress("54.145.255.3", 50051)
        .intercept(GrpcClientResponseInterceptor())
        .usePlaintext()
        .build()!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this,R.color.black)
        dialog = SpotsDialog(this, R.style.LoadingDialog)
        dialog.setCancelable(false)
        db = MPCDatabase.getDatabase(this)!!
        mpcSharedPref = AppSharedPreferenceManager(this)
    }

    fun showLoadingDialog() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismissLoadingDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showWarningMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}