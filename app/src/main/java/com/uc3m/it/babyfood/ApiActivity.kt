package com.uc3m.it.babyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class ApiActivity : AppCompatActivity() {

    //private val apiKey = "0b42d0c40af044c8a21ee108e502dd6b"
    private val apiKey ="6f63320e184e43b6b4f1c6ffbb74528c"
    private lateinit var adapter: RecipeAdapter //declaro el adapter pero se inicializará mas tarde
    private val recipeList = mutableListOf<Recipe>() // Lista de recetas que llegan de la api
    //se trata de una lista que se puede modificar


    override fun onCreate(savedInstanceState: Bundle?) {//carga layout y componentes
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerRecetas)

        adapter = RecipeAdapter(recipeList){ receta ->

            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", receta.id)
            startActivity(intent)

        }
        recyclerView.layoutManager = LinearLayoutManager(this)//linear layout para que las recetas se muestren en una columna
        recyclerView.adapter = adapter

        obtenerTodasLasRecetas()
    }

    private fun obtenerTodasLasRecetas() {
        val queries = listOf(
            "puree",
            "baby porridge",
            "mashed vegetables",
            "fruit puree",
            "soft food",
            "baby food",
            "baby",
            "baby pancakes",
            "baby cakes",
            "baby muffins",
            "baby snacks",
            "baby biscuits",
            "baby finger food"

        )
//Android no permite hacer llamadas a internet en el hilo principal, porque bloquearía la app.
        CoroutineScope(Dispatchers.IO).launch { //forma de ejecutar tareas en segundo plano sin bloquear la app
            try {
                val allRecipes = mutableListOf<Recipe>()

                for (q in queries) {//bucle para cada palabra
                    //busqueda de palabra, devuelve 10 resultados de la api
                    val url = "https://api.spoonacular.com/recipes/complexSearch?query=$q&number=1&apiKey=$apiKey"
                    val respuesta = URL(url).readText()//leemos la respuesta de la api- descarga json-lee texto
                    Log.d("API_RESPUESTA", respuesta)
                    val json = JSONObject(respuesta)//texto a json
                    val listaJson = json.getJSONArray("results")

                    for (i in 0 until listaJson.length()) {
                        val item = listaJson.getJSONObject(i)
                        val title = item.getString("title")
                        val image = item.getString("image")
                        val id = item.getInt("id")
                        allRecipes.add(Recipe(title, image, id))
                    }
                }

                withContext(Dispatchers.Main) {//actualizar la lista de recetas en el hilo principal
                    recipeList.clear()
                    recipeList.addAll(allRecipes)
                    adapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ApiActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}