package id.madhanra.masjidapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.databinding.ItemKasMasjidBinding
import id.madhanra.masjidapp.model.Kas

class KasMasjidAdapter(private val onItemClickCallback: OnItemClickCallback) :
RecyclerView.Adapter<KasMasjidAdapter.KasMasjidViewHolder>() {

    var listKas = ArrayList<Kas>()
    set(listKasMasjid) {
        if (listKasMasjid.size > 0) {
            this.listKas.clear()
        }
        this.listKas.addAll(listKasMasjid)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KasMasjidAdapter.KasMasjidViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_keg_masjid, parent, false
            )
        return KasMasjidViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: KasMasjidAdapter.KasMasjidViewHolder, position: Int
    ) {
        holder.bind(listKas[position])
    }

    override fun getItemCount(): Int = this.listKas.size

    inner class KasMasjidViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ItemKasMasjidBinding.bind(itemView)
        fun bind(kas: Kas) {
            binding.tvKeterangan.text = kas.keterangan
            val nominal = kas.nominal?.toInt()?.formatDecimalSeparator()
            binding.tvNominal.text =
                itemView.context.getString(R.string.tv_nominal, nominal)
            binding.tvKategoriKas.text = kas.kategori
            binding.tvTglInput.text = kas.tglInput
            var color = R.color.green
            if (kas.kategori == itemView.context.getString(R.string.rb_keluar))
                color = R.color.pink
            binding.cvKasMasjid.strokeColor = itemView.context.getColor(color)
            binding.cvKasMasjid.setOnClickListener {
                onItemClickCallback.onItemClicked(kas, adapterPosition)
            }
        }
    }

    fun setSearchedList(listKas: ArrayList<Kas>) {
        if (listKas.isEmpty()) {
            this.listKas.clear()
        } else {
            this.listKas = listKas
        }
        notifyDataSetChanged()
    }

    // Untuk menambah item baru di Recyclerview
    fun addItem(kas: Kas) {
        this.listKas.add(kas)
        notifyItemInserted(this.listKas.size - 1)
    }

    // Mengupdate item di Recyclerview
    fun updateItem(position: Int, kas: Kas) {
        this.listKas[position] = kas
        notifyItemChanged(position, kas)
    }

    // Menghapus item di Recyclerview
    fun removeItem(position: Int) {
        this.listKas.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listKas.size)
    }

    fun Int.formatDecimalSeparator(): String {
        return toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    }

    interface OnItemClickCallback {
        fun onItemClicked(selectedKas: Kas?, position: Int)
    }
}