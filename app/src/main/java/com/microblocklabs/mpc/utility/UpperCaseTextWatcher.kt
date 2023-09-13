package com.microblocklabs.mpc.utility

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.Locale

class UpperCaseTextWatcher(private val editText: EditText) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not needed for this implementation
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Convert the entered text to uppercase
        val upperCaseText = s?.toString()?.toUpperCase(Locale.ROOT)
        // Remove the TextWatcher temporarily to avoid infinite loop
        editText.removeTextChangedListener(this)
        // Update the EditText text
        editText.setText(upperCaseText)
        // Set the cursor position to the end
        editText.setSelection(upperCaseText?.length ?: 0)
        // Reattach the TextWatcher
        editText.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {
        // Not needed for this implementation
    }
}