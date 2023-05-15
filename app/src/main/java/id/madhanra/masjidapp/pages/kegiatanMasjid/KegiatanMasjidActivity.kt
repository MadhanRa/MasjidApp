package id.madhanra.masjidapp.pages.kegiatanMasjid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.adapter.KegMasjidAdapter
import id.madhanra.masjidapp.databinding.ActivityKegiatanMasjidBinding
import id.madhanra.masjidapp.db.helper.KegMasjidHelper
import id.madhanra.masjidapp.helper.MappingHelper
import id.madhanra.masjidapp.model.Kegiatan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class KegiatanMasjidActivity : AppCompatActivity() {

    private lateinit var adapter: KegMasjidAdapter
    private lateinit var binding: ActivityKegiatanMasjidBinding
    private lateinit var mToolbar: MaterialToolbar
    private lateinit var listKegiatanTemp: ArrayList<Kegiatan>

    val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            when (result.resultCode) {
                FormKegMasjidActivity.RESULT_ADD -> {
                    val kegiatan =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(
                            FormKegMasjidActivity.EXTRA_KEGIATAN,
                            Kegiatan::class.java)
                    } else{
                        result.data?.getParcelableExtra<Kegiatan>(FormKegMasjidActivity.EXTRA_KEGIATAN) as Kegiatan
                    }
                    if (kegiatan != null) {
                        adapter.addItem(kegiatan)
                    }
                    binding.
                    rvKegiatanMasjid.smoothScrollToPosition(adapter.itemCount - 1)
                    showSnackBarMessage("Satu item berhasil ditambahkan")
                }

                FormKegMasjidActivity.RESULT_UPDATE -> {
                    val kegiatan =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result
                            .data?.getParcelableExtra(
                                FormKegMasjidActivity.EXTRA_KEGIATAN,
                                Kegiatan::class.java)
                    } else {
                        result
                            .data?.getParcelableExtra<Kegiatan>(
                                FormKegMasjidActivity.EXTRA_KEGIATAN
                            ) as Kegiatan
                    }
                    val position =
                        result.data?.getIntExtra(
                            FormKegMasjidActivity.EXTRA_POSITION
                            , 0) as Int
                    if (kegiatan != null) {
                        adapter.updateItem(position, kegiatan)
                    }
                    binding.rvKegiatanMasjid.smoothScrollToPosition(position)
                    showSnackBarMessage("Satu item berhasil diubah")
                }

                FormKegMasjidActivity.RESULT_DELETE -> {
                    val position =
                        result.data?.getIntExtra(
                            FormKegMasjidActivity.EXTRA_POSITION
                            , 0) as Int
                    adapter.removeItem(position)
                    showSnackBarMessage("Satu item berhasil dihapus")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKegiatanMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.title =
            resources.getString(R.string.title_activity_kegiatan_masjid)

        // Setting RecyclerView
        val rvKegMasjid: RecyclerView = binding.rvKegiatanMasjid
        rvKegMasjid.layoutManager = LinearLayoutManager(this)
        rvKegMasjid.setHasFixedSize(true)
        // Setting adapter
        adapter = KegMasjidAdapter(object : KegMasjidAdapter.OnItemClickCallback {
            override fun onItemClicked(selectedKegiatan: Kegiatan?, position: Int) {
                val intent =
                    Intent(
                        this@KegiatanMasjidActivity,
                        FormKegMasjidActivity::class.java)
                intent.putExtra(FormKegMasjidActivity.EXTRA_KEGIATAN, selectedKegiatan)
                intent.putExtra(FormKegMasjidActivity.EXTRA_POSITION, position)
                resultLauncher.launch(intent)
            }
        })
        rvKegMasjid.adapter = adapter

        binding.fabTambahKeg.setOnClickListener {
            val intent = Intent(
                this@KegiatanMasjidActivity, FormKegMasjidActivity::class.java)
            resultLauncher.launch(intent)
        }
        // Mengambil data jamaah dengan background thread
        loadKegiatanAsync()

        mToolbar = binding.toolbarKegiatanMasjid
        setSupportActionBar(mToolbar)

        setChipGroup()

        if (savedInstanceState == null) {
            // proses ambil data
            loadKegiatanAsync()
        } else {
            // Jika ada data yang tersimpan di savedInstanceState, ambil dari sana
            val list = savedInstanceState.getParcelableArrayList<Kegiatan>(EXTRA_STATE)
            if (list != null) {
                adapter.listKegiatan = list
                listKegiatanTemp = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listKegiatan)
    }

    private fun loadKegiatanAsync() {
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            val kegiatanHelper = KegMasjidHelper.getInstance(applicationContext)
            kegiatanHelper.open()
            val differedKegiatan = async(Dispatchers.IO) {
                val cursor = kegiatanHelper.queryAll()
                MappingHelper.mapCursorToArrayListKegMasjid(cursor)
            }
            binding.progressbar.visibility = View.INVISIBLE
            val listKegiatan = differedKegiatan.await()
            if (listKegiatan.size > 0) {
                adapter.listKegiatan = listKegiatan
                listKegiatanTemp = listKegiatan
                adapter.notifyDataSetChanged()
            } else {
                adapter.listKegiatan = ArrayList()
                listKegiatanTemp = ArrayList()
                showSnackBarMessage("Belum ada data saat ini")
            }
            kegiatanHelper.close()
        }
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
                            searchKegiatan(newText)
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

    private fun searchKegiatan(query: String?) {
        if (query != null) {
            val searchedKegiatan = listKegiatanTemp.filter {
                it.namaKeg?.contains(query, true) == true
                        || it.tglKeg?.contains(query, true) == true
                        || it.penanggungJwbKeg?.contains(query, true) == true
            }
            if (searchedKegiatan.isEmpty()) {
                Toast.makeText(
                    this@KegiatanMasjidActivity,
                    "Data tidak ditemukan", Toast.LENGTH_LONG).show()
            } else {
                adapter.setSearchedList(searchedKegiatan as ArrayList<Kegiatan>)
            }
        }
    }

    private fun setChipGroup() {
        binding.chipGroupFilterKegiatan.forEach { child ->
            (child as? Chip)?.setOnCheckedChangeListener { _, _ ->
                registerFilterChanged()}
        }
    }

    private fun registerFilterChanged() {
        val ids = binding.chipGroupFilterKegiatan.checkedChipIds
        var jamaahKegiatan = listKegiatanTemp

        ids.forEach { id ->
            val text = binding.chipGroupFilterKegiatan.findViewById<Chip>(id).text
            jamaahKegiatan =
                jamaahKegiatan.filter { it.jenisKeg == text } as ArrayList<Kegiatan>
        }
        adapter.setSearchedList(jamaahKegiatan)
    }

    private fun showSnackBarMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }
}