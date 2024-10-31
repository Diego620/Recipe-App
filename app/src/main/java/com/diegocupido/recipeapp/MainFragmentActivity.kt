package com.diegocupido.recipeapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.diegocupido.recipeapp.RecipeScreen.RecipeFragment
import com.diegocupido.recipeapp.SavedRecipesScreen.SavedRecipeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragmentActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        sharedPreferences = getSharedPreferences("RecipePrefs", MODE_PRIVATE)

        if (savedInstanceState == null) {
            loadFragment(RecipeFragment())
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_generate -> {
                    loadFragment(RecipeFragment())
                    true
                }
                R.id.nav_saved -> {
                    loadFragment(SavedRecipeFragment())
                    true
                }
                else -> false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            val editor = sharedPreferences.edit()
            editor.remove("savedInput")
            editor.remove("savedResult")
            editor.apply()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
