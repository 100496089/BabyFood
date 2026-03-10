package com.uc3m.it.babyfood

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

// Clase adaptadora que nos va a facilitar el uso de la BD
class FoodRegisterAdapter (private val mCtx: Context) { // Se encarga de abrir, cerrar y manipular la base de datos.
    private var mDbHelper: DatabaseHelper? = null // ayuda a crear y actualizar la base de datos.
    private var mDb: SQLiteDatabase? = null // representa la base de datos abierta para poder hacer operaciones.

    private class DatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) { // se ejecuta la primera vez para crear la base de datos
            db.execSQL(DATABASE_CREATE) // ejecuta la sentencia SQL para crear la tabla
        }
        // se ejecuta cuando cambias la version de la base de datos
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                TAG, ("Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data")
            )
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE)
            onCreate(db)
        }
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    @Throws(SQLException::class)
    // abre la base de datos en modo escritura
    fun open(): FoodRegisterAdapter {
        mDbHelper = DatabaseHelper(mCtx)
        mDb = mDbHelper!!.writableDatabase
        return this
    }
    //cierra la base de datos
    fun close() {
        mDbHelper!!.close()
    }

    fun createNote(name: String?, comment: String?, date: String?): Long {
        val initialValues = ContentValues()
        initialValues.put(KEY_NAME, name) // crea el content values
        initialValues.put(KEY_COMMENT, comment)
        initialValues.put(KEY_DATE, date)


        return mDb!!.insert(DATABASE_TABLE, null, initialValues) //inserta los datos en la tabla
    }

    fun deleteNote(rowId: Long): Boolean { //borra una fila por su id
        return mDb!!.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0
    }

    fun fetchAllNotes(): Cursor { //para que te de todas los regstros guardados en la base de datos
        return mDb!!.query(
            DATABASE_TABLE, arrayOf(
                KEY_ROWID,KEY_NAME,
                KEY_COMMENT, KEY_DATE
            ), null, null, null, null, null
        )
    }

    @Throws(SQLException::class)
    fun fetchNote(rowId: Long): Cursor { //para que te de un registro de la base de datos
        val mCursor =
            mDb!!.query(
                true, DATABASE_TABLE, arrayOf(
                    KEY_ROWID,
                    KEY_NAME, KEY_COMMENT, KEY_DATE
                ), KEY_ROWID + "=" + rowId, null,
                null, null, null, null
            )
        if (mCursor != null) {
            mCursor.moveToFirst()
        }
        return mCursor
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the name and comment
     * values passed in
     *
     * @param rowId id of note to update
     * @param name value to set note name to
     * @param comment value to set note comment to
     * @return true if the note was successfully updated, false otherwise
     */
    fun updateNote(rowId: Long, name: String?, comment: String?, date: String?): Boolean { //actualiza una nota existente
        val args = ContentValues()
        args.put(KEY_NAME, name)
        args.put(KEY_COMMENT, comment)
        args.put(KEY_DATE, date)

        return mDb!!.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0
    }

    companion object {
        private const val TAG = "APMOV: NotesDbAdapter" // Usado en los mensajes de Log

        //Nombre de la base de datos, tablas (en este caso una) y versión
        private const val DATABASE_NAME = "RegisteredFood" // nombre de la base de datos
        private const val DATABASE_TABLE = "Food" // nombre de cada registro que se haga en la base de datos
        private const val DATABASE_VERSION = 4

        //campos de la tabla de la base de datos
        const val KEY_NAME: String = "Nombre"
        const val KEY_COMMENT: String = "Comentario"
        const val KEY_DATE: String = "Fecha"
        const val KEY_ROWID: String = "_id"

        // Sentencia SQL para crear las tablas de las bases de datos cuando la app se inicia por primera vez
        private const val DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" +
                KEY_ROWID + " integer primary key autoincrement, " +
                KEY_NAME + " text not null, " +
                KEY_COMMENT + " text not null, "+
                KEY_DATE + " text not null "+");"
    }
}
