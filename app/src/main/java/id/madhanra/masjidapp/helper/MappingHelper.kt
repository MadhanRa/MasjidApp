package id.madhanra.masjidapp.helper

import android.database.Cursor
import id.madhanra.masjidapp.db.DatabaseContract.JamaahColumns
import id.madhanra.masjidapp.db.DatabaseContract.KegMasjidColumns
import id.madhanra.masjidapp.db.DatabaseContract.KasMasjidColumns
import id.madhanra.masjidapp.model.Jamaah
import id.madhanra.masjidapp.model.Kas
import id.madhanra.masjidapp.model.Kegiatan

object MappingHelper {

    fun mapCursorToArrayListJamaah(jamaahCursor: Cursor?): ArrayList<Jamaah> {
        val jamaahList = ArrayList<Jamaah>()
        jamaahCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(JamaahColumns._ID))
                val tglInput = getString(getColumnIndexOrThrow(JamaahColumns.TGLINPUT))
                val nip = getString(getColumnIndexOrThrow(JamaahColumns.NIP))
                val nama = getString(getColumnIndexOrThrow(JamaahColumns.NAMA))
                val jenKel = getString(getColumnIndexOrThrow(JamaahColumns.JENKELAMIN))
                val alamat = getString(getColumnIndexOrThrow(JamaahColumns.ALAMAT))
                val kontak = getString(getColumnIndexOrThrow(JamaahColumns.KONTAK))
                val kategori = getString(getColumnIndexOrThrow(JamaahColumns.KATEGORI))
                val tglLahir = getString(getColumnIndexOrThrow(JamaahColumns.TGLLAHIR))
                jamaahList.add(Jamaah(id, nip, nama, alamat, jenKel, tglLahir, kontak, kategori, tglInput))
            }
        }
        return jamaahList
    }

    fun mapCursorToArrayListKegMasjid(kegMasjidCursor: Cursor?): ArrayList<Kegiatan> {
        val kegiatanList = ArrayList<Kegiatan>()
        kegMasjidCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(KegMasjidColumns._ID))
                val namaKeg = getString(getColumnIndexOrThrow(KegMasjidColumns.NAMAKEG))
                val tglKeg = getString(getColumnIndexOrThrow(KegMasjidColumns.TGLKEG))
                val deskripsi = getString(getColumnIndexOrThrow(KegMasjidColumns.DESKRIPSI))
                val penganggungJwbKeg =
                    getString(getColumnIndexOrThrow(KegMasjidColumns.PENANGGUNGJWBKEG))
                kegiatanList.add(Kegiatan(id, namaKeg, tglKeg, deskripsi, penganggungJwbKeg))
            }
        }
        return kegiatanList
    }

    fun mapCursorToArrayListKasMasjid(kasMasjidCursor: Cursor?): ArrayList<Kas> {
        val kasList = ArrayList<Kas>()
        kasMasjidCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(KasMasjidColumns._ID))
                val keterangan =
                    getString(getColumnIndexOrThrow(KasMasjidColumns.KETERANGAN))
                val nominal =
                    getString(getColumnIndexOrThrow(KasMasjidColumns.NOMINAL))
                val kategori =
                    getString(getColumnIndexOrThrow(KasMasjidColumns.KATEGORI))
                val jenis =
                    getString(getColumnIndexOrThrow(KasMasjidColumns.JENIS))
                val tglInput =
                    getString(getColumnIndexOrThrow(KasMasjidColumns.TGLINPUT))
                kasList.add(Kas(id, keterangan, nominal, kategori, jenis, tglInput))
            }
        }
        return kasList
    }
}