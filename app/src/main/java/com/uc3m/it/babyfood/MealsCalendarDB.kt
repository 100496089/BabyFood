package com.uc3m.it.babyfood

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


//ayuda IA (COPILOT) + clase
class MealsCalendarDB(context: Context) :SQLiteOpenHelper(context, "meals.db", null, 1) { //nombre de la BD

    override fun onCreate(db: SQLiteDatabase) { //Creamos tabla con columnas
        db.execSQL("""
            CREATE TABLE meals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                text TEXT NOT NULL,
                recipeId INTEGER 
            )
        """)
    }

    //Por si caambiamos de versión en un futuro (si se quita no afectaria ahora)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS meals")
        onCreate(db)
    }

    //Guardamos una comida en la BD
    fun insertMeal(date: String, type: String, text: String, recipeId: Int? = null) {
        val db = writableDatabase  //Abrimos en modo escritura
        val values = ContentValues()
        values.put("date", date)  //Añadimos valores
        values.put("type", type)
        values.put("text", text)
        values.put("recipeId", recipeId)
        db.insert("meals", null, values)  //Insertamos fila en tabla
    }

    //Comida que se lee de la BD
    data class Meal(val id: Int, val text: String,val recipeId: Int? = null)

    //Leemos comidas de la BD
    fun getMeals(date: String, type: String): List<Meal> {
        val db = readableDatabase  //Abrimos en modo lectura
        val cursor = db.rawQuery(
            "SELECT id, text, recipeId FROM meals WHERE date=? AND type=?",
            arrayOf(date, type)
        )//Buscamos en filas por "date" y "type"

        val list = mutableListOf<Meal>()  //Lista donde guaradamos resultados
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val text = cursor.getString(1)
            val recipeId = if (!cursor.isNull(2)) cursor.getInt(2) else null

            list.add(Meal(id, text, recipeId))
        }
        cursor.close()
        return list
    }

    //Editamos comida de la BD
    fun updateMeal(id: Int, newText: String) {
        val db = writableDatabase //Abrimos en modo escritura
        val values = ContentValues()
        values.put("text", newText)
        db.update("meals", values, "id=?", arrayOf(id.toString())) //Actualizamos segun id
    }

    //Borramos de la BD
    fun deleteMeal(id: Int) {
        val db = writableDatabase //Abrimos en modo escritura
        db.delete("meals", "id=?", arrayOf(id.toString())) //Borro dependiendo de id
    }

}
