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
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class ApiActivity : AppCompatActivity() {

//Ayuda de Gemini y de la pagina web de Spoonacular

    private val apiKeyGoogle = BuildConfig.GOOGLE_TRANSLATE_API_KEY
    private val apiKeySpoonacular = BuildConfig.SPOONACULAR_API_KEY
    private val apiKeySpoonacular2 = BuildConfig.SPOONACULAR_API_KEY_2
    private val apiKeySpoonacular3 = BuildConfig.SPOONACULAR_API_KEY_3
    private val apiKey = apiKeySpoonacular3
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

        //Fecha y tipo de comida que vienen desde FoodActivity
            intent.putExtra("selectedDate", getIntent().getStringExtra("selectedDate"))
            intent.putExtra("mealType", getIntent().getStringExtra("mealType"))

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
    private suspend fun translateText(text: String): String = withContext(Dispatchers.IO) {
        try {
            //api de google translate
            val key = apiKeyGoogle

            val url = "https://translation.googleapis.com/language/translate/v2?key=$key"
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

    private fun obtenerTodasLasRecetas() {
        //Coge los ingredientes que vienen de la pantalla anterior, y si no hay, usa una lista vacía
        //Usa esto porquese necesitan traducir
        val includeIngredients = intent.getStringArrayListExtra("includeIngredients") ?: arrayListOf()
        //excluye los ingredientes directamente de BabyUtils
        val excludeIngredients = BabyUtils.getExcludedFoods()
        
        // Categorías que buscamos para dar variedad (purés, muffins, gachas, tortitas)
        val types = listOf(
            "puree",
            "baby food",
            "porridge",
            "soup",
            "stew",
            "mash",
            "finger food",
            "pancakes",
            "muffins"
        )

//Android no permite hacer llamadas a internet en el hilo principal, porque bloquearía la app.
        CoroutineScope(Dispatchers.IO).launch { //forma de ejecutar tareas en segundo plano sin bloquear la app
            try {
                val allRecipes = mutableListOf<Recipe>()
                val seenIds = mutableSetOf<Int>()

                // Traducimos ingredientes seleccionados y los juntamos en una cadena
                val ingredientsQuery = includeIngredients.joinToString(" ") { translate(it) }
                
                // Preparamos los excluidos (evitando excluir lo que el usuario ha seleccionado)
                val translatedSelected = includeIngredients.map { translate(it).lowercase() }
                // Filtramos para excluir lo que el usuario ha seleccionado para la URL
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
                            "&apiKey=$apiKeySpoonacular3"

                    val respuesta = URL(urlString).readText()
                    Log.d("API_RESPUESTA", "Buscando $type: $respuesta")
                    
                    val json = JSONObject(respuesta)
                    val listaJson = json.getJSONArray("results")

                    for (i in 0 until listaJson.length()) {
                        val item = listaJson.getJSONObject(i)
                        val id = item.getInt("id")
                        if (!seenIds.contains(id)) {
                            seenIds.add(id)
                            val originalTitle = item.getString("title")
                            val translatedTitle = translateText(originalTitle)
                            allRecipes.add(Recipe(
                                originalTitle,
                                translatedTitle,
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