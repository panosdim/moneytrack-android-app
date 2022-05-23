package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.databinding.DialogCategoryBinding
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.utils.generateTextWatcher
import com.panosdim.moneytrack.viewmodel.CategoriesViewModel


class CategoryDialog : BottomSheetDialogFragment() {
    private var _binding: DialogCategoryBinding? = null
    private val binding get() = _binding!!
    private var category: Category? = null
    private val viewModel: CategoriesViewModel by viewModels()
    private val textWatcher = generateTextWatcher(::validateForm)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.categoryName.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                category?.let {
                    updateCategory(it)
                } ?: kotlin.run {
                    saveCategory()
                }
            }
            false
        }

        binding.saveCategory.setOnClickListener {
            category?.let {
                updateCategory(it)
            } ?: kotlin.run {
                saveCategory()
            }
        }

        binding.deleteCategory.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.delete_category_title))
                .setMessage(resources.getString(R.string.delete_category_supporting_text))
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    dialog.dismiss()
                    deleteCategory()
                }
                .show()
        }

        @Suppress("DEPRECATION")
        this.dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return root
    }

    private fun deleteCategory() {
        category?.let {
            viewModel.removeCategory(it).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteCategory.isEnabled = true
                            binding.saveCategory.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteCategory.isEnabled = true
                            binding.saveCategory.isEnabled = true
                        }
                        is Resource.Loading -> {
                            binding.prgIndicator.visibility = View.VISIBLE
                            binding.deleteCategory.isEnabled = false
                            binding.saveCategory.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun saveCategory() {
        val newCategory = Category(
            null,
            binding.categoryName.text.toString(),
            0
        )

        viewModel.addCategory(newCategory).observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource) {
                    is Resource.Success -> {
                        dismiss()
                        binding.prgIndicator.visibility = View.GONE
                        binding.deleteCategory.isEnabled = true
                        binding.saveCategory.isEnabled = true
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resource.message,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.prgIndicator.visibility = View.GONE
                        binding.deleteCategory.isEnabled = true
                        binding.saveCategory.isEnabled = true
                    }
                    is Resource.Loading -> {
                        binding.prgIndicator.visibility = View.VISIBLE
                        binding.deleteCategory.isEnabled = false
                        binding.saveCategory.isEnabled = false
                    }
                }
            }
        }
    }

    private fun updateCategory(category: Category) {
        // Check if we change something in the object
        if (category.category == binding.categoryName.text.toString()) {
            dismiss()
        } else {
            // Update Category
            category.category = binding.categoryName.text.toString()

            viewModel.updateCategory(category).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteCategory.isEnabled = true
                            binding.saveCategory.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteCategory.isEnabled = true
                            binding.saveCategory.isEnabled = true
                        }
                        is Resource.Loading -> {
                            binding.prgIndicator.visibility = View.VISIBLE
                            binding.deleteCategory.isEnabled = false
                            binding.saveCategory.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    fun showForm(categoryItem: Category?) {
        binding.prgIndicator.visibility = View.GONE
        binding.saveCategory.isEnabled = true
        binding.deleteCategory.isEnabled = true

        binding.categoryName.removeTextChangedListener(textWatcher)
        binding.categoryName.error = null

        category = categoryItem
        if (categoryItem == null) {
            binding.categoryName.addTextChangedListener(textWatcher)
            binding.categoryName.setText("")
            binding.deleteCategory.visibility = View.GONE
            binding.saveCategory.setText(R.string.save)
            binding.categoryName.requestFocus()
        } else {
            binding.categoryName.setText(categoryItem.category)
            binding.categoryName.clearFocus()
            binding.deleteCategory.visibility = View.VISIBLE
            binding.saveCategory.setText(R.string.update)
            binding.categoryName.addTextChangedListener(textWatcher)
        }
    }

    private fun validateForm() {
        val categoryName = binding.categoryName
        val saveCategory = binding.saveCategory
        saveCategory.isEnabled = true
        categoryName.error = null

        // Store values.
        val catName = categoryName.text.toString()

        if (catName.isEmpty()) {
            categoryName.error = getString(R.string.error_field_required)
            saveCategory.isEnabled = false
        }

        // Check if existing category has the same name
        category?.let {
            if (catName != category!!.category && viewModel.categories.value?.find {
                    it.category.equals(
                        catName,
                        true
                    )
                } != null) {
                categoryName.error = getString(R.string.error_same_name_conflict)
                saveCategory.isEnabled = false
            }
        } ?: kotlin.run {
            if (viewModel.categories.value?.find {
                    it.category.equals(
                        catName,
                        true
                    )
                } != null) {
                categoryName.error = getString(R.string.error_same_name_conflict)
                saveCategory.isEnabled = false
            }
        }
    }

    private fun isFormValid(): Boolean {
        return binding.categoryName.error == null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CategoryDialog"
    }
}