package com.microblocklabs.mpc.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.activity.HomeScreenActivity
import com.microblocklabs.mpc.databinding.FragmentFullScreenSendTransactionBinding
import com.microblocklabs.mpc.utility.CommonUtils
import com.microblocklabs.mpc.utility.DecimalDigitsInputFilter
import com.microblocklabs.mpc.utility.GSONConverter
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.utils.Convert
import java.math.BigDecimal

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullScreenSendTransactionFragment : DialogFragment() {

    private lateinit var binding: FragmentFullScreenSendTransactionBinding
    private lateinit var myActivity: HomeScreenActivity
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
        myActivity = activity as HomeScreenActivity
        setupScreen(view)
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }

    private fun setupScreen(view: View) {
        binding.etSenderAccount.setText(sender)
        binding.etReceiverAccount.setText(receiver)
        val filters = arrayOf<DecimalDigitsInputFilter>(DecimalDigitsInputFilter(10,6))
        binding.etSentAmount.filters = filters

        binding.etReceiverAccount.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etReceiverAccount.background = requireContext().resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

        binding.etSentAmount.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etSentAmount.background = requireContext().resources.getDrawable(R.drawable.bg_border_grey)
            }
        })

        binding.imgArrowBack.setOnClickListener {
            dismiss()
        }

        binding.buttonSend.setOnClickListener {
            val senderAddress = binding.etSenderAccount.text.toString()
            val receiverAddress = binding.etReceiverAccount.text.toString()
            val amount = binding.etSentAmount.text.toString()
            checkForValidation(senderAddress, receiverAddress, amount)
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkForValidation(senderAccount: String, receiverAccount: String, tokenAmt: String){
        if(receiverAccount.isEmpty() or tokenAmt.isEmpty()) {
            if (receiverAccount.isEmpty() && tokenAmt.isEmpty()) {
                binding.etReceiverAccount.background =
                    requireContext().resources.getDrawable(R.drawable.bg_border_red)
                binding.etSentAmount.background =
                    requireContext().resources.getDrawable(R.drawable.bg_border_red)
            } else {
                if (receiverAccount.isEmpty()) {
                    binding.etReceiverAccount.background =
                        requireContext().resources.getDrawable(R.drawable.bg_border_red)
                    binding.etSentAmount.background =
                        requireContext().resources.getDrawable(R.drawable.bg_border_grey)
                } else if (tokenAmt.isEmpty()) {
                    binding.etReceiverAccount.background =
                        requireContext().resources.getDrawable(R.drawable.bg_border_grey)
                    binding.etSentAmount.background =
                        requireContext().resources.getDrawable(R.drawable.bg_border_red)
                }
            }
            return
        } else if(!CommonUtils.isValidEthereumAddress(receiverAccount)){
            binding.etReceiverAccount.background =
                requireContext().resources.getDrawable(R.drawable.bg_border_red)
            CommonUtils.alertDialog(requireContext(), requireActivity().resources.getString(R.string.your_receiver_address_wrong))
            return
        } else if(senderAccount == receiverAccount){
            binding.etReceiverAccount.background =
                requireContext().resources.getDrawable(R.drawable.bg_border_red)
            CommonUtils.alertDialog(requireContext(), requireActivity().resources.getString(R.string.you_cant_transfer_own_account))
            return
        }else{
            val etherBalance = Convert.toWei(tokenAmt, Convert.Unit.ETHER).toBigInteger()
            if(etherBalance.toInt()> 0){
                myActivity.callTransaction(senderAccount, receiverAccount, etherBalance.toString())
            }else{
                binding.etSentAmount.background =
                    requireContext().resources.getDrawable(R.drawable.bg_border_red)
                CommonUtils.alertDialog(requireContext(), requireActivity().resources.getString(R.string.you_cant_transfer_zero_value))
                return
            }

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