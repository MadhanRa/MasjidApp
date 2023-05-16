package id.madhanra.masjidapp.pages.kasMasjid

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import id.madhanra.masjidapp.R
import id.madhanra.masjidapp.databinding.ActivityFormKasMasjidBinding
import id.madhanra.masjidapp.db.DatabaseContract.KasMasjidColumns
import id.madhanra.masjidapp.db.helper.KasMasjidHelper
import id.madhanra.masjidapp.helper.DatePickerFragment
import id.madhanra.masjidapp.model.Kas
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormKasMasjidActivity : AppCompatActivity(), View.OnClickListener,
    DatePickerFragment.DialogDateListener{

    // Variabel yang dibutuhkan
    private var isEdit = false
    private var kas: Kas? = null
    private var position: Int = 0
    private lateinit var kasHelper: KasMasjidHelper
    private lateinit var binding: ActivityFormKasMasjidBinding
private lateinit var adapterSpinner: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormKasMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kasHelper = KasMasjidHelper.getInstance(applicationContext)
        kasHelper.open()

        kas = intent.getParcelableExtra(EXTRA_KAS)
        if (kas != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            kas = Kas()
        }

        binding.rgKategoriKas.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.masuk -> {
                    adapterSpinner =
                        ArrayAdapter(
                            this,
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                            resources.getStringArray(R.array.jenis_kas_masuk))
                    binding.spJenisKas.adapter = adapterSpinner
                }
                R.id.keluar -> {
                    adapterSpinner =
                        ArrayAdapter(
                            this,
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                            resources.getStringArray(R.array.jenis_kas_keluar))
                    binding.spJenisKas.adapter = adapterSpinner
                }
            }
        }

        val actionBarTitle: String
        val btnTitle: String
        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            kas?.let {
                binding.edtKeterangan.setText(it.keterangan)
                binding.edtNominal.setText(it.nominal)
                binding.rgKategoriKas.check(R.id.masuk)
                if (it.jenis == getString(R.string.rb_keluar)){
                    binding.rgKategoriKas.check(R.id.keluar)
                }
                binding.spJenisKas.setSelection(adapterSpinner.getPosition(it.jenis))
                binding.tvTglKas.text = it.tglInput
            }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnOnceDate.setOnClickListener(this)
        binding.btnSubmitKas.text = btnTitle
        binding.btnSubmitKas.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // Tombol input tanggal
            R.id.btn_kas_masjid -> {
                val datePickerFragment = DatePickerFragment()
                datePickerFragment.show(supportFragmentManager,
                    DATE_PICKER_TAG
                )
            }
            // Tombol submit
            R.id.btn_submit_kas -> {
                val keterangan = binding.edtKeterangan.text.toString().trim()
                val nominal = binding.edtNominal.text.toString().trim()
                val checkedRadioButton = binding.rgKategoriKas.checkedRadioButtonId
                var kategoriKas = getString(R.string.rb_masuk)
                if (checkedRadioButton == R.id.keluar) kategoriKas = getString(R.string.rb_keluar)
                val jenKas = binding.spJenisKas.selectedItem.toString()
                val tglKas = binding.tvTglKas.text.toString()


                // Pesan error jika salah satu form tidak ada isinya
                if (keterangan.isEmpty()) {
                    binding.edtKeterangan.error = "Form tidak boleh kosong"
                    return
                }
                if (nominal.isEmpty()) {
                    binding.edtNominal.error = "Form tidak boleh kosong"
                    return
                }
                if (tglKas == getString(R.string.tv_tgl_input)){
                    binding.tvTglKas.error = "Tanggal kas tidak boleh kosong"
                    return
                }

                kas?.keterangan = keterangan
                kas?.nominal = nominal
                kas?.kategori = kategoriKas
                kas?.jenis = jenKas
                kas?.tglInput = tglKas

                val intent = Intent()
                intent.putExtra(EXTRA_KAS, kas)
                intent.putExtra(EXTRA_POSITION, position)

                // Menginput data ke database
                val values = ContentValues()
                values.put(KasMasjidColumns.KETERANGAN, keterangan)
                values.put(KasMasjidColumns.NOMINAL, nominal)
                values.put(KasMasjidColumns.KATEGORI, kategoriKas)
                values.put(KasMasjidColumns.JENIS, jenKas)
                values.put(KasMasjidColumns.TGLINPUT, tglKas)

                // Jika tujuannya untuk update
                if (isEdit) {
                    val result = kasHelper.update(kas?.id.toString(), values).toLong()
                    if (result > 0) {
                        setResult(RESULT_UPDATE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormKasMasjidActivity,
                            "Gagal update data",
                            Toast.LENGTH_SHORT

                        ).show()
                    }
                } else { // Jika tujuannya input baru
                    val result = kasHelper.insert(values)
                    if (result > 0) {
                        kas?.id = result.toInt()

                        setResult(RESULT_ADD, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormKasMasjidActivity,
                            "Gagal menambah data",
                            Toast.LENGTH_SHORT

                        ).show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Memberikan fungsi ketika menu diklik
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        showAlertDialog(ALERT_DIALOG_CLOSE)
        return super.getOnBackInvokedDispatcher()
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        // Perngkondisian tipe dialog dari Konstanta jenis dialog
        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Data Kas"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = kasHelper.deleteById(kas?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormKasMasjidActivity, "Gagal menghapus data",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        // Siapkan date formatter-nya terlebih dahulu
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Set text dari textview once
        binding.tvTglKas.text = dateFormat.format(calendar.time)
    }

    companion object {
        const val EXTRA_KAS = "extra_kas"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
        private const val DATE_PICKER_TAG = "DatePicker"
    }
}