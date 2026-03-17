package com.uc3m.it.babyfood

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private val apiKey = "6f63320e184e43b6b4f1c6ffbb74528c"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val recipeId = intent.getIntExtra("recipeId", -1)

        if (recipeId != -1) {
            cargarReceta(recipeId)
        }
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
