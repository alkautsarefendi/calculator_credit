package project.alkautsar.simulasikredit.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import project.alkautsar.simulasikredit.adapter.SimulasiCicilanAdapter
import project.alkautsar.simulasikredit.databinding.ActivitySimulasiKpractivityBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilanModel
import project.alkautsar.simulasikredit.utils.Util

class SimulasiKPRActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimulasiKpractivityBinding
    private lateinit var cicilanAdapter: SimulasiCicilanAdapter
    private val cicilanList = mutableListOf<SimulasiCicilanModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulasiKpractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbarSimulasiKpr)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup RecyclerView
        cicilanAdapter = SimulasiCicilanAdapter(cicilanList)
        binding.rvCicilanKPR.apply {
            layoutManager = LinearLayoutManager(this@SimulasiKPRActivity)
            adapter = cicilanAdapter
        }

        setupTextWatchers()

        // Set default visibility of RecyclerView to GONE
        binding.rvCicilanKPR.visibility = View.GONE
        binding.llSummaryKpr.visibility = View.GONE

        // Hitung Button Listener
        binding.btnHitungKpr.setOnClickListener {
            if (validateInputs()) {
                calculateKPR()
            }
        }

        // Reset Button Listener
        binding.btnResetKpr.setOnClickListener {
            resetFields()
        }
    }

    private fun setupTextWatchers() {
        binding.etHargaRumah.addTextChangedListener(Util.NumberTextWatcher(binding.etHargaRumah))
        binding.etDp.addTextChangedListener(Util.NumberTextWatcher(binding.etDp))
        binding.etBiayaAppraisal.addTextChangedListener(Util.NumberTextWatcher(binding.etBiayaAppraisal))
        binding.etBiayaAdministrasi.addTextChangedListener(Util.NumberTextWatcher(binding.etBiayaAdministrasi))
        binding.etBiayaNotaris.addTextChangedListener(Util.NumberTextWatcher(binding.etBiayaNotaris))
        binding.etBiayaAsuransi.addTextChangedListener(Util.NumberTextWatcher(binding.etBiayaAsuransi))
        binding.etNjoptkp.addTextChangedListener(Util.NumberTextWatcher(binding.etNjoptkp))
    }

    private fun validateInputs(): Boolean {
        // Validate if inputs are not empty
        return binding.etHargaRumah.text.isNotEmpty() &&
                binding.etDp.text.isNotEmpty() &&
                binding.etTenorKpr.text.isNotEmpty() &&
                binding.etBungaPinjaman.text.isNotEmpty() &&
                binding.etBiayaProvisi.text.isNotEmpty() &&
                binding.etBiayaAppraisal.text.isNotEmpty() &&
                binding.etBiayaAdministrasi.text.isNotEmpty() &&
                binding.etBiayaNotaris.text.isNotEmpty() &&
                binding.etBiayaAsuransi.text.isNotEmpty() &&
                binding.etNjoptkp.text.isNotEmpty()
    }

    private fun calculateKPR() {
        // Get input values, removing any formatting
        val hargaRumah = binding.etHargaRumah.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val dpPersen = binding.etDp.text.toString().toDouble() / 100
        val tenorTahun = binding.etTenorKpr.text.toString().toInt()
        val bungaTahun = binding.etBungaPinjaman.text.toString().toDouble() / 100
        val biayaProvisi = binding.etBiayaProvisi.text.toString().toDouble() / 100
        val biayaAppraisal = binding.etBiayaAppraisal.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val biayaAdministrasi = binding.etBiayaAdministrasi.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val biayaNotaris = binding.etBiayaNotaris.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val biayaAsuransi = binding.etBiayaAsuransi.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val njoptkp = binding.etNjoptkp.text.toString().replace("[^\\d]".toRegex(), "").toDouble()

        // Kalkulasi pinjaman
        val uangMuka = hargaRumah * dpPersen
        val jumlahPinjaman = hargaRumah - uangMuka
        val bungaBulanan = bungaTahun / 12
        val tenorBulan = tenorTahun * 12

        // Perhitungan angsuran bulanan dengan metode bunga tetap
        val angsuranBulanan = (jumlahPinjaman * bungaBulanan) /
                (1 - Math.pow(1 + bungaBulanan, -tenorBulan.toDouble()))

        // Simpan hasil ke dalam adapter
        cicilanList.clear()
        var sisaCicilan = jumlahPinjaman

        for (bulan in 1..tenorBulan) {
            val bunga = sisaCicilan * bungaBulanan
            val cicilanPokok = angsuranBulanan - bunga
            sisaCicilan -= cicilanPokok

            cicilanList.add(SimulasiCicilanModel(bulan, cicilanPokok, bunga, angsuranBulanan, sisaCicilan))
        }

        // Update RecyclerView visibility dan data
        cicilanAdapter.notifyDataSetChanged()
        binding.rvCicilanKPR.visibility = View.VISIBLE
        binding.llSummaryKpr.visibility = View.VISIBLE

        // Menampilkan summary
        binding.tvJumlahPinjamanKpr.text = Util().formatRupiah(jumlahPinjaman)
        binding.tvLamaPinjamanKpr.text = "$tenorBulan Bulan"
        binding.tvBungaKpr.text = "${bungaTahun * 100}%"
        binding.tvAngsuranKpr.text = Util().formatRupiah(angsuranBulanan)
    }

    private fun resetFields() {
        // Reset all input fields
        binding.etHargaRumah.text.clear()
        binding.etDp.text.clear()
        binding.etTenorKpr.text.clear()
        binding.etBungaPinjaman.text.clear()
        binding.etBiayaProvisi.text.clear()
        binding.etBiayaAppraisal.text.clear()
        binding.etBiayaAdministrasi.text.clear()
        binding.etBiayaNotaris.text.clear()
        binding.etBiayaAsuransi.text.clear()
        binding.etNjoptkp.text.clear()

        // Reset RecyclerView and Summary
        binding.rvCicilanKPR.visibility = View.GONE
        binding.llSummaryKpr.visibility = View.GONE
        cicilanList.clear()
        cicilanAdapter.notifyDataSetChanged()
    }
}