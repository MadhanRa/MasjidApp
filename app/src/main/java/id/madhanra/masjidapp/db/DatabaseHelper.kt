package id.madhanra.masjidapp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import id.madhanra.masjidapp.db.DatabaseContract.JamaahColumns
import id.madhanra.masjidapp.db.DatabaseContract.KegMasjidColumns
import id.madhanra.masjidapp.db.DatabaseContract.KasMasjidColumns

class DatabaseHelper (context: Context) :
    SQLiteOpenHelper (context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TABLE_JAMAAH)
        db?.execSQL(SQL_CREATE_TABLE_KEGMASJID)
        db?.execSQL(SQL_CREATE_TABLE_KASMASJID)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${JamaahColumns.TABLE_NAME}")
        db?.execSQL("DROP TABLE IF EXISTS ${KegMasjidColumns.TABLE_NAME}")
        db?.execSQL("DROP TABLE IF EXISTS ${KasMasjidColumns.TABLE_NAME}")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "dbMasjidApp"
        private const val DATABASE_VERSION = 1

        private const val SQL_CREATE_TABLE_JAMAAH = "CREATE TABLE ${JamaahColumns.TABLE_NAME}" +
                " (${JamaahColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${JamaahColumns.NIP} TEXT NOT NULL," +
                " ${JamaahColumns.NAMA} TEXT NOT NULL," +
                " ${JamaahColumns.JENKELAMIN} TEXT NOT NULL," +
                " ${JamaahColumns.TGLLAHIR} TEXT NOT NULL," +
                " ${JamaahColumns.ALAMAT} TEXT NOT NULL," +
                " ${JamaahColumns.KONTAK} TEXT NOT NULL," +
                " ${JamaahColumns.KATEGORI} TEXT NOT NULL," +
                " ${JamaahColumns.TGLINPUT} TEXT NOT NULL)"

        private const val SQL_CREATE_TABLE_KEGMASJID =
            "CREATE TABLE ${KegMasjidColumns.TABLE_NAME}" +
                " (${KegMasjidColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${KegMasjidColumns.NAMAKEG} TEXT NOT NULL," +
                " ${KegMasjidColumns.TGLKEG} TEXT NOT NULL," +
                " ${KegMasjidColumns.DESKRIPSI} TEXT NOT NULL," +
                " ${KegMasjidColumns.PENANGGUNGJWBKEG} TEXT NOT NULL," +
                " ${KegMasjidColumns.JENISKEG} TEXT NOT NULL)"

        private const val SQL_CREATE_TABLE_KASMASJID =
            "CREATE TABLE ${KasMasjidColumns.TABLE_NAME}" +
                    " (${KasMasjidColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ${KasMasjidColumns.KETERANGAN} TEXT NOT NULL," +
                    " ${KasMasjidColumns.NOMINAL} TEXT NOT NULL," +
                    " ${KasMasjidColumns.KATEGORI} TEXT NOT NULL," +
                    " ${KasMasjidColumns.JENIS} TEXT NOT NULL," +
                    " ${KasMasjidColumns.TGLINPUT} TEXT NOT NULL)"
    }
}