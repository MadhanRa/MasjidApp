package id.madhanra.masjidapp.pages.dataJamaah

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
import id.madhanra.masjidapp.databinding.ActivityFormDataJamaahBinding
import id.madhanra.masjidapp.db.DatabaseContract
import id.madhanra.masjidapp.db.helper.JamaahHelper
import id.madhanra.masjidapp.helper.DatePickerFragment
import id.madhanra.masjidapp.model.Jamaah
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FormDataJamaahActivity : AppCompatActivity(), View.OnClickListener,
    DatePickerFragment.DialogDateListener {

    // Variabel yang dibutuhkan
    private var isEdit = false
    private var jamaah: Jamaah? = null
    private var position: Int = 0
    private lateinit var jamaahHelper: JamaahHelper
    private lateinit var binding: ActivityFormDataJamaahBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormDataJamaahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jamaahHelper = JamaahHelper.getInstance(applicationContext)
        jamaahHelper.open()

        jamaah = intent.getParcelableExtra(EXTRA_JAMAAH)
        if (jamaah != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            jamaah = Jamaah()
        }

        val adapterSpinner = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.kategori_jamaah))
        binding.spKategori.adapter = adapterSpinner

        val actionBarTitle: String
        val btnTitle: String
        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            jamaah?.let {
                binding.edtNip.setText(it.nip)
                binding.edtNama.setText(it.nama)
                binding.rgJenKel.check(R.id.radio_laki)
                if (it.jenKelamin == getString(R.string.perempuan)) binding.rgJenKel.check(R.id.radio_perempuan)
                binding.edtAlamat.setText(it.alamat)
                binding.edtKontak.setText(it.kontak)
                binding.tvOnceDate.text = it.tglLahir
                binding.spKategori.setSelection(adapterSpinner.getPosition(it.kategori))
            }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnOnceDate.setOnClickListener(this)
        binding.btnSubmitJamaah.text = btnTitle
        binding.btnSubmitJamaah.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // Tombol input tanggal
            R.id.btn_once_date -> {
                val datePickerFragment = DatePickerFragment()
                datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)
            }
            // Tombol submit
            R.id.btn_submit_jamaah -> {
                val nip = binding.edtNip.text.toString().trim()
                val nama = binding.edtNama.text.toString().trim()
                val checkedRadioButton = binding.rgJenKel.checkedRadioButtonId
                var jenKelamin = getString(R.string.lakiLaki)
                if (checkedRadioButton == R.id.radio_perempuan) jenKelamin = getString(R.string.perempuan)
                val alamat = binding.edtAlamat.text.toString().trim()
                val kontak = binding.edtKontak.text.toString().trim()
                val tglLahir = binding.tvOnceDate.text.toString()
                val kategori = binding.spKategori.selectedItem.toString()

                // Pesan error jika salah satu form tidak ada isinya
                if (nama.isEmpty()) {
                    binding.edtNama.error = "Form tidak boleh kosong"
                    return
                }
                if (alamat.isEmpty()) {
                    binding.edtAlamat.error = "Form tidak boleh kosong"
                    return
                }
                if (tglLahir == getString(R.string.tv_tanggalLahir)){
                    binding.edtAlamat.error = "Tanggal Lahir tidak boleh kosong"
                    return
                }

                jamaah?.nip = nip
                jamaah?.nama = nama
                jamaah?.jenKelamin = jenKelamin
                jamaah?.alamat = alamat
                jamaah?.tglLahir = tglLahir
                jamaah?.kontak = kontak
                jamaah?.kategori = kategori

                val intent = Intent()
                intent.putExtra(EXTRA_JAMAAH, jamaah)
                intent.putExtra(EXTRA_POSITION, position)

                // Menginput data ke database
                val values = ContentValues()
                values.put(DatabaseContract.JamaahColumns.NIP, nip)
                values.put(DatabaseContract.JamaahColumns.NAMA, nama)
                values.put(DatabaseContract.JamaahColumns.JENKELAMIN, jenKelamin)
                values.put(DatabaseContract.JamaahColumns.ALAMAT, alamat)
                values.put(DatabaseContract.JamaahColumns.TGLLAHIR, tglLahir)
                values.put(DatabaseContract.JamaahColumns.KONTAK, kontak)
                values.put(DatabaseContract.JamaahColumns.KATEGORI, kategori)

                // Jika tujuannya untuk update
                if (isEdit) {
                    val result = jamaahHelper.update(jamaah?.id.toString(), values).toLong()
                    if (result > 0) {
                        setResult(RESULT_UPDATE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormDataJamaahActivity,
                            "Gagal update data",
                            Toast.LENGTH_SHORT

                        ).show()
                    }
                } else { // Jika tujuannya input baru
                    jamaah?.tglInput = getCurrentDate()
                    values.put(DatabaseContract.JamaahColumns.TGLINPUT, getCurrentDate())
                    val result = jamaahHelper.insert(values)
                    if (result > 0) {
                        jamaah?.id = result.toInt()

                        setResult(RESULT_ADD, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormDataJamaahActivity,
                            "Gagal menambah data",
                            Toast.LENGTH_SHORT

                        ).show()
                    }
                }
            }
        }
    }

    // Memanggil menu_form.xml
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
            dialogTitle = "Hapus Data Jamaah"
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
                    val result = jamaahHelper.deleteById(jamaah?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@FormDataJamaahActivity, "Gagal menghapus data",
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
        binding.tvOnceDate.text = dateFormat.format(calendar.time)
    }

    // Mengambil tanggal dan jam
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/M/dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    companion object {
        const val EXTRA_JAMAAH = "extra_jamaah"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
        private const val DATE_PICKER_TAG = "DatePicker"
    }
}