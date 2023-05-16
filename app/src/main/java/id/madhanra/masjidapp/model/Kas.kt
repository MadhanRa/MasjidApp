package id.madhanra.masjidapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Kas(
    var id: Int = 0,
    var keterangan: String? = null,
    var nominal: String? = null,
    var kategori: String? = null,
    var jenis: String? = null,
    var tglInput: String? = null
): Parcelable
