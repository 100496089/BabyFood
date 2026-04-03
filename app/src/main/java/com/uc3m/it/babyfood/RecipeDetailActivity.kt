package com.uc3m.it.babyfood

import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.ImageView
import com.bumptech.glide.Glide
//GEMINI
class RecipeDetailActivity : AppCompatActivity() {

    private val apiKey = BuildConfig.SPOONACULAR_API_KEY_3
    private val apiKeyGoogle = BuildConfig.GOOGLE_TRANSLATE_API_KEY

    private lateinit var db: DatabaseAdapter
    private var isFav = false
    private var recipeTitle = ""
    private var recipeImage = ""
    private var recipeId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        db = DatabaseAdapter(this).open()

        val btnLikeLayout = findViewById<LinearLayout>(R.id.btnLikeLayout)
        val imgLike = findViewById<android.widget.ImageView>(R.id.imgLike)

        // Botón volver a ApiActivity
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
           // val intent = Intent(this, ApiActivity::class.java)
            //startActivity(intent)
            //finish()
            onBackPressedDispatcher.onBackPressed() //cierra la actividad actual y vuelve a la anterior (ApiActivity)
        }

        val recipeIdIntent  = intent.getIntExtra("recipeId", -1)  // Recoge el ID que se envió desde la pantalla anterior

        if (recipeIdIntent != -1) {
            recipeId = recipeIdIntent
            cargarReceta(recipeId) //Llama a la función que descarga la receta
            
            // Estado inicial
            isFav = db.isFavorite(recipeId)
            actualizarIconoFavorito(imgLike)
        }

        btnLikeLayout.setOnClickListener {
            if (recipeId == -1 || recipeTitle.isEmpty()) return@setOnClickListener
            
            isFav = !isFav

            if (isFav) {
                db.addFavorite(recipeId, recipeTitle, recipeImage)
            } else {
                db.removeFavorite(recipeId)
            }
            actualizarIconoFavorito(imgLike)
        }

        setupBottomNavigation()

    }

    private fun actualizarIconoFavorito(img: android.widget.ImageView) {
        if (isFav) {
            img.setColorFilter(android.graphics.Color.RED)
        } else {
            img.setColorFilter(android.graphics.Color.BLACK)
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
//para que no salga en formato html
    private fun cleanHtml(text: String): String {
        return android.text.Html.fromHtml(
            text,
            android.text.Html.FROM_HTML_MODE_LEGACY
        ).toString()
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
    private fun cargarReceta(id: Int) {

        CoroutineScope(Dispatchers.IO).launch { //lanza una corrutina en segundo plano y así no bloquea la interfaz
//Hace una petición GET a Spoonacular y obtiene un JSON
            val url = "https://api.spoonacular.com/recipes/$id/information?apiKey=$apiKey"
            val respuesta = URL(url).readText()

            val json = JSONObject(respuesta)
//Extrae título y resumen
            val title = json.getString("title")
            val translatedTitle = translateText(title)
            val summary = json.getString("summary")
            val translatedSummary = translateText(cleanHtml(summary))
            val image = json.optString("image", "")

            //obtener la lista de ingredientes
            val ingredientsArray = json.getJSONArray("extendedIngredients")

            val ingredientesList = mutableListOf<String>()

            for (i in 0 until ingredientsArray.length()) {
                val ingrediente = ingredientsArray.getJSONObject(i)
                ingredientesList.add(ingrediente.getString("original"))
            }
            val ingredientesTraducidosList = mutableListOf<String>()

            for (ingrediente in ingredientesList) {
                val traducido = translateText(ingrediente)
                ingredientesTraducidosList.add("• $traducido")
            }

            val ingredientesTraducidos = ingredientesTraducidosList.joinToString("\n")
            withContext(Dispatchers.Main) {
                recipeTitle = title
                recipeImage = image
                findViewById<TextView>(R.id.txtTitle).text = translatedTitle
                findViewById<TextView>(R.id.txtIngredients).text = ingredientesTraducidos
                findViewById<TextView>(R.id.txtSummary).text = translatedSummary

                val imgRecipe = findViewById<ImageView>(R.id.imgRecipe)
                Glide.with(this@RecipeDetailActivity)
                    .load(image)
                    .into(imgRecipe)

            }

        }

    }

}