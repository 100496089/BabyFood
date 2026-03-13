package com.uc3m.it.babyfood

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MealsCalendarDB(context: Context) :
    SQLiteOpenHelper(context, "meals.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE meals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                text TEXT NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS meals")
        onCreate(db)
    }

    fun insertMeal(date: String, type: String, text: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("date", date)
        values.put("type", type)
        values.put("text", text)
        db.insert("meals", null, values)
    }

    data class Meal(val id: Int, val text: String)

    fun getMeals(date: String, type: String): List<Meal> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, text FROM meals WHERE date=? AND type=?",
            arrayOf(date, type)
        )

        val list = mutableListOf<Meal>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val text = cursor.getString(1)
            list.add(Meal(id, text))
        }
        cursor.close()
        return list
    }
    fun updateMeal(id: Int, newText: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("text", newText)
        db.update("meals", values, "id=?", arrayOf(id.toString()))
    }

    fun deleteMeal(id: Int) {
        val db = writableDatabase
        db.delete("meals", "id=?", arrayOf(id.toString()))
    }

}
