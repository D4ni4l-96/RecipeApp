package com.danial.recipeapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danial.recipeapp.data.AppDatabase
import com.danial.recipeapp.data.Recipe
import com.danial.recipeapp.databinding.ActivityRecipeDetailBinding
import com.danial.recipeapp.util.loadRecipeTypesFromJson
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var db: AppDatabase
    private var currentRecipe: Recipe? = null
    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val pickedUri = result.data?.data
                if (pickedUri != null) {
                    val localUri = copyUriToInternalStorage(pickedUri)
                    if (localUri != null) {
                        selectedImageUri = localUri
                        binding.detailImage.setImageURI(localUri)
                    } else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        setupDropdown()
        setupImageSelector()
        loadRecipe()
        setupUpdateButton()
        setupDeleteButton()
    }

    private fun setupDropdown() {
        val types = loadRecipeTypesFromJson(this)
        val dropdown = binding.detailType as AutoCompleteTextView
        dropdown.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                types
            )
        )
    }

    private fun setupImageSelector() {
        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePicker.launch(intent)
        }
    }

    private fun loadRecipe() {
        val recipeId = intent.getIntExtra("recipe_id", -1)
        if (recipeId == -1) {
            Toast.makeText(this, "Invalid recipe", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            currentRecipe = db.recipeDao().getById(recipeId)
            currentRecipe?.let { recipe ->
                binding.detailTitle.setText(recipe.title)
                binding.detailType.setText(recipe.type, false)
                binding.detailIngredients.setText(recipe.ingredients.joinToString("\n"))
                binding.detailSteps.setText(recipe.steps.joinToString("\n"))
                selectedImageUri = Uri.parse(recipe.imageUri)
                binding.detailImage.setImageURI(selectedImageUri)
            }
        }
    }

    private fun setupUpdateButton() {
        binding.editButton.setOnClickListener {
            val recipe = currentRecipe ?: return@setOnClickListener

            val title = binding.detailTitle.text.toString()
            val type = binding.detailType.text.toString()
            val ingredients = binding.detailIngredients.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val steps = binding.detailSteps.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val imageUri = selectedImageUri?.toString() ?: recipe.imageUri

            if (title.isBlank() || type.isBlank() || ingredients.isEmpty() || steps.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedRecipe = recipe.copy(
                title = title,
                type = type,
                ingredients = ingredients,
                steps = steps,
                imageUri = imageUri
            )

            lifecycleScope.launch {
                db.recipeDao().update(updatedRecipe)
                finish()
            }
        }
    }

    private fun setupDeleteButton() {
        binding.deleteButton.setOnClickListener {
            // Show confirmation dialog
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Yes") { _, _ ->
                    currentRecipe?.let {
                        lifecycleScope.launch {
                            db.recipeDao().delete(it)
                            finish()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .create()

            dialog.show()
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
