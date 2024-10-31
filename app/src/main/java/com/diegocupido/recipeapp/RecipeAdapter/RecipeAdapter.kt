package com.diegocupido.recipeapp.RecipeAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diegocupido.recipeapp.R
import com.diegocupido.recipeapp.Recipe

class RecipeAdapter(
    private val recipes: MutableList<Recipe>, // Make the list mutable to allow deletion
    private val itemClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit // Add a delete click listener
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View, val itemClick: (Recipe) -> Unit, val onDeleteClick: (Recipe) -> Unit) : RecyclerView.ViewHolder(view) {
        private val recipeNameTextView: TextView = view.findViewById(R.id.recipe_name)
        private val binButton: ImageButton = view.findViewById(R.id.bin_button)

        fun bind(recipe: Recipe) {
            recipeNameTextView.text = recipe.name
            itemView.setOnClickListener { itemClick(recipe) }
            binButton.setOnClickListener { onDeleteClick(recipe) } // Set the delete click listener
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view, itemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    // Method to remove a recipe
    fun removeRecipe(recipe: Recipe) {
        val position = recipes.indexOf(recipe)
        if (position != -1) {
            recipes.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

