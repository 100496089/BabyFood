package com.uc3m.it.babyfood

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import android.widget.*
import android.content.Intent

class CalendarActivity : AppCompatActivity() {
    //Creamos variables globales
    private val mealsByDate= mutableMapOf<String, MutableList<String>>() // Guardamos las comidas por fecha y tipo
    private var selectedDate: String = "" //Guardamos la fecha seleccionada

    //Referencias a layouts de las comidas
    private lateinit var breakfastLayout: LinearLayout
    private lateinit var lunchLayout: LinearLayout
    private lateinit var snackLayout: LinearLayout
    private lateinit var dinnerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_calendar) //Conectamos con nuestro xml

        // Conectamos los elementos del XML con variables de Kotlin
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        breakfastLayout = findViewById(R.id.breakfastLayout)
        lunchLayout = findViewById(R.id.lunchLayout)
        snackLayout = findViewById(R.id.snackLayout)
        dinnerLayout = findViewById(R.id.dinnerLayout)

        val addBreakfastButton = findViewById<Button>(R.id.addBreakfast)
        val addLunchButton = findViewById<Button>(R.id.addLunch)
        val addSnackButton = findViewById<Button>(R.id.addSnack)
        val addDinnerButton = findViewById<Button>(R.id.addDinner)

        // Cuando el usuario selecciona un día del calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            loadMealsForDate(selectedDate) }

        //Boton
        addBreakfastButton.setOnClickListener { openMealScreen("Desayuno") }
        addLunchButton.setOnClickListener { openMealScreen("Almuerzo") }
        addSnackButton.setOnClickListener { openMealScreen("Merienda") }
        addDinnerButton.setOnClickListener { openMealScreen("Cena") }


    }
private fun openMealScreen(mealType: String) {
    val intent = Intent(this, MealDetailActivity::class.java)
    intent.putExtra("mealType", mealType)
    intent.putExtra("selectedDate", selectedDate)
    startActivity(intent)
}

    // Muestra un diálogo donde el usuario escribe la comida
private fun showAddMealDialog(mealType: String) {
    // Campo de texto donde el usuario escribe la comida
    val editText = EditText(this)
    editText.hint = "Write your meal"

    // Creamos el layout del diálogo para añadir el botón extra
    val layout = LinearLayout(this)
    layout.orientation = LinearLayout.VERTICAL
    layout.setPadding(50, 40, 50, 10)
    layout.addView(editText)

    // Botón extra para ir a la pantalla de tu compañera
    val exploreButton = Button(this)
    exploreButton.text = "Explore recipes"
    layout.addView(exploreButton)

    // Acción del botón que abre la otra pantalla
    exploreButton.setOnClickListener {
        val intent = Intent(this, MainActivity::class.java) // <-- cambia el nombre si tu compañera usa otro
        startActivity(intent)
    }

    // Construimos el diálogo
    AlertDialog.Builder(this)
        .setTitle("Add meal")
        .setMessage("Enter the meal for $mealType:")
        .setView(layout)
        .setPositiveButton("Save") { _, _ ->
            val mealText = editText.text.toString()

            // Guardamos la comida
            saveMeal(selectedDate, mealText, mealType)

            // Actualizamos la pantalla
            loadMealsForDate(selectedDate)
        }
        .setNegativeButton("Cancel", null)
        .show()
}


    // Guarda una comida asociada a una fecha y tipo
    private fun saveMeal(date: String, meal: String, mealType: String) {
        val key = "$date-$mealType"
        val list = mealsByDate.getOrPut(key) { mutableListOf() }
        list.add(meal)
    }

    // Carga todas las comidas de un día
    private fun loadMealsForDate(date: String) {
        loadMealsForType(date, "breakfast", breakfastLayout)
        loadMealsForType(date, "lunch", lunchLayout)
        loadMealsForType(date, "snack", snackLayout)
        loadMealsForType(date, "dinner", dinnerLayout)
    }

    // Carga las comidas de un tipo concreto
    private fun loadMealsForType(date: String, mealType: String, container: LinearLayout) {
        container.removeAllViews()
        val key = "$date-$mealType"
        val meals = mealsByDate[key] ?: emptyList()

        for (meal in meals) {
            val textView = TextView(this)
            textView.text = meal
            textView.textSize = 16f
            container.addView(textView)
        }
    }
}