package com.uc3m.it.babyfood

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView



class FoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() //, para que el contenido pueda ir hasta los bordes de la pantalla.
        setContentView(R.layout.activity_food) //este será el layout que se sará como interfaz
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//para el selector de meses

        val optionsMonths = arrayOf(
            "tercer mes",
            "cuarto mes",
            "quinto mes",
            "sexto mes",
            "séptimo mes",
            "octavo mes",
            "noveno mes",
            "décimo mes",
            "undécimo mes",
            "duodécimo mes"
        )
//adapter que mostrará las opciones en el spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            optionsMonths
        )
        val selectOpts: Spinner = findViewById<Spinner>(R.id.spinnerMonths)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)//es un layout XML que Google incluye dentro del propio framework de Android.
        selectOpts.adapter = adapter//Conectas el Spinner con el adapter para que muestre las opciones.


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

// Adapter
        val foodAdapter = FoodAdapter(foodList) //el adapter le meto la lista
        // Grid de 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = foodAdapter

        //listener para la búsqueda
        searchEditText.addTextChangedListener { text ->

            val filteredList = foodList.filter { food ->
                food.name.contains(
                    text.toString(),
                    ignoreCase = true //hace que no importe mayúsculas/minúsculas
                )
            }
            foodAdapter.updateList(filteredList)

    }

    }

}