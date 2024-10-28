package com.diegocupido.recipeapp

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SavedRecipeFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val recipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_recipe, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = requireActivity().getSharedPreferences("RecipePrefs", android.content.Context.MODE_PRIVATE)
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = RecipeAdapter(recipeList, { recipe -> showRecipeDetails(recipe) }, { recipe -> deleteRecipe(recipe) })
        recipeRecyclerView.adapter = recipeAdapter


        loadSavedRecipes()

        return view
    }

    private fun loadSavedRecipes() {
        val savedRecipes = sharedPreferences.getStringSet("savedRecipes", emptySet())
        recipeList.clear()

        savedRecipes?.forEach { recipeString ->
            val parts = recipeString.split("::")
            if (parts.size == 2) {
                recipeList.add(Recipe(parts[0], parts[1]))
            }
        }
        recipeAdapter.notifyDataSetChanged()
    }

    private fun showRecipeDetails(recipe: Recipe) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle(recipe.name)
        dialogBuilder.setMessage(recipe.details)

        dialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteRecipe(recipe: Recipe) {
        // Remove the recipe from the list
        recipeAdapter.removeRecipe(recipe)

        // Update SharedPreferences to remove the recipe
        val savedRecipes = sharedPreferences.getStringSet("savedRecipes", mutableSetOf())?.toMutableSet()
        savedRecipes?.remove("${recipe.name}::${recipe.details}")
        sharedPreferences.edit().putStringSet("savedRecipes", savedRecipes).apply()
    }
}
