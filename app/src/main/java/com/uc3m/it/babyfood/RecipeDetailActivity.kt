package com.uc3m.it.babyfood

import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private val apiKey = "6f63320e184e43b6b4f1c6ffbb74528c"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Botón volver a ApiActivity
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
           // val intent = Intent(this, ApiActivity::class.java)
            //startActivity(intent)
            //finish()
            onBackPressedDispatcher.onBackPressed() //cierra la actividad actual y vuelve a la anterior (ApiActivity)
        }

        val recipeId = intent.getIntExtra("recipeId", -1)

        if (recipeId != -1) {
            cargarReceta(recipeId)
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_button -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.calendar_button -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.search_button -> {
                  //  startActivity(Intent(this, ApiActivity::class.java))
                    finish()   // cierra la actividad actual, sin crear nada nuevo
                    true //indicamos que el botón de menú ya fue gestionado
                }
                R.id.favorites_button -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                else -> false
            }
        }
        // Sombreado de la lupa (search_button)
       // bottomNav.selectedItemId = R.id.search_button
        bottomNav.menu.findItem(R.id.search_button).isChecked = true
        //Dentro del menú buscamos el botón de buscar y con 'isChecked' lo marcamos como seleccionado

    }

    private fun cargarReceta(id: Int) {

        CoroutineScope(Dispatchers.IO).launch {

            val url = "https://api.spoonacular.com/recipes/$id/information?apiKey=$apiKey"
            val respuesta = URL(url).readText()

            val json = JSONObject(respuesta)

            val title = json.getString("title")
            val summary = json.getString("summary")

            val ingredientsArray = json.getJSONArray("extendedIngredients")

            val ingredientes = StringBuilder()

            for (i in 0 until ingredientsArray.length()) {
                val ingrediente = ingredientsArray.getJSONObject(i)
                ingredientes.append("• ")
                ingredientes.append(ingrediente.getString("original"))
                ingredientes.append("\n")
            }

            withContext(Dispatchers.Main) {

                findViewById<TextView>(R.id.txtTitle).text = title
                findViewById<TextView>(R.id.txtIngredients).text = ingredientes.toString()
                findViewById<TextView>(R.id.txtSummary).text = summary

            }

        }
    }
}