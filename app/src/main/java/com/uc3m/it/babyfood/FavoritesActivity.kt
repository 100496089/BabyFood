package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class FavoritesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseAdapter
    private lateinit var adapter: RecipeAdapter
    private val favoriteList = mutableListOf<Recipe>()

    // ChatGPT: Vinculamos la llave para traducir también aquí
    private val apiKeyGoogle = BuildConfig.GOOGLE_TRANSLATE_API_KEY

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

    // ChatGPT: Función de traducción para la lista
    private suspend fun translateText(text: String, source: String = "en", target: String = "es"): String = withContext(Dispatchers.IO) {
        if (text.isEmpty()) return@withContext ""
        try {
            val key = apiKeyGoogle
            val url = "https://translation.googleapis.com/language/translate/v2?key=$key"
            val body = JSONObject().apply {
                put("q", text)
                put("source", source)
                put("target", target)
                put("format", "text")
            }
            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            json.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText")
        } catch (e: Exception) {
            text
        }
    }

    private fun cargarFavoritos() {
        favoriteList.clear()
        val cursor = db.fetchAllFavorites()
        
        CoroutineScope(Dispatchers.IO).launch {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_TITLE))
                    val image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_IMAGE))
                    
                    // Traducimos el título guardado para mostrarlo en español
                    val translated = translateText(title, "en", "es")
                    
                    favoriteList.add(Recipe(title, translated, image, id))
                } while (cursor.moveToNext())
            }
            cursor?.close()
            
            withContext(Dispatchers.Main) {
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

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}