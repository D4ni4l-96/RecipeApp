package com.danial.recipeapp.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danial.recipeapp.R
import com.danial.recipeapp.data.Recipe
import com.danial.recipeapp.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onEditClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(DiffCallback()) {

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipeTitle.text = recipe.title
            binding.recipeType.text = recipe.type

            val context = binding.root.context
            val uri = Uri.parse(recipe.imageUri)

            try {
                val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeDrawable(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        .let { android.graphics.drawable.BitmapDrawable(context.resources, it) }
                }
                binding.recipeImage.setImageDrawable(drawable)
            } catch (e: Exception) {
                Log.e("RecipeAdapter", "Image load failed", e)
                binding.recipeImage.setImageResource(R.drawable.placeholder_image)
            }

            binding.root.setOnClickListener {
                onItemClick(recipe)
            }

            binding.editButton.setOnClickListener {
                onEditClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }

}
