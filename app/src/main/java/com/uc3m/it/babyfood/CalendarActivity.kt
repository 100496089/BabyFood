package com.uc3m.it.babyfood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.*
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView


class CalendarActivity : AppCompatActivity() {

    //ayuda IA (COPILOT)
    private lateinit var db: MealsCalendarDB //conexión con BD
    private var selectedDate = ""  //Para que el usuario selccione fecha
    //Contenedores donde se mostrarán las comidas
    private lateinit var breakfastLayout: LinearLayout
    private lateinit var lunchLayout: LinearLayout
    private lateinit var snackLayout: LinearLayout
    private lateinit var dinnerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar) //cargamos xml asociado

        // ayuda IA (COPILOT)
        //Para que al pulsar barra inferior cambiemos de pantalla
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.calendar_button

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){

                R.id.home_button -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }

                R.id.search_button -> {
                    startActivity(Intent(this, FoodActivity::class.java))
                    true
                }

                R.id.favorites_button -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }

                R.id.calendar_button -> true

                else -> false
            }
        }
        
        db = MealsCalendarDB(this)

        val calendarView = findViewById<CalendarView>(R.id.calendarView) //configura calendario
        calendarView.firstDayOfWeek = java.util.Calendar.MONDAY //hacemos que el primer día sea el lunes

        //Conectamos contenedores del xml con variables
        breakfastLayout = findViewById(R.id.breakfastLayout)
        lunchLayout = findViewById(R.id.lunchLayout)
        snackLayout = findViewById(R.id.snackLayout)
        dinnerLayout = findViewById(R.id.dinnerLayout)

        // Obtener la fecha actual
        val today = java.util.Calendar.getInstance()
        val day = today.get(java.util.Calendar.DAY_OF_MONTH)
        val month = today.get(java.util.Calendar.MONTH) + 1
        val year = today.get(java.util.Calendar.YEAR)

        selectedDate = "$day/$month/$year" // Formateamos fecha

        // Cargamos comidas del día actual al abrir la app
        loadMeals()

        calendarView.setOnDateChangeListener { _, year, month, day ->  selectedDate = "$day/${month + 1}/$year"
            loadMeals()
        }//Para que se carguen las comidas cuando el usuario cambie de día

        //Cuando se pulse boton de añadir comida hara una accion
        findViewById<Button>(R.id.addBreakfast).setOnClickListener {
            showInputUI("breakfast", breakfastLayout) //muestra formulario
        }
        findViewById<Button>(R.id.addLunch).setOnClickListener { showInputUI("lunch", lunchLayout) }
        findViewById<Button>(R.id.addSnack).setOnClickListener { showInputUI("snack", snackLayout) }
        findViewById<Button>(R.id.addDinner).setOnClickListener { showInputUI("dinner", dinnerLayout)}

    }

    //ayuda IA (COPILOT)
    //Accion que hace al pulsar el (+) , enseña formulario
    private fun showInputUI(type: String, container: LinearLayout) {
        container.removeAllViews()//borramos temporalmente comidas para enseñar formulario

        val input = EditText(this) //campo para que el usuario escriba
        input.hint = "Añadir..." //letras sombradas

        val saveButton = Button(this) //boton para guardar
        saveButton.text = "Guardar"

        val exploreButton = Button(this) //boton para explorar
        exploreButton.text = "Explorar recetas"

        //hacemos que sean visibles dentro del contenedor
        container.addView(input)
        container.addView(saveButton)
        container.addView(exploreButton)

        saveButton.setOnClickListener {
            val text = input.text.toString() //obtenemos el texto
            db.insertMeal(selectedDate, type, text) //lo almacenamos en la BD
            loadMeals() //cargamos comidas para que se muestren
        }

        exploreButton.setOnClickListener { //cuando se pulsa botón nos lleva a otra pantalla
            //startActivity(Intent(this, FoodActivity::class.java))
            val intent = Intent(this, FoodActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("mealType", type)
            startActivity(intent)
        }
    }

    private fun loadMeals() { //dentro llamamos a otra funcion para cada comida
        loadMealForType("breakfast", breakfastLayout)
        loadMealForType("lunch", lunchLayout)
        loadMealForType("snack", snackLayout)
        loadMealForType("dinner", dinnerLayout)
    }

    //ayuda IA (COPILOT)
    private fun loadMealForType(type: String, container: LinearLayout) { //cargamos comidas guardadas
        container.removeAllViews() //limpiamos para repintar desde cero si cambiamos de día

        // Pedimos a la BD todas las comidas del dia y para cada tipo
        val meals = db.getMeals(selectedDate, type)

        if (meals.isEmpty()) return   // Si no hay comidas no mostramos nada

        for (meal in meals) {
            //Creamos recuadro tipo botón para las comidas
            val button = Button(this)
            button.text = meal.text //Se ajusta al tamaño del texto que introduce el usuario
            button.textSize = 16f
            button.isAllCaps = false
            button.setPadding(20, 20, 20, 20) //espacio interno entre borde y texto

            //Le ponemos un estilo al recuadro mediante un xml
            button.setBackgroundResource(R.drawable.button_calendar_meals)

            //Definimos como se va a colocar el recuadro dentro del LinearLayout
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200) //ancho y alto
            params.setMargins(0, 10, 0, 10)  // márgenes externos
            button.layoutParams = params

            //Definimos que es lo que pasa al pulsar recuadro comida: editar o eliminar
            button.setOnClickListener {
                if (meal.recipeId != null) {
                    val intent = Intent(this, RecipeDetailActivity::class.java)
                    intent.putExtra("recipeId", meal.recipeId)
                    startActivity(intent)
                } else {
                    showEditDeleteDialog(meal.id, meal.text, type)
                }
            }
            container.addView(button) //Añadimos botón al LinearLayout
        }
    }

    //ayuda IA (COPILOT) + ejemplo clase
    //Muestra formulario para editar o eliminar comida
    private fun showEditDeleteDialog(id: Int, oldText: String, type: String) {
        val builder = AlertDialog.Builder(this) //Creamos constructor de dialogo
        builder.setTitle("Editar comida") //Título del dialogo

        val input = EditText(this) //Creamos editext que el usuario pueda escribir
        input.setText(oldText) //Muestra lo actual para que se pueda modificar
        builder.setView(input) //Insertamos el edit text en el dialogo

        //Botón de guardar hace que se actulice la comida
        builder.setPositiveButton("Guardar") { _, _ -> db.updateMeal(id, input.text.toString())
            loadMeals()
        }
        //Botón de eliminar hace que se elimine la comida
        builder.setNegativeButton("Eliminar") { _, _ -> db.deleteMeal(id)
            loadMeals()
        }

        //Botón de cancelar ncierra el dialogo
        builder.setNeutralButton("Cancelar", null)

        builder.show() //Mostramos dialogo en pantalla
    }

}
