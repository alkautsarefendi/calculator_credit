package project.alkautsar.simulasikredit

import android.content.ContentValues
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import project.alkautsar.simulasikredit.databinding.ActivityBungaAnuitasBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilan
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class BungaAnuitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBungaAnuitasBinding
    private lateinit var jumlahPinjamanTextView: TextView
    private lateinit var lamaPinjamanTextView: TextView
    private lateinit var bungaPertahunTextView: TextView
    private lateinit var ringkasanAngsuranTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityBungaAnuitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi TextView
        jumlahPinjamanTextView = binding.tvJumlahPinjaman
        lamaPinjamanTextView = binding.tvLamaPinjaman
        bungaPertahunTextView = binding.tvBungaPertahun
        ringkasanAngsuranTextView = binding.tvRingkasanAngsuran

        // Initialize RecyclerView LayoutManager
        binding.rvTabelSimulasiAnuitas.layoutManager = LinearLayoutManager(this)
        binding.etPinjamanAnuitas.addTextChangedListener(Util.NumberTextWatcher(binding.etPinjamanAnuitas))
//        binding.etBungaAnuitas.addTextChangedListener(Util.InterestRateTextWatcher(binding.etBungaAnuitas))

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

                binding.llSummary.visibility = View.VISIBLE
                // Update TextViews with the calculated values
                updateSummary(pinjaman, tenor, bunga, simulasiList)

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

        binding.btnGeneratePdf.setOnClickListener {
            generatePdf()
        }
    }

    private fun updateSummary(pinjaman: Double, tenor: Int, bunga: Double, simulasiList: List<SimulasiCicilan>) {
        // Menampilkan Jumlah Pinjaman
        jumlahPinjamanTextView.text = "${Util().formatRupiah(pinjaman)}"

        // Menampilkan Lama Pinjaman
        lamaPinjamanTextView.text = "$tenor Bulan"

        // Menampilkan Bunga Pertahun
        bungaPertahunTextView.text = "$bunga%"

        // Menghitung dan Menampilkan Angsuran per Bulan
        val cicilanBulanan = if (simulasiList.isNotEmpty()) {
            simulasiList[0].totalCicilan // Total cicilan bulan pertama
        } else {
            0.0
        }
        ringkasanAngsuranTextView.text = "${Util().formatRupiah(cicilanBulanan)}"
    }

    private fun hitungCicilanAnuitas(pinjaman: Double, bunga: Double, tenor: Int): List<SimulasiCicilan> {
        val simulasiList = mutableListOf<SimulasiCicilan>()

        // Menghitung bunga bulanan
        val bungaBulanan = bunga / 100 / 12 // Bunga bulanan dari bunga tahunan
        // Menghitung cicilan per bulan menggunakan rumus anuitas
        val cicilanPerBulan = (pinjaman * bungaBulanan) / (1 - Math.pow(1 + bungaBulanan, -tenor.toDouble()))

        var saldoPinjaman = pinjaman
        for (bulan in 1..tenor) {
            val bungaBulanIni = saldoPinjaman * bungaBulanan // Bunga untuk bulan ini
            val pokokBulanIni = cicilanPerBulan - bungaBulanIni // Pembayaran pokok untuk bulan ini

            // Pastikan tidak ada nilai negatif
            if (pokokBulanIni < 0) {
                Log.e("ERROR", "Pokok Bulan Ini is negative: $pokokBulanIni")
                continue
            }

            // Hitung saldo pinjaman setelah pembayaran bulan ini
            saldoPinjaman -= pokokBulanIni // Update saldo yang tersisa

            // Tambahkan ke daftar dengan properti baru untuk saldo yang tersisa
            simulasiList.add(SimulasiCicilan(bulan, pokokBulanIni, bungaBulanIni, cicilanPerBulan, saldoPinjaman))
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
        binding.llSummary.visibility = View.GONE
    }

    private fun generatePdf() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.textSize = 12f

        // Header
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Simulasi Cicilan Anuitas", pageInfo.pageWidth / 2f, 50f, paint)

        // Table Header
        val yPosition = 100f
        canvas.drawText("Bulan", 20f, yPosition, paint)
        canvas.drawText("Cicilan Pokok", 120f, yPosition, paint)
        canvas.drawText("Bunga", 220f, yPosition, paint)
        canvas.drawText("Total Cicilan", 320f, yPosition, paint)

        // Render data from RecyclerView
        val simulasiList = (binding.rvTabelSimulasiAnuitas.adapter as? SimulasiCicilanAdapter)?.getData() ?: emptyList()
        var y = yPosition + 30f

        for (item in simulasiList) {
            canvas.drawText(item.bulan.toString(), 20f, y, paint)
            canvas.drawText(Util().formatRupiah(item.cicilanPokok), 120f, y, paint)
            canvas.drawText(Util().formatRupiah(item.bunga), 220f, y, paint)
            canvas.drawText(Util().formatRupiah(item.totalCicilan), 320f, y, paint)
            y += 30f
        }

        // Add Watermark in top-right corner
        val watermarkPaint = Paint().apply {
            color = Color.LTGRAY
            alpha = 50 // Transparency
            textSize = 12f // Set an appropriate text size
        }

        // Retrieve app name from resources
        val watermarkText = "Created by ${getString(R.string.app_name)}"
        val textWidth = watermarkPaint.measureText(watermarkText)

        // Position at top-right corner
        val xWatermarkPosition = pageInfo.pageWidth - textWidth - 20f // 20f is some padding from the right
        val yWatermarkPosition = 50f // Align with the header

        canvas.drawText(watermarkText, xWatermarkPosition, yWatermarkPosition, watermarkPaint)

        pdfDocument.finishPage(page)

        // Save the PDF to storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "simulasi_cicilan.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let {
                val outputStream = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    pdfDocument.writeTo(stream)
                    Toast.makeText(this, "PDF saved successfully in Documents", Toast.LENGTH_SHORT).show()

                    // Open the PDF file
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(it, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
            } ?: run {
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For Android 9 and below
            val file = File(getExternalFilesDir(null), "simulasi_cicilan.pdf")
            try {
                val outputStream: OutputStream = FileOutputStream(file)
                pdfDocument.writeTo(outputStream)
                outputStream.close()
                Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                openPdf(file)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        pdfDocument.close()
    }

    private fun openPdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Check if there is an app that can handle this intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

}
