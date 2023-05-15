package id.madhanra.masjidapp.pages.dataJamaah

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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.adapter.JamaahAdapter
import id.madhanra.masjidapp.databinding.ActivityDataJamaahBinding
import id.madhanra.masjidapp.db.helper.JamaahHelper
import id.madhanra.masjidapp.helper.MappingHelper
import id.madhanra.masjidapp.model.Jamaah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DataJamaahActivity : AppCompatActivity() {

    // Variabel yang dibutuhkan
    private lateinit var binding: ActivityDataJamaahBinding
    private lateinit var adapter: JamaahAdapter
    private lateinit var mToolbar: MaterialToolbar
    private lateinit var listJamaahTemp: ArrayList<Jamaah>

    // Launcher untuk intent yang memiliki kembalian
    val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            when (result.resultCode) {
                FormDataJamaahActivity.RESULT_ADD -> {
                    val jamaah = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(FormDataJamaahActivity.EXTRA_JAMAAH, Jamaah::class.java)
                    } else{
                            result.data?.getParcelableExtra<Jamaah>(FormDataJamaahActivity.EXTRA_JAMAAH) as Jamaah}
                    if (jamaah != null) {
                        adapter.addItem(jamaah)
                    }
                    binding.rvDataJamaah.smoothScrollToPosition(adapter.itemCount - 1)
                    showSnackBarMessage("Satu item berhasil ditambahkan")
                }

                FormDataJamaahActivity.RESULT_UPDATE -> {
                    val jamaah = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(FormDataJamaahActivity.EXTRA_JAMAAH, Jamaah::class.java)
                    } else {
                        result.data?.getParcelableExtra<Jamaah>(FormDataJamaahActivity.EXTRA_JAMAAH) as Jamaah
                    }
                    val position =
                        result.data?.getIntExtra(FormDataJamaahActivity.EXTRA_POSITION, 0) as Int
                    if (jamaah != null) {
                        adapter.updateItem(position, jamaah)
                    }
                    binding.rvDataJamaah.smoothScrollToPosition(position)
                    showSnackBarMessage("Satu item berhasil diubah")
                }

                FormDataJamaahActivity.RESULT_DELETE -> {
                    val position =
                        result.data?.getIntExtra(FormDataJamaahActivity.EXTRA_POSITION, 0) as Int
                    adapter.removeItem(position)
                    showSnackBarMessage("Satu item berhasil dihapus")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataJamaahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Data Jamaah Masjid"
        // Setting RecyclerView
        binding.rvDataJamaah.layoutManager = LinearLayoutManager(this)
        binding.rvDataJamaah.setHasFixedSize(true)
        // Setting adapter
        adapter = JamaahAdapter(object : JamaahAdapter.OnItemClickCallback {
            override fun onItemClicked(selectedJamaah: Jamaah?, position: Int) {
                val intent = Intent(this@DataJamaahActivity, FormDataJamaahActivity::class.java)
                intent.putExtra(FormDataJamaahActivity.EXTRA_JAMAAH, selectedJamaah)
                intent.putExtra(FormDataJamaahActivity.EXTRA_POSITION, position)
                resultLauncher.launch(intent)
            }
        })
        binding.rvDataJamaah.adapter = adapter
        binding.fabTambah.setOnClickListener {
            val intent = Intent(this@DataJamaahActivity, FormDataJamaahActivity::class.java)
            resultLauncher.launch(intent)
        }
        // Mengambil data jamaah dengan background thread
        loadJamaahAsync()

        mToolbar = binding.toolbarDataJamaah
        setSupportActionBar(mToolbar)

        setChipGroup()

        if (savedInstanceState == null) {
            // proses ambil data
            loadJamaahAsync()
        } else {
            // Jika ada data yang tersimpan di savedInstanceState, ambil dari sana
            val list = savedInstanceState.getParcelableArrayList<Jamaah>(EXTRA_STATE)
            if (list != null) {
                adapter.listJamaah = list
                listJamaahTemp = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listJamaah)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_search, menu)

        mToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search_bar -> {
                    val menuItemSearch = menu?.findItem(R.id.search_bar)
                    val searchView : SearchView = menuItemSearch?.actionView as SearchView
                    searchView.queryHint = "Cari Jamaah"
                    searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean = false

                        override fun onQueryTextChange(newText: String?): Boolean {
                            searchJamaah(newText)
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

    private fun setChipGroup() {
        binding.chipGroupFilter.forEach { child ->
            (child as? Chip)?.setOnCheckedChangeListener { _, _ -> registerFilterChanged()}
        }
    }

    private fun registerFilterChanged() {
        val ids = binding.chipGroupFilter.checkedChipIds
        var jamaahFiltered = listJamaahTemp

        ids.forEach { id ->
            val text = binding.chipGroupFilter.findViewById<Chip>(id).text
            jamaahFiltered = jamaahFiltered.filter { it.jenKelamin == text || it.kategori == text } as ArrayList<Jamaah>
        }
        adapter.setSearchedList(jamaahFiltered)
    }

    private fun searchJamaah(query: String?) {
        if (query != null) {
            val searchedJamaah = listJamaahTemp.filter {
                it.nama?.contains(query, true) == true
                        || it.tglLahir?.contains(query, true) == true
                        || it.alamat?.contains(query, true) == true
            }
            if (searchedJamaah.isEmpty()) {
                Toast.makeText(this@DataJamaahActivity, "Data tidak ditemukan", Toast.LENGTH_LONG).show()
            } else {
                adapter.setSearchedList(searchedJamaah as ArrayList<Jamaah>)
            }
        }
    }

    private fun loadJamaahAsync() {
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            val jamaahHelper = JamaahHelper.getInstance(applicationContext)
            jamaahHelper.open()
            val differedJamaah = async(Dispatchers.IO) {
                val cursor = jamaahHelper.queryAll()
                MappingHelper.mapCursorToArrayListJamaah(cursor)
            }
            binding.progressbar.visibility = View.INVISIBLE
            val listJamaah = differedJamaah.await()
            if (listJamaah.size > 0) {
                adapter.listJamaah = listJamaah
                listJamaahTemp = listJamaah
                adapter.notifyDataSetChanged()
            } else {
                adapter.listJamaah = ArrayList()
                listJamaahTemp = ArrayList()
                showSnackBarMessage("Belum ada data saat ini")
            }
            jamaahHelper.close()
        }
    }

    private fun showSnackBarMessage(msg: String) {
        Snackbar.make(binding.rvDataJamaah, msg, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }
}