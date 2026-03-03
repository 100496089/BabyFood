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
            Food("Zanahoria"),
            Food("Plátano"),
            Food("Brócoli"),
            Food("Patata"),
            Food("Tomate"),
            Food("Calabaza")
        )

// Grid de 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)

// Adapter
        recyclerView.adapter = FoodAdapter(foodList)
    }



}