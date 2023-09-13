package com.microblocklabs.mpc.utility

import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.Spanned
import java.util.regex.Pattern

class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
    var mPattern: Pattern

    init {
        mPattern =
            Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = SpannableStringBuilder(dest).replace(dstart, dend, source, start, end)
        val inputValue = input.toString()

        if (inputValue.isEmpty()) {
            return null
        }

        val decimalIndex = inputValue.indexOf('.')
        if (decimalIndex == -1) {
            if (inputValue.length > 15) {
                return ""
            }
        } else {
            val integerPart = inputValue.substring(0, decimalIndex)
            val decimalPart = inputValue.substring(decimalIndex + 1)

            if (integerPart.length > 15 || decimalPart.length > 6 || inputValue.length > 16) {
                return ""
            }

            if (integerPart.isNotEmpty() && integerPart.length > 9) {
                return ""
            }
        }

        return null
    }
}