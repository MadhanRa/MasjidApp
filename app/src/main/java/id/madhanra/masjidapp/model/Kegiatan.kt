package id.madhanra.masjidapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Kegiatan(
    var id: Int = 0,
    var namaKeg: String? = null,
    var tglKeg: String? = null,
    var deskripsi: String? = null,
    var penanggungJwbKeg: String? = null,
    var jenisKeg: String? = null
): Parcelable
