package com.uc3m.it.babyfood

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import java.io.File

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_activity)

        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        
        val nombreBebe = prefs.getString("nombre", "Bebé")
        val welcomeText = findViewById<TextView>(R.id.welcomeMessage)
        welcomeText?.text = "¡Hola, $nombreBebe!"

        val imageViewBebe = findViewById<ShapeableImageView>(R.id.ImagenBebe)
        val photoPath = prefs.getString("foto_perfil", null)
        photoPath?.let {
            val file = File(it)
            if (file.exists()) {
                imageViewBebe.setImageURI(Uri.fromFile(file))
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home_button -> true
                R.id.search_button -> {
                    startActivity(Intent(this, FoodActivity::class.java))
                    true
                }
                R.id.favorites_button -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.calendar_button -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    fun addFood(view: View?){
        startActivity(Intent(this, AddFoodActivity::class.java))
    }

    fun profileChange(view: View?){
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.putExtra("isEditing", true) //nos permite enviar información entre activities. En este caso, para saber si estamos editando el perfil
        startActivity(intent)
    }

    fun foodRegister(view: View?){
        startActivity(Intent(this, FoodRegisterActivity::class.java))
    }

    fun openWeightChart(view: View?) {
        startActivity(Intent(this, WeightChartActivity::class.java))
    }

    fun openRecipes(view: View?) {
        startActivity(Intent(this, FoodActivity::class.java))
    }
}