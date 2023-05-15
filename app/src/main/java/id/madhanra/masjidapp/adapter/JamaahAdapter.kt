package id.madhanra.masjidapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.databinding.ItemDataJamaahBinding
import id.madhanra.masjidapp.model.Jamaah
import java.util.Calendar

class JamaahAdapter(private val onItemClickCallback: OnItemClickCallback) :
    RecyclerView.Adapter<JamaahAdapter.JamaahViewHolder>() {

    var listJamaah = ArrayList<Jamaah>()
        set(listJamaah) {
            if (listJamaah.size > 0) {
                this.listJamaah.clear()
            }
            this.listJamaah.addAll(listJamaah)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JamaahAdapter.JamaahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_data_jamaah, parent, false)
        return JamaahViewHolder(view)
    }

    override fun onBindViewHolder(holder: JamaahAdapter.JamaahViewHolder, position: Int) {
        holder.bind(listJamaah[position])
    }

    override fun getItemCount(): Int = this.listJamaah.size

    inner class JamaahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemDataJamaahBinding.bind(itemView)
        fun bind(jamaah: Jamaah) {
            val age = itemView.context.getString(R.string.tv_umur, getAge(jamaah.tglLahir))
            var color = R.color.blue_700
            if (jamaah.jenKelamin == "Perempuan") color = R.color.pink
            binding.cvDataJamaah.strokeColor = itemView.context.getColor(color)
            binding.tvNamaJamaah.text = jamaah.nama
            binding.tvAlamatJamaah.text = jamaah.alamat
            binding.tvKategoriJamaah.text = jamaah.kategori
            binding.tvUmurJamaah.text = age
            binding.cvDataJamaah.setOnClickListener {
                onItemClickCallback.onItemClicked(jamaah, adapterPosition)
            }
        }
    }

    fun setSearchedList(listJamaah: ArrayList<Jamaah>) {
        if (listJamaah.isEmpty()) {
            this.listJamaah.clear()
        } else {
            this.listJamaah= listJamaah
        }
        notifyDataSetChanged()
    }

    fun getAge(tglLahir: String?): String {
        val tglArray = tglLahir?.split("-")?.toTypedArray()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cYear = Integer.parseInt(tglArray?.get(0) ?: "0")
        val age = currentYear - cYear
        return age.toString()
    }

    // Untuk menambah item baru di Recyclerview
    fun addItem(jamaah: Jamaah) {
        this.listJamaah.add(jamaah)
        notifyItemInserted(this.listJamaah.size - 1)
    }

    // Mengupdate item di Recyclerview
    fun updateItem(position: Int, jamaah: Jamaah) {
        this.listJamaah[position] = jamaah
        notifyItemChanged(position, jamaah)
    }

    // Menghapus item di Recyclerview
    fun removeItem(position: Int) {
        this.listJamaah.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listJamaah.size)
    }

    interface OnItemClickCallback {
        fun onItemClicked(selectedJamaah: Jamaah?, position: Int)
    }
}