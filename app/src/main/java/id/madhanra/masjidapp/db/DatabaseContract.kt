package id.madhanra.masjidapp.db

import android.provider.BaseColumns

class DatabaseContract {
    internal class JamaahColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "jamaah"
            const val _ID = "_id"
            const val NAMA = "nama"
            const val NIP = "nip"
            const val JENKELAMIN = "jenKelamin"
            const val ALAMAT = "alamt"
            const val KATEGORI = "kategori"
            const val KONTAK = "kontak"
            const val TGLLAHIR = "tglLahir"
            const val TGLINPUT = "tglInput"
        }
    }

    internal class KegMasjidColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "kegMasjid"
            const val _ID = "_id"
            const val NAMAKEG = "namaKeg"
            const val TGLKEG = "tglKeg"
            const val DESKRIPSI = "deskripsi"
            const val PENANGGUNGJWBKEG = "penanggungJwbKeg"
            const val JENISKEG = "jenisKegiatan"
        }
    }
}