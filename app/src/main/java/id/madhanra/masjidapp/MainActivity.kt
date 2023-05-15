package id.madhanra.masjidapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import id.madhanra.masjidapp.pages.dataJamaah.DataJamaahActivity
import id.madhanra.masjidapp.databinding.ActivityMainBinding
import id.madhanra.masjidapp.pages.kegiatanMasjid.KegiatanMasjidActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDataJamaah.setOnClickListener(this)
        binding.btnKegiatanMasjid.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_data_jamaah -> {
                val intent = Intent(this@MainActivity, DataJamaahActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_kegiatan_masjid -> {
                val intent = Intent(
                    this@MainActivity,
                    KegiatanMasjidActivity::class.java)
                startActivity(intent)
            }
        }
    }
}