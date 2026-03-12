package com.uc3m.it.babyfood

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

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->

            when(item.itemId){

                R.id.home_button -> {
                    true
                }

                R.id.search_button -> {
                        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
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
                        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
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
    //  Metodo que procesa la pulsacion (onClick) del boton Añadir Alimento
    fun addFood(view: View?){
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            AddFoodActivity::class.java
        )
        startActivity(intent)
    }

    //boton editar perfil
    fun profileChange(view: View?){
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            MainActivity::class.java
        )
        startActivity(intent)

    }

    //boton registro de alimentos
    fun foodRegister(view: View?){
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            FoodRegisterActivity::class.java
        )
        startActivity(intent)

    }



}