package project.alkautsar.simulasikredit

import android.os.Bundle
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import project.alkautsar.simulasikredit.databinding.ActivityBungaEfektifBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilan

class BungaEfektifActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBungaEfektifBinding
    private lateinit var cicilanAdapter: SimulasiCicilanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout using View Binding
        binding = ActivityBungaEfektifBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide summary initially
        binding.llSummaryEfektif.visibility = LinearLayout.GONE
        binding.etPinjamanEfektif.addTextChangedListener(Util.NumberTextWatcher(binding.etPinjamanEfektif))
        // Set up the button listeners
        binding.btnHitungEfektif.setOnClickListener { calculateEffectiveInterest() }
        binding.btnResetEfektif.setOnClickListener { resetFields() }
    }

    private fun calculateEffectiveInterest() {
        // Ambil nilai input dan validasi
        val pinjamanStr = binding.etPinjamanEfektif.text.toString().replace(",", "")
        val bungaStr = binding.etBungaEfektif.text.toString().replace("%", "").trim()
        val tenorStr = binding.etTenorEfektif.text.toString()

        if (TextUtils.isEmpty(pinjamanStr) || TextUtils.isEmpty(bungaStr) || TextUtils.isEmpty(tenorStr)) {
            Toast.makeText(this, "Harap masukkan semua data", Toast.LENGTH_SHORT).show()
            return
        }

        // Konversi input ke angka
        val pinjaman = pinjamanStr.toDoubleOrNull()
        val bunga = bungaStr.toDoubleOrNull() // Bunga dalam persen per tahun
        val tenor = tenorStr.toIntOrNull() // Tenor dalam bulan

        if (pinjaman == null || bunga == null || tenor == null || tenor <= 0) {
            Toast.makeText(this, "Harap masukkan data yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Hitung total bunga efektif
        val bungaEfektif = (1 + (bunga / 100)) pow tenor.toDouble() - 1 // Bunga efektif
        val totalBunga = pinjaman * bungaEfektif // Total bunga untuk seluruh tenor

        // Hitung angsuran pokok per bulan
        val angsuranPokok = pinjaman / tenor // Angsuran pokok per bulan
        val cicilanList = mutableListOf<SimulasiCicilan>()
        var totalDibayar = 0.0

        // Hitung cicilan per bulan
        for (bulan in 1..tenor) {
            val totalAngsuran = angsuranPokok + (totalBunga / tenor) // Total angsuran per bulan
            totalDibayar += totalAngsuran // Akumulasi total yang sudah dibayar
            val sisaCicilan = pinjaman - (angsuranPokok * bulan) // Hitung sisa cicilan

            cicilanList.add(SimulasiCicilan(bulan, angsuranPokok, totalBunga / tenor, totalAngsuran, sisaCicilan))
        }

        // Atur RecyclerView dengan data yang dihitung
        binding.rvCicilanEfektif.layoutManager = LinearLayoutManager(this)
        cicilanAdapter = SimulasiCicilanAdapter(cicilanList)
        binding.rvCicilanEfektif.adapter = cicilanAdapter

        // Tampilkan ringkasan
        binding.tvJumlahPinjamanEfektif.text = "Rp ${String.format("%,.2f", pinjaman)}"
        binding.tvLamaPinjamanEfektif.text = "$tenor Bulan"
        binding.tvBungaEfektif.text = "${bungaStr}%"
        binding.tvTotalBayarEfektif.text = "Rp ${String.format("%,.2f", (pinjaman + totalBunga) / tenor)}"

        // Tampilkan bagian ringkasan
        binding.llSummaryEfektif.visibility = LinearLayout.VISIBLE
    }

    private fun resetFields() {
        // Reset all input fields
        binding.etPinjamanEfektif.setText("")
        binding.etBungaEfektif.setText("")
        binding.etTenorEfektif.setText("")
        binding.rvCicilanEfektif.adapter = null
        // Hide the summary section again
        binding.llSummaryEfektif.visibility = LinearLayout.GONE
    }

    // Extension function to perform exponentiation
    private infix fun Double.pow(exponent: Double): Double {
        return Math.pow(this, exponent)
    }
}