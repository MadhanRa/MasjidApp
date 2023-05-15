package id.madhanra.masjidapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.databinding.ItemKegMasjidBinding
import id.madhanra.masjidapp.model.Kegiatan

class KegMasjidAdapter(private val onItemClickCallback: OnItemClickCallback) :
    RecyclerView.Adapter<KegMasjidAdapter.KegMasjidViewHolder>() {

    var listKegiatan = ArrayList<Kegiatan>()
        set(listKegMasjid) {
            if (listKegMasjid.size > 0) {
                this.listKegiatan.clear()
            }
            this.listKegiatan.addAll(listKegMasjid)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KegMasjidAdapter.KegMasjidViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_keg_masjid, parent, false
            )
        return KegMasjidViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: KegMasjidAdapter.KegMasjidViewHolder, position: Int
    ) {
        holder.bind(listKegiatan[position])
    }

    override fun getItemCount(): Int = this.listKegiatan.size

    inner class KegMasjidViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ItemKegMasjidBinding.bind(itemView)
        fun bind(kegiatan: Kegiatan) {
            binding.tvNamaKeg.text = kegiatan.namaKeg
            binding.tvTglKegiatan.text = kegiatan.tglKeg
            binding.tvPenanngungJwb.text = kegiatan.penanggungJwbKeg
            binding.tvJenKeg.text = kegiatan.jenisKeg
            binding.cvKegMasjid.setOnClickListener {
                onItemClickCallback.onItemClicked(kegiatan, adapterPosition)
            }
        }
    }

    fun setSearchedList(listKegiatan: ArrayList<Kegiatan>) {
        if (listKegiatan.isEmpty()) {
            this.listKegiatan.clear()
        } else {
            this.listKegiatan = listKegiatan
        }
        notifyDataSetChanged()
    }

    // Untuk menambah item baru di Recyclerview
    fun addItem(kegiatan: Kegiatan) {
        this.listKegiatan.add(kegiatan)
        notifyItemInserted(this.listKegiatan.size - 1)
    }

    // Mengupdate item di Recyclerview
    fun updateItem(position: Int, kegiatan: Kegiatan) {
        this.listKegiatan[position] = kegiatan
        notifyItemChanged(position, kegiatan)
    }

    // Menghapus item di Recyclerview
    fun removeItem(position: Int) {
        this.listKegiatan.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listKegiatan.size)
    }

    interface OnItemClickCallback {
        fun onItemClicked(selectedKegiatan: Kegiatan?, position: Int)
    }
}