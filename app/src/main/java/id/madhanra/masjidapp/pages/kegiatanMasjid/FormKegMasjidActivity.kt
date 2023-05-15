package id.madhanra.masjidapp.pages.kegiatanMasjid

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
import id.madhanra.masjidapp.pages.dataJamaah.FormDataJamaahActivity
import id.madhanra.masjidapp.databinding.ActivityFormKegMasjidBinding
import id.madhanra.masjidapp.db.DatabaseContract.KegMasjidColumns
import id.madhanra.masjidapp.db.helper.KegMasjidHelper
import id.madhanra.masjidapp.helper.DatePickerFragment
import id.madhanra.masjidapp.model.Kegiatan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormKegMasjidActivity :
    AppCompatActivity(), View.OnClickListener, DatePickerFragment.DialogDateListener {

    // Variabel yang dibutuhkan
    private var isEdit = false
    private var kegiatan: Kegiatan? = null
    private var position: Int = 0
    private lateinit var kegiatanHelper: KegMasjidHelper
    private lateinit var binding: ActivityFormKegMasjidBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormKegMasjidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kegiatanHelper = KegMasjidHelper.getInstance(applicationContext)
        kegiatanHelper.open()

        kegiatan = intent.getParcelableExtra(EXTRA_KEGIATAN)
        if (kegiatan != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            kegiatan = Kegiatan()
        }

        val adapterSpinner =
            ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.jenis_kegiatan))
        binding.spJenKeg.adapter = adapterSpinner

        val actionBarTitle: String
        val btnTitle: String
        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            kegiatan?.let {
                binding.edtNamaKeg.setText(it.namaKeg)
                binding.tvTglKegiatan.text = it.tglKeg
                binding.edtDeskripsi.setText(it.deskripsi)
                binding.edtPenganngungJwb.setText(it.penanggungJwbKeg)
                binding.spJenKeg.setSelection(adapterSpinner.getPosition(it.jenisKeg))
            }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnTglKeg.setOnClickListener(this)

        binding.btnSubmitKegiatan.text = btnTitle
        binding.btnSubmitKegiatan.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_tgl_keg -> {
                val datePickerFragment = DatePickerFragment()
                datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)
            }
            R.id.btn_submit_kegiatan -> {
                val namaKeg = binding.edtNamaKeg.text.toString().trim()
                val tglKegiatan = binding.tvTglKegiatan.text.toString()
                val deskripsi = binding.edtDeskripsi.text.toString().trim()
                val penanggungJwb = binding.edtPenganngungJwb.text.toString().trim()
                val jenKeg = binding.spJenKeg.selectedItem.toString()

                // Pesan error jika salah satu form tidak ada isinya
                if (namaKeg.isEmpty()) {
                    binding.edtNamaKeg.error = "Form tidak boleh kosong"
                    return
                }
                if (deskripsi.isEmpty()) {
                    binding.edtDeskripsi.error = "Form tidak boleh kosong"
                    return
                }
                if (tglKegiatan == getString(R.string.tv_tanggalKeg)){
                    binding.tvTglKegiatan.error = "Tanggal Kegiatan tidak boleh kosong"
                    return
                }

                kegiatan?.namaKeg = namaKeg
                kegiatan?.tglKeg = tglKegiatan
                kegiatan?.deskripsi = deskripsi
                kegiatan?.penanggungJwbKeg = penanggungJwb
                kegiatan?.jenisKeg = jenKeg

                val intent = Intent()
                intent.putExtra(EXTRA_KEGIATAN, kegiatan)
                intent.putExtra(EXTRA_POSITION, position)

                // Menginput data ke database
                val values = ContentValues()
                values.put(KegMasjidColumns.NAMAKEG, namaKeg)
                values.put(KegMasjidColumns.TGLKEG, tglKegiatan)
                values.put(KegMasjidColumns.DESKRIPSI, deskripsi)
                values.put(KegMasjidColumns.PENANGGUNGJWBKEG, penanggungJwb)
                values.put(KegMasjidColumns.JENISKEG, jenKeg)

                // Jika tujuannya untuk update
                if (isEdit) {
                    val result =
                        kegiatanHelper.update(kegiatan?.id.toString(), values).toLong()
                    if (result > 0) {
                        setResult(RESULT_UPDATE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormKegMasjidActivity,
                            "Gagal update data",
                            Toast.LENGTH_SHORT

                        ).show()
                    }
                } else {
                    // Jika tujuannya input baru
                    val result = kegiatanHelper.insert(values)
                    if (result > 0) {
                        kegiatan?.id = result.toInt()

                        setResult(RESULT_ADD, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormKegMasjidActivity,
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        showAlertDialog(FormDataJamaahActivity.ALERT_DIALOG_CLOSE)
        return super.getOnBackInvokedDispatcher()
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == FormDataJamaahActivity.ALERT_DIALOG_CLOSE
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
                    val result =
                        kegiatanHelper.deleteById(kegiatan?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FormKegMasjidActivity,
                            "Gagal menghapus data",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        binding.tvTglKegiatan.text = dateFormat.format(calendar.time)
    }
    companion object {
        const val EXTRA_KEGIATAN = "extra_kegiatan"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
        private const val DATE_PICKER_TAG = "DatePicker"
    }
}