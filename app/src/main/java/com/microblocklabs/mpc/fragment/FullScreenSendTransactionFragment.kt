package com.microblocklabs.mpc.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.databinding.FragmentFullScreenSendTransactionBinding
import com.microblocklabs.mpc.utility.CommonUtils

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullScreenSendTransactionFragment : DialogFragment() {

    private lateinit var binding: FragmentFullScreenSendTransactionBinding
    var sender: String = ""
    var receiver: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sender = arguments?.getString(SENDER_ADDRESS) ?: throw IllegalStateException("No args provided")
        receiver = arguments?.getString(RECEIVER_ADDRESS) ?: throw IllegalStateException("No args provided")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFullScreenSendTransactionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScreen(view)
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }

    private fun setupScreen(view: View) {
        binding.etSenderAccount.setText(sender)
        binding.etReceiverAccount.setText(receiver)

        binding.imgArrowBack.setOnClickListener {
            dismiss()
        }

        binding.buttonSend.setOnClickListener {
            CommonUtils.showToastMessage(requireContext(), "Work is under process")
        }

    }

    companion object {
        private const val SENDER_ADDRESS = "SENDER_ADDRESS"
        private const val RECEIVER_ADDRESS = "RECEIVER_ADDRESS"

        fun newInstance(
            senderAddress: String, receiverAddress: String
        ): FullScreenSendTransactionFragment = FullScreenSendTransactionFragment().apply {
            arguments = Bundle().apply {
                putString(SENDER_ADDRESS, senderAddress)
                putString(RECEIVER_ADDRESS, receiverAddress)
            }
        }
    }

}