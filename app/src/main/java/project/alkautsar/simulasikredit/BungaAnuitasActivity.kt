package project.alkautsar.simulasikredit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import project.alkautsar.simulasikredit.databinding.ActivityBungaAnuitasBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilan

class BungaAnuitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBungaAnuitasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityBungaAnuitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView LayoutManager
        binding.rvTabelSimulasiAnuitas.layoutManager = LinearLayoutManager(this)
        binding.etPinjamanAnuitas.addTextChangedListener(Util.NumberTextWatcher(binding.etPinjamanAnuitas))
        binding.etBungaAnuitas.addTextChangedListener(Util.InterestRateTextWatcher(binding.etBungaAnuitas))

        // Button click listener
        binding.btnHitungAnuitas.setOnClickListener {
            // Get pinjaman value after formatting
            val pinjamanString = binding.etPinjamanAnuitas.text.toString().replace("[^\\d]".toRegex(), "")
            val pinjaman = pinjamanString.toDoubleOrNull() ?: 0.0

            // Get bunga value after formatting
            val bungaString = binding.etBungaAnuitas.text.toString().replace("%", "").trim()
            val bunga = bungaString.toDoubleOrNull() ?: 0.0

            // Get tenor value
            val tenor = binding.etTenorAnuitas.text.toString().toIntOrNull() ?: 0

            // Log the values for debugging
            Log.d("DEBUG", "Pinjaman: $pinjaman, Bunga: $bunga, Tenor: $tenor")

            if (pinjaman > 0 && bunga > 0 && tenor > 0) {
                val simulasiList = hitungCicilanAnuitas(pinjaman, bunga, tenor)
                binding.rvTabelSimulasiAnuitas.adapter = SimulasiCicilanAdapter(simulasiList)
            } else {
                Log.e("ERROR", "Invalid input values: Pinjaman: $pinjaman, Bunga: $bunga, Tenor: $tenor")
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back button press
        }

        // Reset button click listener
        binding.btnReset.setOnClickListener {
            resetFields()
        }

    }

    private fun hitungCicilanAnuitas(pinjaman: Double, bunga: Double, tenor: Int): List<SimulasiCicilan> {
        val simulasiList = mutableListOf<SimulasiCicilan>()
        val bungaBulanan = bunga / 100 / 12
        val cicilanPerBulan = pinjaman * bungaBulanan / (1 - Math.pow(1 + bungaBulanan, -tenor.toDouble()))

        var saldoPinjaman = pinjaman
        for (bulan in 1..tenor) {
            val bungaBulanIni = saldoPinjaman * bungaBulanan
            val pokokBulanIni = cicilanPerBulan - bungaBulanIni
            simulasiList.add(SimulasiCicilan(bulan, pokokBulanIni, bungaBulanIni, cicilanPerBulan))
            saldoPinjaman -= pokokBulanIni
        }

        return simulasiList
    }

    private fun resetFields() {
        // Clear input fields
        binding.etPinjamanAnuitas.text.clear()
        binding.etBungaAnuitas.text.clear()
        binding.etTenorAnuitas.text.clear()

        // Clear RecyclerView adapter
        binding.rvTabelSimulasiAnuitas.adapter = null
    }


}
