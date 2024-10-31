package com.diegocupido.recipeapp.SavedRecipesScreen

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
import com.diegocupido.recipeapp.R
import com.diegocupido.recipeapp.Recipe
import com.diegocupido.recipeapp.RecipeAdapter.RecipeAdapter

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

    // fetches recipe from the generating screen
    private fun loadSavedRecipes() {
        val userId = sharedPreferences.getString("currentUserId", "defaultUserId") // Retrieve the current user's ID
        val savedRecipesKey = "savedRecipes_$userId"
        val savedRecipes = sharedPreferences.getStringSet(savedRecipesKey, emptySet())

        recipeList.clear()

        savedRecipes?.forEach { recipeString ->
            val parts = recipeString.split("::")
            if (parts.size == 2) {
                recipeList.add(Recipe(parts[0], parts[1]))
            }
        }
        recipeAdapter.notifyDataSetChanged()
    }

    // Displays the recipe
    private fun showRecipeDetails(recipe: Recipe) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle(recipe.name)
        dialogBuilder.setMessage(recipe.details)

        dialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    // Delete Recipe
    private fun deleteRecipe(recipe: Recipe) {
        recipeAdapter.removeRecipe(recipe)

        val userId = sharedPreferences.getString("currentUserId", "defaultUserId")
        val savedRecipesKey = "savedRecipes_$userId"

        val savedRecipes = sharedPreferences.getStringSet(savedRecipesKey, mutableSetOf())?.toMutableSet()
        savedRecipes?.remove("${recipe.name}::${recipe.details}")

        sharedPreferences.edit().putStringSet(savedRecipesKey, savedRecipes).apply()
    }
}
