package com.diegocupido.recipeapp

import android.graphics.Paint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_generation)
        onClickHintUpdate()
        setupStarButton()
        underlineText()
        displayingFeaturedRecipes()
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

    private fun underlineText()
    {
        val featuredRecipesText: TextView = findViewById(R.id.featured_recipe_text)
        featuredRecipesText.paintFlags = featuredRecipesText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun displayingFeaturedRecipes()
    {
        val recyclerView: RecyclerView = findViewById(R.id.featured_recipes_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val recipes = listOf(
            Pair("Pizza", R.drawable.pizza_slice),
            Pair("Spaghetti", R.drawable.spagetti),
            Pair("Ramen", R.drawable.ramen_bowl),
            Pair("Bacon&Eggs", R.drawable.scrambled_eggs),
            Pair("Steak", R.drawable.steak)
        )

        // Set the adapter
        val adapter = FeaturedRecipesAdapter(recipes)
        recyclerView.adapter = adapter
    }

    private fun navBar()
    {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}