package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.panosdim.moneytrack.adapters.CategoriesAdapter
import com.panosdim.moneytrack.databinding.FragmentCategoriesBinding
import com.panosdim.moneytrack.dialogs.CategoryDialog
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.utils.ExtendedFloatingActionButtonScrollListener
import com.panosdim.moneytrack.viewmodel.CategoriesViewModel

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private val categoriesViewAdapter =
        CategoriesAdapter(mutableListOf()) { categoryItem: Category ->
            categoryItemClicked(
                categoryItem
            )
        }
    private val categoryDialog: CategoryDialog = CategoryDialog()
    private val viewModel: CategoriesViewModel by viewModels()
    
    private fun categoryItemClicked(categoryItem: Category) {
        categoryDialog.showNow(childFragmentManager, CategoryDialog.TAG)
        categoryDialog.showForm(categoryItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            val data = list.toMutableList()
            data.sortByDescending { it.count }
            binding.rvCategories.adapter =
                CategoriesAdapter(data) { categoryItem: Category -> categoryItemClicked(categoryItem) }
        }

        binding.catSwipeRefresh.setOnRefreshListener {
            viewModel.refreshCategories()
            binding.catSwipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val rvCategories = binding.rvCategories
        rvCategories.setHasFixedSize(true)
        rvCategories.layoutManager = LinearLayoutManager(root.context)
        rvCategories.adapter = categoriesViewAdapter
        rvCategories.addOnScrollListener(ExtendedFloatingActionButtonScrollListener(binding.addNewCategory))

        binding.addNewCategory.setOnClickListener {
            categoryDialog.showNow(childFragmentManager, CategoryDialog.TAG)
            categoryDialog.showForm(null)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}