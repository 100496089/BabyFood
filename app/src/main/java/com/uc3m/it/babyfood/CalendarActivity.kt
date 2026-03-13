package com.uc3m.it.babyfood

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import android.widget.*
import android.content.Intent
///NUEVO
import com.google.android.material.bottomnavigation.BottomNavigationView

class CalendarActivity : AppCompatActivity() {

    private lateinit var db: MealsCalendarDB
    private var selectedDate = ""
    private val meals = mutableMapOf<String, String>() // clave: "fecha-tipo", valor: comida

    private lateinit var breakfastLayout: LinearLayout
    private lateinit var lunchLayout: LinearLayout
    private lateinit var snackLayout: LinearLayout
    private lateinit var dinnerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        ///NUEVO
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.calendar_button

        
        db = MealsCalendarDB(this)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.firstDayOfWeek = java.util.Calendar.MONDAY

        breakfastLayout = findViewById(R.id.breakfastLayout)
        lunchLayout = findViewById(R.id.lunchLayout)
        snackLayout = findViewById(R.id.snackLayout)
        dinnerLayout = findViewById(R.id.dinnerLayout)

        val today = java.util.Calendar.getInstance()
        val day = today.get(java.util.Calendar.DAY_OF_MONTH)
        val month = today.get(java.util.Calendar.MONTH) + 1
        val year = today.get(java.util.Calendar.YEAR)

        selectedDate = "$day/$month/$year"

        // Cargar comidas del día actual al abrir la app
        loadMeals()

        calendarView.setOnDateChangeListener { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
            loadMeals()
        }

        findViewById<Button>(R.id.addBreakfast).setOnClickListener {
            showInputUI("breakfast", breakfastLayout)
        }
        findViewById<Button>(R.id.addLunch).setOnClickListener {
            showInputUI("lunch", lunchLayout)
        }
        findViewById<Button>(R.id.addSnack).setOnClickListener {
            showInputUI("snack", snackLayout)
        }
        findViewById<Button>(R.id.addDinner).setOnClickListener {
            showInputUI("dinner", dinnerLayout)
        }
    }

    private fun showInputUI(type: String, container: LinearLayout) {
        container.removeAllViews()

        val input = EditText(this)
        input.hint = "Añadir..."

        val saveButton = Button(this)
        saveButton.text = "Guardar"

        val exploreButton = Button(this)
        exploreButton.text = "Explorar recetas"

        container.addView(input)
        container.addView(saveButton)
        container.addView(exploreButton)

        saveButton.setOnClickListener {
            val text = input.text.toString()
            db.insertMeal(selectedDate, type, text)
            loadMeals()
        }

        exploreButton.setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }
    }

    private fun loadMeals() {
        loadMealForType("breakfast", breakfastLayout)
        loadMealForType("lunch", lunchLayout)
        loadMealForType("snack", snackLayout)
        loadMealForType("dinner", dinnerLayout)
    }

    private fun loadMealForType(type: String, container: LinearLayout) {
        container.removeAllViews()

        // ---------------------------------------------
        // NUEVO: obtener comidas guardadas en SQL
        // ---------------------------------------------
        val meals = db.getMeals(selectedDate, type)   // NUEVO

        if (meals.isEmpty()) return   // NO mostramos "+" extra

        for (meal in meals) {
            //// NUEVO: crear recuadro tipo botón
            val button = Button(this)
            button.text = meal.text
            button.textSize = 16f
            button.setPadding(20, 20, 20, 20)

            //// NUEVO: estilo visual
            button.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,   // ancho
                200                                       // alto en píxeles (puedes cambiarlo)
            )
            params.setMargins(0, 10, 0, 10)               // márgenes opcionales
            button.layoutParams = params


            //// NUEVO: al pulsar → editar o eliminar
            button.setOnClickListener {
                showEditDeleteDialog(meal.id, meal.text, type)
            }

            container.addView(button)
        }
    }
    private fun showEditDeleteDialog(id: Int, oldText: String, type: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar comida")

        val input = EditText(this)
        input.setText(oldText)
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            db.updateMeal(id, input.text.toString()) //// NUEVO
            loadMeals()
        }

        builder.setNegativeButton("Eliminar") { _, _ ->
            db.deleteMeal(id) //// NUEVO
            loadMeals()
        }

        builder.setNeutralButton("Cancelar", null)

        builder.show()
    }
}
