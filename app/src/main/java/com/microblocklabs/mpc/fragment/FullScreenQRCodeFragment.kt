package com.microblocklabs.mpc.fragment

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context.WINDOW_SERVICE
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.utility.CommonUtils

class FullScreenQRCodeFragment() : DialogFragment() {
    lateinit var bitmap: Bitmap
//    lateinit var qrEncoder: QRGEncoder
    var items: String = ""

    private lateinit var imgBack: ImageView
    private lateinit var imgQRCode: ImageView
    private lateinit var imgCopyAddress: ImageView
    lateinit var walletAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = arguments?.getString(ITEMS) ?: throw IllegalStateException("No args provided")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isCancelable = false
        return inflater.inflate(R.layout.layout_full_screen_qr_code_dialog, container, false)
    }
    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScreen(view)

//        button.setOnClickListener {
//            //send back data to PARENT fragment using callback
//            callbackListener.onDataReceived(editText.text.toString())
//            // Now dismiss the fragment
//            dismiss()
//        }

    }

    private fun setupScreen(view: View) {
        imgBack = view.findViewById(R.id.img_arrow_back)
        imgQRCode = view.findViewById(R.id.img_qr_code_recieve)
        imgCopyAddress = view.findViewById(R.id.img_copy_wallet_address)
        walletAddress = view.findViewById(R.id.wallet_address)
        imgQRCode = view.findViewById(R.id.img_qr_code_recieve)
        val bitmap = generateQRCode(items.trim())
        walletAddress.text = items.trim()
        imgQRCode.setImageBitmap(bitmap)

        imgBack.setOnClickListener {
            dismiss()
        }

        imgCopyAddress.setOnClickListener {
            CommonUtils.copyToClipBoard(requireContext(), items)
        }
        walletAddress.setOnClickListener {
            CommonUtils.copyToClipBoard(requireContext(), items)
        }
    }


    private fun generateQRCode(text: String): Bitmap {
        val width = 250
        val height = 250
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix =
                codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    bitmap.setPixel(x, y, color)
                }
            }
        } catch (e: WriterException) {

            Log.d(TAG, "generateQRCode: ${e.message}")

        }
        return bitmap
    }


    companion object {

        private const val ITEMS = "items"

        fun newInstance(
            items: String
        ): FullScreenQRCodeFragment = FullScreenQRCodeFragment().apply {
            arguments = Bundle().apply {
                putString(ITEMS, items)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme){
            override fun onBackPressed() {
                super.onBackPressed()
                dismiss()
            }

        }
    }
}