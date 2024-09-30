package project.alkautsar.simulasikredit.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Locale

class Util {

    fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        formatRupiah.maximumFractionDigits = 0  // Hilangkan digit setelah koma
        return formatRupiah.format(number)
    }

    class NumberTextWatcher(private val editText: EditText) : TextWatcher {

        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No action needed
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No action needed
        }

        override fun afterTextChanged(s: Editable?) {
            if (s.toString() != current) {
                // Remove the listener to prevent infinite loop
                editText.removeTextChangedListener(this)

                // Clean the input
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                val parsed = cleanString.toLongOrNull()

                // Format the number
                current = if (parsed != null) {
                    NumberFormat.getNumberInstance(Locale.US).format(parsed)
                } else {
                    ""
                }

                // Set the formatted text back to EditText
                editText.setText(current)
                editText.setSelection(current.length)

                // Reattach the listener
                editText.addTextChangedListener(this)
            }
        }
    }





}