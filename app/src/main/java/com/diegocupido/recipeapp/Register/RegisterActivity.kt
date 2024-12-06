package com.diegocupido.recipeapp.Register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.diegocupido.recipeapp.Database.DatabaseHelper
import com.diegocupido.recipeapp.Login.LoginActivity
import com.diegocupido.recipeapp.R


class RegisterActivity : AppCompatActivity() {

    lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val registerButton = findViewById<Button>(R.id.buttonRegisterSubmit)
        val phoneEmailEditText = findViewById<EditText>(R.id.editTextPhoneEmailRegister)
        val passwordEditText = findViewById<EditText>(R.id.editTextPasswordRegister)

        registerButton.setOnClickListener {
            val phoneEmail = phoneEmailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (phoneEmail.isNotEmpty() && password.isNotEmpty()) {
                val result = dbHelper.addUser(phoneEmail, password)
                if (result != -1L) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}