package id.madhanra.masjidapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Jamaah(
    var id: Int = 0,
    var nip: String? = null,
    var nama: String? = null,
    var alamat: String? = null,
    var jenKelamin: String? = null,
    var tglLahir: String? = null,
    var kontak: String? = null,
    var kategori: String? = null,
    var tglInput: String? = null
): Parcelable
