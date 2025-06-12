package com.danial.recipeapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.danial.recipeapp.data.AppDatabase
import com.danial.recipeapp.databinding.ActivityMainBinding
import com.danial.recipeapp.util.loadRecipeTypesFromJson
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.danial.recipeapp.R
import com.danial.recipeapp.repository.RecipeRepository
import com.danial.recipeapp.ui.viewmodel.RecipeViewModel
import com.danial.recipeapp.ui.viewmodel.RecipeViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeTypes: List<String>
    private val viewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(RecipeRepository(AppDatabase.getInstance(this).recipeDao()))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.topAppBar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            WindowInsetsCompat.CONSUMED
        }

        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        setupRecyclerView()
        setupDropdown()
        setupFab()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        val sharedPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("username")
            .apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onItemClick = { recipe ->
                val intent = Intent(this, RecipeViewActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                startActivity(intent)
            },
            onEditClick = { recipe ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                startActivity(intent)
            }
        )

        binding.recipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recipeAdapter
        }
    }

    private fun setupDropdown() {
        recipeTypes = loadRecipeTypesFromJson(this).toMutableList().apply {
            add(0, "All") // First item to show all recipes
        }

        val dropdown = binding.recipeTypeDropdown as AutoCompleteTextView
        dropdown.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, recipeTypes))

        dropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedType = recipeTypes[position]
            if (selectedType == "All") observeRecipes()
            viewModel.setFilter(if (selectedType == "All") null else selectedType)
        }

        // Load all by default
        observeRecipes()

    }

    private fun setupFab() {
        binding.addRecipeFab.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeRecipes() {
        lifecycleScope.launch {
            viewModel.filteredRecipes.collect { list ->
                recipeAdapter.submitList(list)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeRecipes()

    }
}
