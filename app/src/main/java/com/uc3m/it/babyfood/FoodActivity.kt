package com.uc3m.it.babyfood

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_food)
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

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            optionsMonths
        )
        val selectOpts: Spinner = findViewById<Spinner>(R.id.spinnerMonths)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selectOpts.adapter = adapter


        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.recyclerFoods)

        // Lista de alimentos (texto en español)
        val foodList = listOf(
            // Verduras y Hortalizas
            Food("Zanahoria"),
            Food("Brócoli"),
            Food("Patata"),
            Food("Tomate"),
            Food("Calabaza"),
            Food("Calabacín"),
            Food("Berenjena"),
            Food("Guisantes"),
            Food("Judías verdes"),
            Food("Puerro"),
            Food("Batata"),
            Food("Coliflor"),
            Food("Pepino"),

            // Frutas
            Food("Plátano"),
            Food("Manzana"),
            Food("Pera"),
            Food("Naranja"),
            Food("Mandarina"),
            Food("Uva"),
            Food("Aguacate"),
            Food("Mango"),
            Food("Papaya"),
            Food("Fresa"),
            Food("Arándanos"),
            Food("Ciruela"),
            Food("Melocotón"),
            Food("Sandía"),
            Food("Melón"),

            // Proteínas (Carnes, Pescados y Legumbres)
            Food("Pollo"),
            Food("Vacuno"),
            Food("Cerdo"),
            Food("Pavo"),
            Food("Conejo"),
            Food("Huevo"),
            Food("Merluza"),
            Food("Salmón"),
            Food("Lentejas"),
            Food("Garbanzos"),
            Food("Alubias"),
            Food("Tofu"),

            // Cereales y Lácteos
            Food("Maíz"),
            Food("Arroz"),
            Food("Avena"),
            Food("Pasta"),
            Food("Quinoa"),
            Food("Cuscús"),
            Food("Pan"),
            Food("Leche"),
            Food("Yogur natural"),
            Food("Queso fresco"),

            // Otros
            Food("Aceite de oliva")
        )

// Grid de 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)

// Adapter
        recyclerView.adapter = FoodAdapter(foodList)
    }



}