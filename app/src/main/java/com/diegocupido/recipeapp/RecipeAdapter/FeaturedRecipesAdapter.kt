package com.diegocupido.recipeapp.RecipeAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diegocupido.recipeapp.R

class FeaturedRecipesAdapter(
    private val recipes: List<Pair<String, Int>>,
    private val onRecipeClick: (String) -> Unit
) : RecyclerView.Adapter<FeaturedRecipesAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipe_image)
        val recipeName: TextView = view.findViewById(R.id.recipe_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val (name, imageRes) = recipes[position]
        holder.recipeName.text = name
        holder.recipeImage.setImageResource(imageRes)


        holder.itemView.setOnClickListener {
            onRecipeClick(name)
        }
    }

    override fun getItemCount() = recipes.size
}

