package com.danial.recipeapp.ui

import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danial.recipeapp.R
import com.danial.recipeapp.data.AppDatabase
import com.danial.recipeapp.databinding.ActivityRecipeViewBinding
import kotlinx.coroutines.launch

class RecipeViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeViewBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        val recipeId = intent.getIntExtra("recipe_id", -1)
        if (recipeId != -1) {
            loadRecipe(recipeId)
        } else {
            finish()
        }
    }

    private fun loadRecipe(id: Int) {
        lifecycleScope.launch {
            val recipe = db.recipeDao().getById(id)
            recipe?.let {
                binding.viewRecipeTitle.text = it.title
                binding.viewRecipeType.text = "Type: ${it.type}"

                val uri = Uri.parse(it.imageUri)
                try {
                    val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        ImageDecoder.decodeDrawable(source)
                    } else {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        BitmapDrawable(resources, bitmap)
                    }
                    binding.viewRecipeImage.setImageDrawable(drawable)
                } catch (e: Exception) {
                    binding.viewRecipeImage.setImageResource(R.drawable.placeholder_image)
                }

                binding.viewRecipeIngredients.text =
                    it.ingredients.joinToString("\n") { ingredient -> "• $ingredient" }

                binding.viewRecipeSteps.text =
                    it.steps.mapIndexed { index, step -> "${index + 1}. $step" }
                        .joinToString("\n")
            }
        }
    }
}
