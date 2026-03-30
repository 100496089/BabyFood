package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class FavoritesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseAdapter
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

    private fun cargarFavoritos() {
        favoriteList.clear()
        val cursor = db.fetchAllFavorites()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_TITLE))
                val image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_FAV_IMAGE))
                favoriteList.add(Recipe(title, image, id))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        adapter.notifyDataSetChanged()
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