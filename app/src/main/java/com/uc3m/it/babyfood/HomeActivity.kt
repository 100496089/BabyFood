package com.uc3m.it.babyfood

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.net.URLEncoder

class HomeActivity : AppCompatActivity() {

    private val apiKeySpoonacular = BuildConfig.SPOONACULAR_API_KEY_4
    private val apiKeyGoogle = BuildConfig.GOOGLE_TRANSLATE_API_KEY

    // Lista para guardar las referencias a las imágenes de sugerencias del XML
    private lateinit var listaSugerencias: List<ShapeableImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_activity)
        BabyUtils.updateAge(this)

        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)

        val nombreBebe = prefs.getString("nombre", "Bebé")
        val welcomeText = findViewById<TextView>(R.id.welcomeMessage)
        welcomeText?.text = "¡Hola, $nombreBebe!"

        val imageViewBebe = findViewById<ShapeableImageView>(R.id.ImagenBebe)
        val photoPath = prefs.getString("foto_perfil", null)
        photoPath?.let {
            val file = File(it)
            if (file.exists()) {
                //imageViewBebe.setImageURI(Uri.fromFile(file))
                Glide.with(this)
                    .load(file)
                    .circleCrop()
                    .into(imageViewBebe)
            }
        }

        //Vincular los IDs del XML
        listaSugerencias = listOf(
            findViewById(R.id.img_receta_1),
            findViewById(R.id.img_receta_2),
            findViewById(R.id.img_receta_3),
            findViewById(R.id.img_receta_4)
        )

        val alergiasUsuario = prefs.getString("alergia", "")?.split(", ") ?: listOf()
        obtenerSugerenciasAPI(alergiasUsuario)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home_button -> true
                R.id.search_button -> {
                    startActivity(Intent(this, FoodActivity::class.java))
                    true
                }
                R.id.favorites_button -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.calendar_button -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                else -> false
            }
        }

        scheduleWeeklyNotification()
        //AYUDA DE LA IA DE ANDROID STUDIO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun obtenerSugerenciasAPI(alergias: List<String>) {
        val excludeByAge = BabyUtils.getExcludedFoods()
        val ageInMonths = BabyUtils.getAgeInMonths()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Filtramos "Ninguna" para tener solo restricciones REALES
                val totalExclude = (excludeByAge + alergias)
                    .filter { it != "Ninguna" && it.isNotBlank() }
                    .distinct()

                val query = if (ageInMonths >= 12) "healthy" else "baby"

                // Construimos la URL base (query=baby para sugerencias infantiles)
                //var urlString = "https://api.spoonacular.com/recipes/complexSearch?query=baby&number=4&apiKey=$apiKeySpoonacular"
                var urlString = "https://api.spoonacular.com/recipes/complexSearch?query=$query&number=4&apiKey=$apiKeySpoonacular"

                // SOLO añadimos excludeIngredients si realmente hay algo que prohibir
                if (totalExclude.isNotEmpty()) {
                    val excludeString = totalExclude.joinToString(", ")
                    val translatedExcludes = withContext(Dispatchers.IO) { translateText(excludeString) }
                    val finalExcludeParam = translatedExcludes.replace(", ", ",").lowercase()
                    urlString += "&excludeIngredients=${URLEncoder.encode(finalExcludeParam, "UTF-8")}"
                }

                // Log para que verificar en Logcat la búsqueda exacta
                Log.d("HOME_API_URL", "Buscando: $urlString")

                val respuesta = URL(urlString).readText()
                val results = JSONObject(respuesta).getJSONArray("results")

                withContext(Dispatchers.Main) {
                    if (results.length() > 0) {
                        for (i in 0 until results.length()) {
                            if (i < listaSugerencias.size) {
                                val recipe = results.getJSONObject(i)
                                listaSugerencias[i].visibility = View.VISIBLE
                                Glide.with(this@HomeActivity).load(recipe.getString("image")).centerCrop().into(listaSugerencias[i])

                                listaSugerencias[i].setOnClickListener {
                                    startActivity(Intent(this@HomeActivity, RecipeDetailActivity::class.java).apply {
                                        putExtra("recipeId", recipe.getInt("id"))
                                    })
                                }
                            }
                        }
                        // Ocultar huecos si hay menos resultados de los esperados
                        for (j in results.length() until listaSugerencias.size) {
                            listaSugerencias[j].visibility = View.GONE
                        }
                    } else {
                        Log.d("HOME_API_DEBUG", "0 resultados encontrados para $query con exclusiones.")
                        mostrarRecetasFijas()
                            //for (view in listaSugerencias) view.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("API_HOME_ERROR", "Error en la red: ${e.message}")
                withContext(Dispatchers.Main) {
                    mostrarRecetasFijas()
                }
            }
        }
    }

    private suspend fun translateText(text: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKeyGoogle"
            val body = JSONObject().apply {
                put("q", text)
                put("source", "es")
                put("target", "en")
                put("format", "text")
            }
            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            json.getJSONObject("data")
                .getJSONArray("translations")
                .getJSONObject(0)
                .getString("translatedText")
        } catch (e: Exception) {
            text
        }
    }

    // Muestra 4 recetas fijas si la API falla o no hay resultados
    private fun mostrarRecetasFijas() {
        val recetasFijas = listOf(
            Pair(658101, "https://img.spoonacular.com/recipes/658101-312x231.jpg"),
            Pair(661170, "https://img.spoonacular.com/recipes/661170-312x231.jpg"),
            Pair(1096247, "https://spoonacular.com/recipeImages/1096247-312x231.jpg"),
            Pair(655698, "https://spoonacular.com/recipeImages/655698-312x231.jpg")
        )

        for (i in 0 until listaSugerencias.size) {
            val (id, imageUrl) = recetasFijas[i]
            listaSugerencias[i].visibility = View.VISIBLE
            Glide.with(this).load(imageUrl).centerCrop().into(listaSugerencias[i])

            listaSugerencias[i].setOnClickListener {
                startActivity(Intent(this, RecipeDetailActivity::class.java).apply {
                    putExtra("recipeId", id)
                })
            }
        }
    }

    fun addFood(view: View?){
        startActivity(Intent(this, AddFoodActivity::class.java))
    }
    fun profileChange(view: View?){
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.putExtra("isEditing", true)
        startActivity(intent)
    }
    fun foodRegister(view: View?){
        startActivity(Intent(this, FoodRegisterActivity::class.java))
    }
    fun openWeightChart(view: View?) {
        startActivity(Intent(this, WeightChartActivity::class.java))
    }
    fun openRecipes(view: View?) {
        startActivity(Intent(this, FoodActivity::class.java))
    }

    //notificacion
    private fun scheduleWeeklyNotification() {
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Configuramos para que empiece, por ejemplo, ahora mismo
        val startTime = System.currentTimeMillis()

        // Intervalo de una semana en milisegundos
        //val intervalWeek = AlarmManager.INTERVAL_DAY * 7
        val intervalWeek = 60000L

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime + 10000, // Empieza en 10 segundos para probar
            intervalWeek,
            pendingIntent
        )
    }
}