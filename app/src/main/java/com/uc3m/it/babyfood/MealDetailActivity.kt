package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MealDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_detail)

        val mealType = intent.getStringExtra("mealType") ?: ""
        val selectedDate = intent.getStringExtra("selectedDate") ?: ""

        val title = findViewById<TextView>(R.id.tvMealTitle)
        val input = findViewById<EditText>(R.id.etMealInput)
        val saveButton = findViewById<Button>(R.id.btnSaveMeal)
        val exploreButton = findViewById<Button>(R.id.btnExploreRecipes)

        title.text = "$mealType - $selectedDate"

        saveButton.setOnClickListener {
            val text = input.text.toString()
            // Aquí puedes guardar el texto si quieres
            finish() // vuelve atrás
        }

        exploreButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
