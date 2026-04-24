package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


//Clase y correcciones de fallos con GEMINI
class FoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BabyUtils.updateAge(this) //que actualiza la edad del bebé

        enableEdgeToEdge() //, para que el contenido pueda ir hasta los bordes de la pantalla.
        setContentView(R.layout.activity_food) //este será el layout que se sará como interfaz
        //R.layout  Para acceder a los recursos desde el código
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }// Sirve para que el contenido de tu app no se quede tapado por la cámara frontal o los botones de navegación del sistema, añadiendo un margen (padding) dinámico.

        // Botón volver
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configuración del spinner de meses
        val optionsMonths = arrayOf(
            "primer-quinto mes",
            "sexto mes",
            "séptimo mes",
            "octavo mes",
            "noveno mes",
            "décimo mes",
            "undécimo mes",
            "duodécimo mes",
            "más de un año"
        )

        val monthsValues = intArrayOf(
            4,   // primer-quinto mes
            6,   // sexto mes
            7,   // séptimo mes
            8,   // octavo mes
            9,   // noveno mes
            10,  // décimo mes
            11,  // undécimo mes
            12,  // duodécimo mes
            13   // más de un año
        )
        //adapter que mostrará las opciones en el spinner
        val adapterMonths = ArrayAdapter(this, android.R.layout.simple_spinner_item, optionsMonths)
        val selectOpts: Spinner = findViewById(R.id.spinnerMonths)
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selectOpts.adapter = adapterMonths

        // Ponemos la selección automática según la edad real del bebé
        val currentAge = BabyUtils.getAgeInMonths() //qué opción del spinner debe salir seleccionada
        val selectionIndex = when (currentAge) { //convierte la edad en meses a una posición del spinner
            in 0..5 -> 0
            6 -> 1
            7 -> 2
            8 -> 3
            9 -> 4
            10 -> 5
            11 -> 6
            12 -> 7
            else -> 8 // 12 meses o más
        }
        
        var lastValidPosition = selectionIndex //guarda la última posición permitida
        selectOpts.setSelection(selectionIndex) //hace que el spinner se ponga automáticamente en la edad real del bebé


        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.recyclerFoods)
        val searchEditText: EditText = findViewById(R.id.editSearch)//el buscador


        // Lista de alimentos
        val foodList = listOf(
            // Verduras y Hortalizas
            Food("Zanahoria", R.drawable.z_zanahoria),
            Food("Brócoli",R.drawable.z_brocoli),
            Food("Patata", R.drawable.z_patata),
            Food("Tomate",R.drawable.z_tomate),
            Food("Calabaza",R.drawable.z_calabaza),
            Food("Calabacín",R.drawable.z_calabacin),
            Food("Berenjena",R.drawable.z_berenjena),
            Food("Guisantes",R.drawable.z_guisantes),
            Food("Judías verdes",R.drawable.z_judiasverdes),
            Food("Puerro",R.drawable.z_puerro),
            Food("Batata",R.drawable.z_batata),
            Food("Coliflor",R.drawable.z_coliflor),
            Food("Pepino",R.drawable.z_pepino),

            // Frutas
            Food("Plátano",R.drawable.z_platano),
            Food("Manzana", R.drawable.z_manzana),
            Food("Pera",R.drawable.z_pera),
            Food("Naranja",R.drawable.z_naranja),
            Food("Uva",R.drawable.z_uva),
            Food("Aguacate",R.drawable.z_aguacate),
            Food("Mango",R.drawable.z_mango),
            Food("Papaya",R.drawable.z_papaya),
            Food("Fresa",R.drawable.z_fresa),
            Food("Arándanos",R.drawable.z_arandano),
            Food("Ciruela",R.drawable.z_ciruela),
            Food("Melocotón",R.drawable.z_melocoton),
            Food("Sandía",R.drawable.z_sandia),
            Food("Melón",R.drawable.z_melon),
            Food("Limon",R.drawable.z_limon),
            Food("Kiwi",R.drawable.z_kiwi),

            // Proteínas (Carnes, Pescados y Legumbres)
            Food("Pollo", R.drawable.z_pollo),
            Food("Vacuno", R.drawable.z_vacuno),
            Food("Cerdo",R.drawable.z_cerdo),
            Food("Pavo",R.drawable.z_pavo),
            Food("Conejo",R.drawable.z_conejo),
            Food("Huevo",R.drawable.z_huevo),
            Food("Merluza",R.drawable.z_merluza),
            Food("Salmón",R.drawable.z_salmon),
            Food("Lentejas",R.drawable.z_lentejas),
            Food("Garbanzos",R.drawable.z_garbanzos),
            Food("Alubias",R.drawable.z_alubias),
            Food("Tofu",R.drawable.z_tofu),

            // Cereales y Lácteos
            Food("Maíz",R.drawable.z_maiz),
            Food("Arroz",R.drawable.z_arroz),
            Food("Avena",R.drawable.z_avena),
            Food("Pasta",R.drawable.z_pasta),
            Food("Quinoa",R.drawable.z_quinoa),
            Food("Cuscús",R.drawable.z_cuscus),
            Food("Pan",R.drawable.z_pan),
            Food("Leche",R.drawable.z_leche),
            Food("Yogur natural",R.drawable.z_yougurt),
            Food("Queso fresco",R.drawable.z_queso),

            // Otros
            Food("Aceite de oliva",R.drawable.z_aceite)
        )

        val foodAdapter = FoodAdapter(emptyList()) //el adapter le meto la lista

        // Función para aplicar filtros de edad y búsqueda según la posición del spinner
        fun updateFoodList() {
            val spinnerPosition = selectOpts.selectedItemPosition
            val selectedMonths = monthsValues[spinnerPosition]

            val text = searchEditText.text.toString()
            if (spinnerPosition == 0) {
                foodAdapter.updateList(emptyList())
                Toast.makeText(this, "En esta etapa solo se recomienda leche materna", Toast.LENGTH_SHORT).show()
                return
            }
            val filteredList = foodList.filter { food ->
                val matchesSearch = food.name.contains(text, ignoreCase = true)

                val isAllowed = selectedMonths >= BabyUtils.getMinAgeForFood(food.name)

                matchesSearch && isAllowed
            }

            foodAdapter.updateList(filteredList)
        }

        // Listener del Spinner para controlar que no se elija una edad mayor a la real
        selectOpts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Aquí position es la opción que ha elegido el usuario
                if (position > selectionIndex) {
                    Toast.makeText(this@FoodActivity, "No puedes seleccionar una edad mayor a la de tu bebé", Toast.LENGTH_SHORT).show()
                    selectOpts.setSelection(lastValidPosition)
                } else {
                    lastValidPosition = position
                    updateFoodList()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Grid de 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = foodAdapter

        //listener para la búsqueda--- GEMINI
        searchEditText.addTextChangedListener {
            updateFoodList()
        }

        //GEMINI
        val btnContinue = findViewById<Button>(R.id.btnContinue)//Es una función que busca en tu archivo XML (activity_food.xml) un elemento que tenga el ID btnContinue
        // Para devolver un resultado a su actividad

        btnContinue.setOnClickListener {//listener que se activa cuando tocas el botón
            val selectedFoods = foodList
                .filter { it.isSelected }
                .map { it.name }
            if (selectedFoods.size > 2) {
                Toast.makeText(this, "Selecciona máximo 2 alimentos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ApiActivity::class.java)//El .class.java es necesario porque Android (que corre sobre una base de Java) necesita la referencia técnica de esa clase para poder abrirla.
            intent.putStringArrayListExtra("includeIngredients", ArrayList(selectedFoods)) //pasar la lista de alimentos seleccionados a ApiActivity

            intent.putExtra("selectedDate", getIntent().getStringExtra("selectedDate"))//enviamos fecha calendario
            intent.putExtra("mealType", getIntent().getStringExtra("mealType"))//enviamos tipo comida calendario

            startActivity(intent)
        }
    }
}
