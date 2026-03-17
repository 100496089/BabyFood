package com.uc3m.it.babyfood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView

class FoodAdapter(
    private var items: List<Food>//por que con val no pero con private var si
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textFoodName)
        val image: ImageView = itemView.findViewById(R.id.imageFood)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = items[position]

        holder.textName.text = food.name
        holder.image.setImageResource(food.imageRes)

        // Cambiar fondo según selección
        if (food.isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.item_food_selected)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_food_normal)
        }

        holder.itemView.setOnClickListener {
            food.isSelected = !food.isSelected
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Food>) {
        items = newList
        notifyDataSetChanged()
    }
}

