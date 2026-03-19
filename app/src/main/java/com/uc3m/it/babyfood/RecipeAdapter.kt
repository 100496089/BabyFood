package com.uc3m.it.babyfood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.util.Log

class RecipeAdapter(private val recipeList: List<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

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

        val receta = recipeList[position]
        Log.d("RECETA_IMG", receta.image)
        holder.titulo.text = receta.title

        Glide.with(holder.img.context)
            .load(receta.image)
            .into(holder.img)
    }

    override fun getItemCount(): Int = recipeList.size
}