package project.alkautsar.simulasikredit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        // Button click listener
        binding.btnHitungAnuitas.setOnClickListener {
            val pinjaman = binding.etPinjamanAnuitas.text.toString().toDoubleOrNull() ?: 0.0
            val bunga = binding.etBungaAnuitas.text.toString().toDoubleOrNull() ?: 0.0
            val tenor = binding.etTenorAnuitas.text.toString().toIntOrNull() ?: 0

            if (pinjaman > 0 && bunga > 0 && tenor > 0) {
                val simulasiList = hitungCicilanAnuitas(pinjaman, bunga, tenor)
                binding.rvTabelSimulasiAnuitas.adapter = SimulasiCicilanAdapter(simulasiList)
            }
        }

        binding.etPinjamanAnuitas.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.etPinjamanAnuitas.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp,.]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = Util().formatRupiah(parsed)

                        current = formatted
                        binding.etPinjamanAnuitas.setText(formatted)
                        binding.etPinjamanAnuitas.setSelection(formatted.length)
                    }

                    binding.etPinjamanAnuitas.addTextChangedListener(this)
                }
            }
        })

        binding.etBungaAnuitas.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.etBungaAnuitas.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("%", "").trim()

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        current = "$parsed%"
                        binding.etBungaAnuitas.setText(current)
                        binding.etBungaAnuitas.setSelection(current.length)
                    }

                    binding.etBungaAnuitas.addTextChangedListener(this)
                }
            }
        })

        binding.etTenorAnuitas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val value = s.toString().toIntOrNull()
                    if (value != null && value > 60) {
                        binding.etTenorAnuitas.setText("60")
                        binding.etTenorAnuitas.setSelection(binding.etTenorAnuitas.text.length)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
}
