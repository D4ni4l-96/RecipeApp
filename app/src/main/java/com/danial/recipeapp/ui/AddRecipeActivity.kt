package com.danial.recipeapp.ui

import android.app.Activity
import android.content.Context
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
import com.danial.recipeapp.databinding.ActivityAddRecipeBinding
import com.danial.recipeapp.util.loadRecipeTypesFromJson
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private var selectedImageUri: Uri? = null
    private lateinit var db: AppDatabase

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val originalUri = result.data?.data
                val copiedUri = originalUri?.let { copyUriToInternalStorage(this, it) }

                if (copiedUri != null) {
                    selectedImageUri = copiedUri
                    binding.recipeImageView.setImageURI(copiedUri)
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        setupDropdown()
        setupImageSelector()
        setupSaveButton()
    }

    private fun setupDropdown() {
        val types = loadRecipeTypesFromJson(this)
        val dropdown = binding.recipeTypePicker as AutoCompleteTextView
        dropdown.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types))
    }

    private fun setupImageSelector() {
        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePicker.launch(intent)
        }
    }

    private fun setupSaveButton() {
        binding.saveRecipeButton.setOnClickListener {
            val title = binding.recipeTitleInput.text.toString()
            val type = binding.recipeTypePicker.text.toString()
            val ingredients = binding.recipeIngredientsInput.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val steps = binding.recipeStepsInput.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val imageUri = selectedImageUri?.toString()

            if (title.isBlank() || type.isBlank() || ingredients.isEmpty() || steps.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recipe = Recipe(
                title = title,
                type = type,
                imageUri = imageUri,
                ingredients = ingredients,
                steps = steps
            )

            lifecycleScope.launch {
                db.recipeDao().insert(recipe)
                finish()
            }
        }
    }


    private fun copyUriToInternalStorage(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")
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
