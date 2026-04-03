package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class FavoritesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseAdapter
    private val apiKeyGoogle = BuildConfig.GOOGLE_TRANSLATE_API_KEY
    private lateinit var adapter: RecipeAdapter
    private val favoriteList = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        db = DatabaseAdapter(this).open()

        // Botón volver
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerFavorites)
        
        adapter = RecipeAdapter(favoriteList) { receta ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", receta.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }
//carga la base de datos
private fun cargarFavoritos() {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        val tempList = mutableListOf<Recipe>()
        val cursor = db.fetchAllFavorites()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_TITLE))
                val image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_IMAGE))

                val translatedTitle = translateText(title)

                tempList.add(
                    Recipe(
                        title = title,
                        translatedTitle = translatedTitle,
                        image = image,
                        id = id
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor?.close() //cierra recursos

        withContext(kotlinx.coroutines.Dispatchers.Main) {
            favoriteList.clear()
            favoriteList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }
    }
}

//ChatGPT
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
                    startActivity(Intent(this, FoodActivity::class.java))
                    true
                }
                R.id.favorites_button -> {
                    // Ya estamos en Favoritos
                    true
                }
                else -> false
            }
        }
        // Marcar el ítem de favoritos como seleccionado (corazón)
        bottomNav.selectedItemId = R.id.favorites_button
    }
    private suspend fun translateText(text: String): String = withContext(Dispatchers.IO) {
        try {
            //api de google translate
            val apiKey = apiKeyGoogle

            val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"
            //lo que le mando a google para que traduzca
            val body = JSONObject().apply {
                put("q", text) //el texto que quiero traducir
                put("source", "en") //el idioma original
                put("target", "es") //al que lo quiero traducir
                put("format", "text") //el formato
            }
            //conectamos con la api
            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            //Le dices que vas a enviar datos, no solo leer una URL
            connection.requestMethod = "POST"
            //manda un json
            connection.setRequestProperty("Content-Type", "application/json")
            //Esto habilita enviar datos en el cuerpo de la petición
            connection.doOutput = true
            //enviamos los datos
            connection.outputStream.use {
                it.write(body.toString().toByteArray())
            }
            //recoge la respuesta de Google como texto
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            val json = JSONObject(response)

            json.getJSONObject("data") //entras en data
                .getJSONArray("translations") //entras en translation
                .getJSONObject(0) //coges la primera traduccion
                .getString("translatedText") //sacas el texto traducido

        } catch (e: Exception) {
            text // si falla, devuelve original
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}