package com.panosdim.moneytrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.databinding.RowCategoryBinding
import com.panosdim.moneytrack.model.Category

class CategoriesAdapter(
    private val categoriesList: List<Category>,
    private val clickListener: (Category) -> Unit
) :
    RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RowCategoryBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        with(holder) {
            with(categoriesList[position]) {
                binding.tvCategory.text = category
                holder.itemView.setOnClickListener { clickListener(this) }
            }
        }
    }

    override fun getItemCount() = categoriesList.size

    inner class CategoryViewHolder(val binding: RowCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)
}