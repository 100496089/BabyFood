package com.uc3m.it.babyfood

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseAdapter (private val mCtx: Context) {
    private var mDbHelper: DatabaseHelper? = null // onCreate() → crea tablas si no existen onUpgrade() → actualiza tablas si cambió la versión
    private var mDb: SQLiteDatabase? = null //insert(), query(), delete(), update()

    private class DatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DATABASE_CREATE)
            db.execSQL(DATABASE_CREATE_WEIGHTS)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion")
            // En una app real, aquí haríamos ALTER TABLE para no perder datos,
            // pero para desarrollo simplificamos borrando y creando.
            db.execSQL("DROP TABLE IF EXISTS $REGISTER_TABLE")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_WEIGHTS")
            onCreate(db)
        }
    }

    //Permito la escritura en las tablas con writableDatabase
    @Throws(SQLException::class)
    fun open(): DatabaseAdapter {
        mDbHelper = DatabaseHelper(mCtx)
        mDb = mDbHelper!!.writableDatabase //!! -> Confío en que la variable no es null, si lo es, lanza una excepción
        return this
    }

    fun close() {
        mDbHelper!!.close()
    }

    // --- MÉTODOS PARA ALIMENTOS ---

    fun createNote(name: String?, comment: String?, date: String?, photo: String?, rate: String?, category: String?): Long {
        val initialValues = ContentValues()
        initialValues.put(KEY_NAME, name)
        initialValues.put(KEY_COMMENT, comment)
        initialValues.put(KEY_DATE, date)
        initialValues.put(KEY_PHOTO, photo)
        initialValues.put(KEY_RATE, rate)
        initialValues.put(KEY_CATEGORY, category)
        return mDb!!.insert(REGISTER_TABLE, null, initialValues)
    }

    fun deleteNote(rowId: Long): Boolean {
        return mDb!!.delete(REGISTER_TABLE, KEY_ROWID + "=" + rowId, null) > 0
    }

    fun fetchAllNotes(): Cursor {
        return mDb!!.query(REGISTER_TABLE, arrayOf(KEY_ROWID, KEY_NAME, KEY_COMMENT, KEY_DATE, KEY_PHOTO, KEY_RATE, KEY_CATEGORY),
            null, null, null, null, null)
    }

    @Throws(SQLException::class)
    fun fetchNote(rowId: Long): Cursor {
        val mCursor = mDb!!.query(true, REGISTER_TABLE, arrayOf(KEY_ROWID, KEY_NAME, KEY_COMMENT, KEY_DATE, KEY_PHOTO, KEY_RATE, KEY_CATEGORY),
            KEY_ROWID + "=" + rowId, null, null, null, null, null)
        mCursor?.moveToFirst()
        return mCursor
    }

    //buscar nota por nombre
    fun fetchNotesBySearch(query: String): Cursor {
        return mDb!!.query(
            REGISTER_TABLE,
            arrayOf(KEY_ROWID, KEY_NAME, KEY_COMMENT, KEY_DATE, KEY_PHOTO, KEY_RATE, KEY_CATEGORY),
            "$KEY_NAME LIKE ?",
            arrayOf("%$query%"),
            null,
            null,
            null
        )
    }

    fun updateNote(rowId: Long, name: String?, comment: String?, date: String?, photo: String?, rate: String?, category: String?): Boolean {
        val args = ContentValues()
        args.put(KEY_NAME, name)
        args.put(KEY_COMMENT, comment)
        args.put(KEY_DATE, date)
        args.put(KEY_PHOTO, photo)
        args.put(KEY_RATE, rate)
        args.put(KEY_CATEGORY, category)
        return mDb!!.update(REGISTER_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0
    }

    // --- MÉTODOS PARA PESOS ---

    fun insertWeight(weight: Double, date: String): Long {
        val values = ContentValues()
        values.put(KEY_WEIGHT_VALUE, weight)
        values.put(KEY_WEIGHT_DATE, date)
        return mDb!!.insert(TABLE_WEIGHTS, null, values)
    }

    fun fetchAllWeights(): Cursor {
        return mDb!!.query(TABLE_WEIGHTS, arrayOf(KEY_WEIGHT_ID, KEY_WEIGHT_VALUE, KEY_WEIGHT_DATE), 
            null, null, null, null, "$KEY_WEIGHT_DATE ASC")
    }

    fun deleteWeight(id: Long): Boolean {
        return mDb!!.delete(TABLE_WEIGHTS, "$KEY_WEIGHT_ID=$id", null) > 0
    }

    companion object {
        private const val TAG = "DatabaseAdapter"
        private const val DATABASE_NAME = "AppDatabase"
        private const val REGISTER_TABLE = "Food"
        private const val TABLE_WEIGHTS = "Pesos"
        private const val DATABASE_VERSION = 7 // Incrementado de 6 a 7

        // Campos Alimentos
        const val KEY_ROWID = "_id"
        const val KEY_NAME = "Nombre"
        const val KEY_COMMENT = "Comentario"
        const val KEY_DATE = "Fecha"
        const val KEY_PHOTO = "Foto"
        const val KEY_RATE = "Calificacion"
        const val KEY_CATEGORY = "Categoria"

        // Campos Pesos
        const val KEY_WEIGHT_ID = "_id"
        const val KEY_WEIGHT_VALUE = "valor"
        const val KEY_WEIGHT_DATE = "fecha"

        private const val DATABASE_CREATE = "create table $REGISTER_TABLE (" +
                "$KEY_ROWID integer primary key autoincrement, " +
                "$KEY_NAME text not null, " +
                "$KEY_COMMENT text not null, " +
                "$KEY_DATE text not null, " +
                "$KEY_PHOTO text not null, " +
                "$KEY_RATE text not null," +
                "$KEY_CATEGORY text not null);"

        private const val DATABASE_CREATE_WEIGHTS = "create table $TABLE_WEIGHTS (" +
                "$KEY_WEIGHT_ID integer primary key autoincrement, " +
                "$KEY_WEIGHT_VALUE real not null, " +
                "$KEY_WEIGHT_DATE text not null);"
    }
}