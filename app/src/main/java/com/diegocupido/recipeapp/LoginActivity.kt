package com.diegocupido.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

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
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    // make it go to the final screen
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
        }
    }
}