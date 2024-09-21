package project.alkautsar.simulasikredit

import java.text.NumberFormat
import java.util.Locale

class Util {

    fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(number).replace(",00", "")
    }
}