package com.danial.recipeapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danial.recipeapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if already logged in
        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("is_logged_in", false)) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val savedUsername = sharedPrefs.getString("registered_username", null)
        val savedPassword = sharedPrefs.getString("registered_password", null)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()



            if (username == savedUsername && password == savedPassword) {
                sharedPrefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("username", username)
                    .apply()
                goToMain()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerRedirectText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))

        }
    }
}
