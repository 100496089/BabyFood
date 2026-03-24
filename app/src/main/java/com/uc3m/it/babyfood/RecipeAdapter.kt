package com.uc3m.it.babyfood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.util.Log
//GEMINI
class RecipeAdapter(private val recipeList: List<Recipe>, private val onClick: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {
//una función que se ejecuta cuando el usuario toca una receta
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgReceta)
        val titulo: TextView = v.findViewById(R.id.txtTitulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val receta = recipeList[position] //obtiene la receta correspondiente
        Log.d("RECETA_IMG", receta.image)
        holder.titulo.text = receta.title //muestra el titulo

        holder.itemView.setOnClickListener {
            onClick(receta)
        }

        Glide.with(holder.img.context) //carga la imagen con glide, entonces nueva versión
            .load(receta.image)
            .into(holder.img)
    }

    override fun getItemCount(): Int = recipeList.size
}