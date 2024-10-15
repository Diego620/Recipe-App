package com.diegocupido.recipeapp

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_generation)
        onClickHintUpdate()
        setupStarButton()
        underlineText()
        displayingFeaturedRecipes()
        bottomNav()
        setUpUi()
    }

    private fun onClickHintUpdate() {
        val editText = findViewById<EditText>(R.id.input_recipe)

        // Remove the hint when the EditText is focused
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // Clear the hint when focused
                editText.hint = ""
            } else {
                // Restore the hint when not focused
                editText.hint = "Enter The Name Of The Dish"
            }
        }
    }

    private fun setupStarButton() {
        var isStarred: Boolean = false
        val starButton: ImageButton = findViewById(R.id.star_button)


        if (isStarred) {
            starButton.setImageResource(R.drawable.ic_star_filled)
        } else {
            starButton.setImageResource(R.drawable.ic_star)
        }

        starButton.setOnClickListener {
            isStarred = !isStarred

            if (isStarred) {
                starButton.setImageResource(R.drawable.ic_star_filled)
            } else {
                starButton.setImageResource(R.drawable.ic_star)
            }
        }
    }

    private fun underlineText() {
        val featuredRecipesText: TextView = findViewById(R.id.featured_recipe_text)
        featuredRecipesText.paintFlags = featuredRecipesText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun displayingFeaturedRecipes() {
        val recyclerView: RecyclerView = findViewById(R.id.featured_recipes_recycler_view)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val recipes = listOf(
            Pair("Pizza", R.drawable.pizza_slice),
            Pair("Spaghetti", R.drawable.spagetti),
            Pair("Ramen", R.drawable.ramen_bowl),
            Pair("Bacon&Eggs", R.drawable.scrambled_eggs),
            Pair("Steak", R.drawable.steak)
        )

        val adapter = FeaturedRecipesAdapter(recipes)
        recyclerView.adapter = adapter
    }

    private fun bottomNav() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_generate

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_generate -> {
                    true
                }

                R.id.nav_saved -> {
                    val intent = Intent(this, SavedRecipeActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    private fun setUpUi() {
        val inputRecipe: EditText = findViewById(R.id.input_recipe)
        val recipeCardView: CardView = findViewById(R.id.recipeCardView)

        // Ensure the CardView is not visible at startup
        recipeCardView.visibility = View.GONE

        inputRecipe.setOnClickListener {
            showInputDialog()
        }
    }
    private fun showInputDialog() {
        val generatedRecipeText: TextView = findViewById(R.id.generatedRecipeText)
        val recipeCardView: CardView = findViewById(R.id.recipeCardView)

        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val dialogInputRecipe: EditText = dialogView.findViewById(R.id.dialog_input_recipe)

        MaterialAlertDialogBuilder(this)
            .setTitle("Enter Recipe Name")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val userInput = dialogInputRecipe.text.toString()

                // Check for valid input
                if (userInput.isNotEmpty()) {
                    // Set the generated recipe text
                    generatedRecipeText.text = "Generated Recipe for: $userInput"
                    // Make the CardView visible
                    recipeCardView.visibility = View.VISIBLE
                } else {
                    // Handle empty input
                    generatedRecipeText.text = "Please enter a dish name."
                    // Ensure the CardView is not visible if input is empty
                    recipeCardView.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}