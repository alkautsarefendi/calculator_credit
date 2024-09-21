package project.alkautsar.simulasikredit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.alkautsar.simulasikredit.databinding.ItemSimulasiCicilanBinding
import project.alkautsar.simulasikredit.model.SimulasiCicilan

class SimulasiCicilanAdapter(private val simulasiList: List<SimulasiCicilan>) :
    RecyclerView.Adapter<SimulasiCicilanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSimulasiCicilanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(simulasi: SimulasiCicilan) {
            binding.tvBulan.text = simulasi.bulan.toString()
            binding.tvCicilanPokok.text = Util().formatRupiah(simulasi.cicilanPokok)
            binding.tvBunga.text = Util().formatRupiah(simulasi.bunga)
            binding.tvTotalCicilan.text = Util().formatRupiah(simulasi.totalCicilan)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSimulasiCicilanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(simulasiList[position])
    }

    override fun getItemCount(): Int = simulasiList.size
}
