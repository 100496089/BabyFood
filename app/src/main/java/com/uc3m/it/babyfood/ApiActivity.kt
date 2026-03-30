package com.uc3m.it.babyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class ApiActivity : AppCompatActivity() {

//Ayuda de Gemini y de la pagina web de Spoonacular

    //private val apiKey = "0b42d0c40af044c8a21ee108e502dd6b"
    private val apiKey ="6f63320e184e43b6b4f1c6ffbb74528c"
    private lateinit var adapter: RecipeAdapter //declaro el adapter pero se inicializará mas tarde
    private val recipeList = mutableListOf<Recipe>() // Lista de recetas que llegan de la api
    //se trata de una lista que se puede modificar


    override fun onCreate(savedInstanceState: Bundle?) {//carga layout y componentes
        super.onCreate(savedInstanceState)
        BabyUtils.updateAge(this)
        setContentView(R.layout.activity_api)

        // Botón volver al foodActivity
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, FoodActivity::class.java)
            startActivity(intent)
            finish()

        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerRecetas)

        adapter = RecipeAdapter(recipeList){ receta ->

            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", receta.id)
            startActivity(intent)

        }
        recyclerView.layoutManager = LinearLayoutManager(this)//linear layout para que las recetas se muestren en una columna
        recyclerView.adapter = adapter

        setupBottomNavigation()
        obtenerTodasLasRecetas()
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
                    // Ya estamos en ApiActivity (búsqueda)
                    true
                }
                R.id.favorites_button -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    // Lógica para favoritos si existe
                    true
                }
                else -> false
            }
        }
        // Marcar el ítem de búsqueda como seleccionado (lupa)
        bottomNav.selectedItemId = R.id.search_button
    }

    private fun translate(food: String): String {
        return when (food.lowercase()) {
            // Verduras y hortalizas
            "zanahoria" -> "carrot"
            "brócoli" -> "broccoli"
            "patata" -> "potato"
            "tomate" -> "tomato"
            "calabaza" -> "pumpkin"
            "calabacín" -> "zucchini"
            "berenjena" -> "eggplant"
            "guisantes" -> "peas"
            "judías verdes" -> "green beans"
            "puerro" -> "leek"
            "batata" -> "sweet potato"
            "coliflor" -> "cauliflower"
            "pepino" -> "cucumber"

            // Frutas
            "plátano" -> "banana"
            "manzana" -> "apple"
            "pera" -> "pear"
            "naranja" -> "orange"
            "uva" -> "grape"
            "aguacate" -> "avocado"
            "mango" -> "mango"
            "papaya" -> "papaya"
            "fresa" -> "strawberry"
            "arándanos" -> "blueberries"
            "ciruela" -> "plum"
            "melocotón" -> "peach"
            "sandía" -> "watermelon"
            "melón" -> "melon"
            "limon" -> "lemon"
            "limón" -> "lemon"
            "kiwi" -> "kiwi"

            // Proteínas
            "pollo" -> "chicken"
            "vacuno" -> "beef"
            "cerdo" -> "pork"
            "pavo" -> "turkey"
            "conejo" -> "rabbit"
            "huevo" -> "egg"
            "merluza" -> "hake"
            "salmón" -> "salmon"
            "lentejas" -> "lentils"
            "garbanzos" -> "chickpeas"
            "alubias" -> "beans"
            "tofu" -> "tofu"

            // Cereales y lácteos
            "maíz" -> "corn"
            "arroz" -> "rice"
            "avena" -> "oats"
            "pasta" -> "pasta"
            "quinoa" -> "quinoa"
            "cuscús" -> "couscous"
            "pan" -> "bread"
            "leche" -> "milk"
            "yogur natural" -> "plain yogurt"
            "queso fresco" -> "fresh cheese"

            // Otros
            "aceite de oliva" -> "olive oil"

            else -> food.lowercase()
        }
    }

    private fun obtenerTodasLasRecetas() {
        val includeIngredients = intent.getStringArrayListExtra("includeIngredients") ?: arrayListOf()
        val excludeIngredients = BabyUtils.getExcludedFoods()
        
        // Categorías que buscamos para dar variedad (purés, muffins, gachas, tortitas)
        val types = listOf("puree", "muffins", "porridge", "pancakes")

//Android no permite hacer llamadas a internet en el hilo principal, porque bloquearía la app.
        CoroutineScope(Dispatchers.IO).launch { //forma de ejecutar tareas en segundo plano sin bloquear la app
            try {
                val allRecipes = mutableListOf<Recipe>()
                val seenIds = mutableSetOf<Int>()

                // Traducimos ingredientes seleccionados
                val ingredientsQuery = includeIngredients.joinToString(" ") { translate(it) }
                
                // Preparamos los excluidos (evitando excluir lo que el usuario ha seleccionado)
                val translatedSelected = includeIngredients.map { translate(it).lowercase() }
                val excludeParam = excludeIngredients
                    .map { translate(it).lowercase() }
                    .filter { it !in translatedSelected }
                    .joinToString(",")

                for (type in types) {
                    // Formato de búsqueda: "manzana puree", "manzana muffins", etc.
                    val finalQuery = "$ingredientsQuery $type".trim()
                    
                    // Si no hay nada seleccionado, buscamos por categoría genérica de bebé
                    val searchQuery = if (finalQuery.isEmpty()) "baby $type" else finalQuery

                    // Usamos solo la 'query' para que la búsqueda sea flexible como antes
                    val urlString = "https://api.spoonacular.com/recipes/complexSearch?" +
                            "query=${URLEncoder.encode(searchQuery, "UTF-8")}" +
                            "&excludeIngredients=${URLEncoder.encode(excludeParam, "UTF-8")}" +
                            "&number=5" + 
                            "&apiKey=$apiKey"

                    val respuesta = URL(urlString).readText()
                    Log.d("API_RESPUESTA", "Buscando $type: $respuesta")
                    
                    val json = JSONObject(respuesta)
                    val listaJson = json.getJSONArray("results")

                    for (i in 0 until listaJson.length()) {
                        val item = listaJson.getJSONObject(i)
                        val id = item.getInt("id")
                        if (!seenIds.contains(id)) {
                            seenIds.add(id)
                            allRecipes.add(Recipe(
                                item.getString("title"),
                                item.getString("image"),
                                id
                            ))
                        }
                    }
                }

                withContext(Dispatchers.Main) {//actualizar la lista de recetas en el hilo principal
                    recipeList.clear()
                    recipeList.addAll(allRecipes)
                    adapter.notifyDataSetChanged()
                    
                    if (allRecipes.isEmpty()) {
                        Toast.makeText(this@ApiActivity, "No se encontraron recetas", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ApiActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("API_ERROR", "Error en la llamada", e)
                }
            }
        }
    }
}
