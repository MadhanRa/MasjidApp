package id.madhanra.masjidapp.pages.kasMasjid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.adapter.KasMasjidAdapter
import id.madhanra.masjidapp.databinding.ActivityKasMasjidBinding
import id.madhanra.masjidapp.db.helper.KasMasjidHelper
import id.madhanra.masjidapp.helper.MappingHelper
import id.madhanra.masjidapp.model.Kas
import id.madhanra.masjidapp.pages.kegiatanMasjid.FormKegMasjidActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class KasMasjidActivity : AppCompatActivity() {

    private lateinit var adapter: KasMasjidAdapter
    private lateinit var binding: ActivityKasMasjidBinding
    private lateinit var mToolbar: MaterialToolbar
    private lateinit var listKasTemp: ArrayList<Kas>

    val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            when (result.resultCode) {
                FormKasMasjidActivity.RESULT_ADD -> {
                    val kas =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            result.data?.getParcelableExtra(
                                FormKasMasjidActivity.EXTRA_KAS,
                                Kas::class.java)
                        } else{
                            result.data?.getParcelableExtra<Kas>(
                                FormKasMasjidActivity.EXTRA_KAS) as Kas
                        }
                    if (kas != null) {
                        adapter.addItem(kas)
                    }
                    binding.
                    rvKasMasjid.smoothScrollToPosition(adapter.itemCount - 1)
                    showSnackBarMessage("Satu item berhasil ditambahkan")
                }

                FormKasMasjidActivity.RESULT_UPDATE -> {
                    val kas =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            result
                                .data?.getParcelableExtra(
                                    FormKasMasjidActivity.EXTRA_KAS,
                                    Kas::class.java)
                        } else {
                            result
                                .data?.getParcelableExtra<Kas>(
                                    FormKasMasjidActivity.EXTRA_KAS
                                ) as Kas
                        }
                    val position =
                        result.data?.getIntExtra(
                            FormKasMasjidActivity.EXTRA_POSITION
                            , 0) as Int
                    if (kas != null) {
                        adapter.updateItem(position, kas)
                    }
                    binding.rvKasMasjid.smoothScrollToPosition(position)
                    showSnackBarMessage("Satu item berhasil diubah")
                }

                FormKasMasjidActivity.RESULT_DELETE -> {
                    val position =
                        result.data?.getIntExtra(
                            FormKasMasjidActivity.EXTRA_POSITION
                            , 0) as Int
                    adapter.removeItem(position)
                    showSnackBarMessage("Satu item berhasil dihapus")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKasMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.title =
            resources.getString(R.string.title_activity_kegiatan_masjid)

        // Setting RecyclerView
        val rvKasMasjid: RecyclerView = binding.rvKasMasjid
        rvKasMasjid.layoutManager = LinearLayoutManager(this)
        rvKasMasjid.setHasFixedSize(true)
        // Setting adapter
        adapter = KasMasjidAdapter(object : KasMasjidAdapter.OnItemClickCallback {
            override fun onItemClicked(selectedKas: Kas?, position: Int) {
                val intent =
                    Intent(
                        this@KasMasjidActivity,
                        FormKegMasjidActivity::class.java)
                intent.putExtra(FormKasMasjidActivity.EXTRA_KAS, selectedKas)
                intent.putExtra(FormKasMasjidActivity.EXTRA_POSITION, position)
                resultLauncher.launch(intent)
            }
        })
        rvKasMasjid.adapter = adapter

        binding.fabTambahKeg.setOnClickListener {
            val intent = Intent(
                this@KasMasjidActivity, FormKegMasjidActivity::class.java)
            resultLauncher.launch(intent)
        }
        // Mengambil data jamaah dengan background thread
        loadKasAsync()

        mToolbar = binding.toolbarKasMasjid
        setSupportActionBar(mToolbar)

        setChipGroup()

        if (savedInstanceState == null) {
            // proses ambil data
            loadKasAsync()
        } else {
            // Jika ada data yang tersimpan di savedInstanceState, ambil dari sana
            val list = savedInstanceState.getParcelableArrayList<Kas>(EXTRA_STATE)
            if (list != null) {
                adapter.listKas = list
                listKasTemp = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listKas)
    }

    private fun loadKasAsync() {
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            val kasHelper = KasMasjidHelper.getInstance(applicationContext)
            kasHelper.open()
            val differedKas = async(Dispatchers.IO) {
                val cursor = kasHelper.queryAll()
                MappingHelper.mapCursorToArrayListKasMasjid(cursor)
            }
            binding.progressbar.visibility = View.INVISIBLE
            val listKas = differedKas.await()
            if (listKas.size > 0) {
                adapter.listKas = listKas
                listKasTemp = listKas
                setTotalKas(listKas)
                adapter.notifyDataSetChanged()
            } else {
                adapter.listKas = ArrayList()
                listKasTemp = ArrayList()
                showSnackBarMessage("Belum ada data saat ini")
            }
            kasHelper.close()
        }
    }

    private fun setTotalKas(listKas: ArrayList<Kas>) {
        var total = 0
        listKas.forEach {
            if (it.kategori == getString(R.string.rb_masuk)) {
                total += it.nominal?.toInt() ?: 0
            } else {
                total -= it.nominal?.toInt() ?: 0
            }
        }
        val displayTotal = getString(R.string.tv_nominal, total.formatDecimalSeparator())
        binding.tvTotalKas.text = displayTotal
    }

    private fun Int.formatDecimalSeparator(): String {
        return toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_search, menu)

        mToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search_bar -> {
                    val menuItemSearch = menu?.findItem(R.id.search_bar)
                    val searchView : SearchView =
                        menuItemSearch?.actionView as SearchView
                    searchView.queryHint = "Cari Kegiatan"
                    searchView.setOnQueryTextListener(
                        object: SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean = false

                            override fun onQueryTextChange(newText: String?): Boolean {
                                searchKas(newText)
                                return true
                            }
                        })
                    true
                }
                else -> true
            }
        }
        return true
    }

    private fun searchKas(query: String?) {
        if (query != null) {
            val searchedKas = listKasTemp.filter {
                it.keterangan?.contains(query, true) == true
                        || it.nominal?.contains(query, true) == true
                        || it.jenis?.contains(query, true) == true
            }
            if (searchedKas.isEmpty()) {
                Toast.makeText(
                    this@KasMasjidActivity,
                    "Data tidak ditemukan", Toast.LENGTH_LONG).show()
            } else {
                adapter.setSearchedList(searchedKas as ArrayList<Kas>)
            }
        }
    }

    private fun setChipGroup() {
        binding.chipGroupFilterKas.forEach { child ->
            (child as? Chip)?.setOnCheckedChangeListener { _, _ ->
                registerFilterChanged()}
        }
    }

    private fun registerFilterChanged() {
        val ids = binding.chipGroupFilterKas.checkedChipIds
        var jamaahKas = listKasTemp

        ids.forEach { id ->
            val text = binding.chipGroupFilterKas.findViewById<Chip>(id).text
            jamaahKas =
                jamaahKas.filter { it.kategori == text } as ArrayList<Kas>
        }
        adapter.setSearchedList(jamaahKas)
    }

    private fun showSnackBarMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }
}