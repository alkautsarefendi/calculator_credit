package project.alkautsar.simulasikredit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.alkautsar.simulasikredit.databinding.ItemHeaderBinding
import project.alkautsar.simulasikredit.databinding.ItemSimulasiCicilanBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilan

class SimulasiCicilanAdapter(private val simulasiList: List<SimulasiCicilan>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1

    // ViewHolder untuk item data (cicilan)
    inner class ItemViewHolder(private val binding: ItemSimulasiCicilanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(simulasi: SimulasiCicilan) {
            binding.tvBulan.text = simulasi.bulan.toString()
            binding.tvCicilanPokok.text = Util().formatRupiah(simulasi.cicilanPokok)
            binding.tvBunga.text = Util().formatRupiah(simulasi.bunga)
            binding.tvTotalCicilan.text = Util().formatRupiah(simulasi.totalCicilan)
            binding.tvSisaCicilan.text = Util().formatRupiah(simulasi.sisaCicilan)
        }
    }

    // ViewHolder untuk header (hanya ditampilkan sekali)
    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            // Header tidak membutuhkan data binding spesifik, jadi kita hanya perlu inflate layout header
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            // Inflate header layout
            val binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            // Inflate item data layout
            val binding = ItemSimulasiCicilanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            // Tidak ada binding data khusus untuk header
            (holder as HeaderViewHolder).bind()
        } else {
            // Bind data cicilan pada baris setelah header
            val simulasi = simulasiList[position - 1] // Posisi -1 karena header ada di posisi 0
            (holder as ItemViewHolder).bind(simulasi)
        }
    }

    override fun getItemCount(): Int {
        return simulasiList.size + 1 // Tambahkan 1 untuk header
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEADER // Header di posisi pertama
        } else {
            VIEW_TYPE_ITEM // Data item untuk posisi selanjutnya
        }
    }

    fun getData(): List<SimulasiCicilan> {
        return simulasiList
    }
}
