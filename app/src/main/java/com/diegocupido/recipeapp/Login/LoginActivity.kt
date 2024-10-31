package com.diegocupido.recipeapp.Login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.diegocupido.recipeapp.Database.DatabaseHelper
import com.diegocupido.recipeapp.MainFragmentActivity
import com.diegocupido.recipeapp.R
import com.diegocupido.recipeapp.Register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize the DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RecipePrefs", MODE_PRIVATE)

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        val phoneEmailEditText = findViewById<EditText>(R.id.editTextPhoneEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)

        loginButton.setOnClickListener {
            val phoneEmail = phoneEmailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (phoneEmail.isNotEmpty() && password.isNotEmpty()) {
                val isValid = dbHelper.checkUser(phoneEmail, password)
                if (isValid) {
                    // Store the current user ID (or email) in SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putString("currentUserId", phoneEmail) // Store user ID (email or unique ID)
                    editor.apply()

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainFragmentActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
