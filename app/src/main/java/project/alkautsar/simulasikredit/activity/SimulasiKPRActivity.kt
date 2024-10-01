package project.alkautsar.simulasikredit.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import project.alkautsar.simulasikredit.adapter.SimulasiCicilanAdapter
import project.alkautsar.simulasikredit.databinding.ActivitySimulasiKpractivityBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilanModel
import project.alkautsar.simulasikredit.utils.BaseUtils
import project.alkautsar.simulasikredit.utils.Util

class SimulasiKPRActivity : BaseUtils() {

    private lateinit var binding: ActivitySimulasiKpractivityBinding
    private lateinit var cicilanAdapter: SimulasiCicilanAdapter
    private val cicilanList = mutableListOf<SimulasiCicilanModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulasiKpractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showInterstitial()
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.BannerView.loadAd(adRequest)

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

        // Set default visibility of RecyclerView and summary to GONE
        binding.rvCicilanKPR.visibility = View.GONE
        binding.llSummaryKpr.visibility = View.GONE

        // Hitung Button Listener
        binding.btnHitungKpr.setOnClickListener {
            if (validateInputs()) {
                showInterstitial()
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
        // Parsing input values
        val hargaRumah = binding.etHargaRumah.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val dpPersen = binding.etDp.text.toString().toDouble() / 100
        val tenorTahun = binding.etTenorKpr.text.toString().toInt()
        val bungaTahun = binding.etBungaPinjaman.text.toString().toDouble() / 100
        val biayaProvisiPersen = binding.etBiayaProvisi.text.toString().toDouble() / 100 // Biaya Provisi dalam persen
        val biayaAppraisal = binding.etBiayaAppraisal.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val biayaAdministrasi = binding.etBiayaAdministrasi.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val biayaNotaris = binding.etBiayaNotaris.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val biayaAsuransi = binding.etBiayaAsuransi.text.toString().replace("[^\\d]".toRegex(), "").toDouble()
        val njoptkp = binding.etNjoptkp.text.toString().replace("[^\\d]".toRegex(), "").toDouble()

        val uangMuka = hargaRumah * dpPersen
        val jumlahPinjaman = hargaRumah - uangMuka
        val bungaBulanan = bungaTahun / 12
        val tenorBulan = tenorTahun * 12

        // Hitung Biaya Provisi berdasarkan persentase dan plafon pinjaman
        val biayaProvisi = biayaProvisiPersen * jumlahPinjaman

        // Perhitungan angsuran bulanan dengan metode anuitas
        val angsuranBulanan = (jumlahPinjaman * bungaBulanan) /
                (1 - Math.pow(1 + bungaBulanan, -tenorBulan.toDouble()))

        // Simpan hasil ke dalam adapter
        cicilanList.clear()
        var sisaCicilan = jumlahPinjaman
        var totalInterestPaid = 0.0

        for (bulan in 1..tenorBulan) {
            val bunga = sisaCicilan * bungaBulanan
            var cicilanPokok = angsuranBulanan - bunga

            // Pastikan cicilan pokok tidak lebih besar dari sisa cicilan
            if (cicilanPokok > sisaCicilan) {
                cicilanPokok = sisaCicilan // Sesuaikan cicilan pokok jika lebih dari sisa cicilan
            }

            // Update sisa cicilan
            sisaCicilan -= cicilanPokok
            totalInterestPaid += bunga

            // Tambahkan ke dalam daftar cicilan
            cicilanList.add(SimulasiCicilanModel(bulan, cicilanPokok, bunga, angsuranBulanan, sisaCicilan))
        }

        // Hitung Pajak Pembeli (BPHTB) = 5% x (Harga Rumah - NJOPTKP/NPTKP)
        val pajakPembeli = 0.05 * (hargaRumah - njoptkp)

        // Hitung total biaya
        val totalPayment = angsuranBulanan * tenorBulan
        val totalCost = totalPayment + biayaAppraisal + biayaAdministrasi + biayaNotaris + biayaAsuransi + pajakPembeli + biayaProvisi

        // Update RecyclerView visibility dan data
        cicilanAdapter.notifyDataSetChanged()
        binding.rvCicilanKPR.visibility = View.VISIBLE
        binding.llSummaryKpr.visibility = View.VISIBLE

        // Menampilkan summary
        binding.tvHargaRumahKpr.text = Util().formatRupiah(hargaRumah) // Harga Rumah
        binding.tvDpKpr.text = Util().formatRupiah(uangMuka) // Uang Muka
        binding.tvPlafonPinjamanKpr.text = Util().formatRupiah(jumlahPinjaman) // Plafon Pinjaman
        binding.tvBungaPinjamanKpr.text = "${bungaTahun * 100}%" // Bunga Pinjaman
        binding.tvTenorKpr.text = "$tenorBulan Bulan" // Tenor
        binding.tvBiayaProvisiKpr.text = Util().formatRupiah(biayaProvisi) // Biaya Provisi
        binding.tvBiayaAppraisalKpr.text = Util().formatRupiah(biayaAppraisal) // Biaya Appraisal
        binding.tvBiayaAdministrasiKpr.text = Util().formatRupiah(biayaAdministrasi) // Biaya Administrasi
        binding.tvBiayaNotarisKpr.text = Util().formatRupiah(biayaNotaris) // Biaya Notaris
        binding.tvBiayaAsuransiKpr.text = Util().formatRupiah(biayaAsuransi) // Biaya Asuransi
        binding.tvPajakPembeliKpr.text = Util().formatRupiah(pajakPembeli) // Pajak Pembeli

        // Perhitungan total biaya pajak
        val totalBiayaPajak = biayaAppraisal + biayaAdministrasi + biayaNotaris + biayaAsuransi + pajakPembeli + biayaProvisi
        binding.tvTotalBiayaPajakKpr.text = Util().formatRupiah(totalBiayaPajak) // Total Biaya Pajak

        // Menampilkan total pembayaran termasuk uang muka dan total biaya pajak
        val totalPembayaranPertama = uangMuka + totalBiayaPajak
        binding.tvTotalPembayaranKpr.text = Util().formatRupiah(totalPembayaranPertama) // Total Pembayaran Pertama
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