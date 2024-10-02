package project.alkautsar.simulasikredit.activity

import android.os.Bundle
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import project.alkautsar.simulasikredit.utils.Util
import project.alkautsar.simulasikredit.adapter.SimulasiCicilanAdapter
import project.alkautsar.simulasikredit.databinding.ActivityBungaTetapBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilanModel
import project.alkautsar.simulasikredit.utils.BaseUtils

class BungaTetapActivity : BaseUtils() {

    private lateinit var binding: ActivityBungaTetapBinding
    private lateinit var cicilanAdapter: SimulasiCicilanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using View Binding
        binding = ActivityBungaTetapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInterstitial()
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.BannerView.loadAd(adRequest)

        // Hide summary initially
        binding.llSummaryTetap.visibility = LinearLayout.GONE
        binding.etPinjamanTetap.addTextChangedListener(Util.NumberTextWatcher(binding.etPinjamanTetap))
        // Set up the button listeners
        binding.btnHitungTetap.setOnClickListener {
            showInterstitial()
            calculateFlatInterest()
        }

        binding.btnResetTetap.setOnClickListener {
            resetFields()
        }

        binding.toolbarBungaTetap.setNavigationOnClickListener {
            onBackPressed() // Handle back button press
        }

    }

    private fun calculateFlatInterest() {
        // Ambil nilai input dan validasi
        val pinjamanStr = binding.etPinjamanTetap.text.toString().replace(",", "")
        val bungaStr = binding.etBungaTetap.text.toString().replace("%", "").trim()
        val tenorStr = binding.etTenorTetap.text.toString()

        if (TextUtils.isEmpty(pinjamanStr) || TextUtils.isEmpty(bungaStr) || TextUtils.isEmpty(
                tenorStr
            )
        ) {
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

        // Hitung total bunga
        val totalBunga =
            pinjaman * (bunga / 100) * (tenor / 12.0) // Total bunga untuk seluruh tenor

        // Hitung angsuran pokok per bulan
        val angsuranPokok = pinjaman / tenor // Angsuran pokok per bulan
        val cicilanList = mutableListOf<SimulasiCicilanModel>()
        var totalDibayar = 0.0

        // Hitung cicilan per bulan
        for (bulan in 1..tenor) {
            val totalAngsuran = angsuranPokok + (totalBunga / tenor) // Total angsuran per bulan
            totalDibayar += totalAngsuran // Akumulasi total yang sudah dibayar
            val sisaCicilan = pinjaman - (angsuranPokok * bulan) // Hitung sisa cicilan

            cicilanList.add(
                SimulasiCicilanModel(
                    bulan,
                    angsuranPokok,
                    totalBunga / tenor,
                    totalAngsuran,
                    sisaCicilan
                )
            )
        }

        // Atur RecyclerView dengan data yang dihitung
        binding.rvCicilanTetap.layoutManager = LinearLayoutManager(this)
        cicilanAdapter = SimulasiCicilanAdapter(cicilanList)
        binding.rvCicilanTetap.adapter = cicilanAdapter

        // Tampilkan ringkasan
        binding.tvJumlahPinjamanTetap.text = "Rp ${String.format("%,.2f", pinjaman)}"
        binding.tvLamaPinjamanTetap.text = "$tenor Bulan"
        binding.tvBungaTetap.text = "${bungaStr}%"
        binding.tvAngsuranTetap.text =
            "Rp ${String.format("%,.2f", (pinjaman + totalBunga) / tenor)}"

        // Tampilkan bagian ringkasan
        binding.llSummaryTetap.visibility = LinearLayout.VISIBLE
    }


    private fun resetFields() {
        // Reset all input fields
        binding.etPinjamanTetap.setText("")
        binding.etBungaTetap.setText("")
        binding.etTenorTetap.setText("")
        binding.rvCicilanTetap.adapter = null
        // Hide the summary section again
        binding.llSummaryTetap.visibility = LinearLayout.GONE
    }
}
