package com.uc3m.it.babyfood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_activity)

        // Recuperar el nombre guardado para el mensaje de bienvenida
        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val nombreBebe = prefs.getString("nombre", "Bebé")
        val welcomeText = findViewById<TextView>(R.id.welcomeMessage)
        if (welcomeText != null) {
            welcomeText.text = "¡Hola, $nombreBebe!"
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->

            when(item.itemId){

                R.id.home_button -> {
                    true
                }

                R.id.search_button -> {
                        val intent = Intent(
                            this,
                            FoodActivity::class.java
                        )
                        startActivity(intent)
                    true
                }

                R.id.favorites_button -> {
                    true
                }

                R.id.calendar_button -> {
                        val intent = Intent(
                            this,
                            CalendarActivity::class.java
                        )
                        startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    fun addFood(view: View?){
        val intent = Intent(
            this,
            AddFoodActivity::class.java
        )
        startActivity(intent)
    }

    //boton editar perfil
    fun profileChange(view: View?){
        val intent = Intent(
            this,
            MainMenuActivity::class.java
        )
        intent.putExtra("isEditing", true)
        startActivity(intent)
    }

    fun foodRegister(view: View?){
        val intent = Intent(
            this,
            FoodRegisterActivity::class.java
        )
        startActivity(intent)
    }
}