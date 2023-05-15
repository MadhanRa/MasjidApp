package id.madhanra.masjidapp.db.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import id.madhanra.masjidapp.db.DatabaseContract.JamaahColumns.Companion.TABLE_NAME
import id.madhanra.masjidapp.db.DatabaseContract.JamaahColumns.Companion._ID
import id.madhanra.masjidapp.db.DatabaseHelper
import java.sql.SQLException

class JamaahHelper (context: Context) {
    // Variabel yang dibutuhkan
    private var dataBaseHelper: DatabaseHelper = DatabaseHelper(context)
    private lateinit var database: SQLiteDatabase
    companion object {
        private const val DATABASE_TABLE = TABLE_NAME
        // Untuk menginisiasi database
        private var INSTANCE: JamaahHelper? = null
        fun getInstance(context: Context): JamaahHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: JamaahHelper(context)
            }
    }
    // Membuka database
    @Throws(SQLException::class)
    fun open() {
        database = dataBaseHelper.writableDatabase
    }
    // Menutup database
    fun close() {
        dataBaseHelper.close()
        if (database.isOpen) {
            database.close()
        }
    }

    // Mengambil data
    fun queryAll(): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            null,
            null,
            null,
            null,
            "$_ID ASC"
        )
    }
    // Mengambil dengan id tertentu
    fun queryById(id: String): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            "$_ID = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }
    // Menyimpan data
    fun insert(values: ContentValues?): Long {
        return database.insert(DATABASE_TABLE, null, values)
    }
    // Memperbarui data
    fun update(id: String, values: ContentValues?): Int {
        return database.update(DATABASE_TABLE, values, "$_ID = ?", arrayOf(id))
    }
    // Menghapus data
    fun deleteById(id: String): Int {
        return database.delete(DATABASE_TABLE, "$_ID = '$id'", null)
    }
}