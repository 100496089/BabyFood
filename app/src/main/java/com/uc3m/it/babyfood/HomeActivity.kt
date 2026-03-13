package com.uc3m.it.babyfood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_activity)

        // Referencia al TextView de bienvenida
        val textViewBienvenida = findViewById<TextView>(R.id.textViewBienvenida)

        // Cargar el nombre desde SharedPreferences
        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "")

        // Actualizar el texto
        if (!nombre.isNullOrEmpty()) {
            textViewBienvenida.text = "Hola, $nombre"
        } else {
            textViewBienvenida.text = "Hola"
        }
    }
    //  Metodo que procesa la pulsacion (onClick) del boton Añadir Alimento
    fun addFood(view: View?){
        val intent = Intent(this, AddFoodActivity::class.java)
        startActivity(intent)
    }

    // Boton editar perfil
    fun profileChange(view: View?){
        val intent = Intent(this, MainMenuActivity::class.java)
        // Pasamos un flag para indicar que queremos editar, para que no nos redirija de vuelta
        intent.putExtra("isEditing", true)
        startActivity(intent)
    }

    // Boton registro de alimentos
    fun foodRegister(view: View?){
        val intent = Intent(this, FoodRegisterActivity::class.java)
        startActivity(intent)
    }
}