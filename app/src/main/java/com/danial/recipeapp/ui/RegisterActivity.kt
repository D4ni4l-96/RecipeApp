package com.danial.recipeapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danial.recipeapp.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginRedirectText: TextView

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.buttonRegister)
        loginRedirectText = findViewById(R.id.textLoginRedirect)

        sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to SharedPreferences (for demo purposes; not secure)
            val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putString("registered_username", username)
                .putString("registered_password", password)
                .apply()

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

            // Redirect to Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
