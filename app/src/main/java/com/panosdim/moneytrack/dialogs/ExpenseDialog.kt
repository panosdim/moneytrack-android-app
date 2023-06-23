package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.databinding.DialogExpenseBinding
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.*
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ExpenseDialog : BottomSheetDialogFragment() {
    private var _binding: DialogExpenseBinding? = null
    private val binding get() = _binding!!
    private var expense: Expense? = null
    private var categoryId: Int? = null
    private val textWatcher = generateTextWatcher(::validateForm)
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val expenseDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE dd-MM-yyyy")
    private val sqlDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var dateSelected: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.expenseAmount.filters = arrayOf<InputFilter>(
            DecimalDigitsInputFilter(
                5,
                2
            )
        )

        binding.expenseComment.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                expense?.let {
                    updateExpense(it)
                } ?: kotlin.run {
                    saveExpense()
                }
            }
            false
        }

        binding.expenseDate.setOnClickListener {
            //Date Picker
            val builder = MaterialDatePicker.Builder.datePicker()
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setOpenAt(dateSelected.toEpochMilli())
            builder.setCalendarConstraints(constraintsBuilder.build())
            builder.setSelection(dateSelected.toEpochMilli())
            builder.setTitleText("Select Date")

            val picker: MaterialDatePicker<Long> = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                dateSelected = fromEpochMilli(selection)
                binding.expenseDate.setText(dateSelected.toShowDateFormat(expenseDateFormatter))
            }

            picker.show(childFragmentManager, picker.toString())
        }

        binding.saveExpense.setOnClickListener {
            binding.prgIndicator.visibility = View.VISIBLE
            binding.saveExpense.isEnabled = false
            binding.deleteExpense.isEnabled = false

            expense?.let {
                updateExpense(it)
            } ?: kotlin.run {
                saveExpense()
            }
        }

        binding.deleteExpense.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.delete_expense_title))
                .setMessage(resources.getString(R.string.delete_expense_supporting_text))
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    dialog.dismiss()
                    deleteExpense()
                }
                .show()
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            list.sortedByDescending { it.count }.forEach { category ->
                val chip = layoutInflater.inflate(
                    R.layout.row_chip_view,
                    requireView().parent.parent as ViewGroup,
                    false
                ) as Chip
                chip.text = category.category
                chip.id = category.id!!
                viewModel.filterCategory?.let {
                    chip.isChecked = it.contains(chip.id)
                }
                binding.expenseCategory.addView(chip)
            }
        }

        binding.expenseCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            categoryId = checkedIds.first()
            validateForm()
        }

        @Suppress("DEPRECATION")
        this.dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        dateSelected = LocalDate.now()
        categoryId = null
    }

    private fun updateExpense(expense: Expense) {
        // Check if we change something in the object
        if (expense.date == dateSelected.format(sqlDateFormatter) &&
            expense.amount == binding.expenseAmount.text.toString().toFloat() &&
            expense.category == categoryId &&
            expense.comment == binding.expenseComment.text.toString()
        ) {
            dismiss()
        } else {
            // Update Expense
            expense.date = dateSelected.format(sqlDateFormatter)
            expense.amount = binding.expenseAmount.text.toString().toFloat()
            expense.category = categoryId!!
            expense.comment = binding.expenseComment.text.toString()

            viewModel.updateExpense(expense).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteExpense.isEnabled = true
                            binding.saveExpense.isEnabled = true
                        }

                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteExpense.isEnabled = true
                            binding.saveExpense.isEnabled = true
                        }

                        is Resource.Loading -> {
                            binding.prgIndicator.visibility = View.VISIBLE
                            binding.deleteExpense.isEnabled = false
                            binding.saveExpense.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun deleteExpense() {
        expense?.let {
            viewModel.removeExpense(it).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteExpense.isEnabled = true
                            binding.saveExpense.isEnabled = true
                        }

                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteExpense.isEnabled = true
                            binding.saveExpense.isEnabled = true
                        }

                        is Resource.Loading -> {
                            binding.prgIndicator.visibility = View.VISIBLE
                            binding.deleteExpense.isEnabled = false
                            binding.saveExpense.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun saveExpense() {
        val newExpense = Expense(
            null,
            dateSelected.format(sqlDateFormatter),
            binding.expenseAmount.text.toString().toFloat(),
            categoryId!!,
            binding.expenseComment.text.toString()
        )

        viewModel.addExpense(newExpense).observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource) {
                    is Resource.Success -> {
                        dismiss()
                        binding.prgIndicator.visibility = View.GONE
                        binding.deleteExpense.isEnabled = true
                        binding.saveExpense.isEnabled = true
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resource.message,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.prgIndicator.visibility = View.GONE
                        binding.deleteExpense.isEnabled = true
                        binding.saveExpense.isEnabled = true
                    }

                    is Resource.Loading -> {
                        binding.prgIndicator.visibility = View.VISIBLE
                        binding.deleteExpense.isEnabled = false
                        binding.saveExpense.isEnabled = false
                    }
                }
            }
        }
    }

    private fun validateForm() {
        val expenseDate = binding.expenseDate
        val expenseAmount = binding.expenseAmount
        val saveExpense = binding.saveExpense
        saveExpense.isEnabled = true
        expenseDate.error = null
        expenseAmount.error = null

        // Store values.
        val date = expenseDate.text.toString()
        val amount = expenseAmount.text.toString()

        // Check for a valid date.
        if (date.isEmpty()) {
            expenseDate.error = getString(R.string.error_field_required)
            saveExpense.isEnabled = false
        }

        // Check for a valid amount.
        if (amount.isEmpty()) {
            expenseAmount.error = getString(R.string.error_field_required)
            saveExpense.isEnabled = false
        }

        // Check if category is selected
        if (categoryId == null) {
            saveExpense.isEnabled = false
        }
    }

    fun showForm(expItem: Expense?) {
        binding.prgIndicator.visibility = View.GONE
        binding.saveExpense.isEnabled = true
        binding.deleteExpense.isEnabled = true

        binding.expenseDate.removeTextChangedListener(textWatcher)
        binding.expenseDate.error = null

        binding.expenseAmount.removeTextChangedListener(textWatcher)
        binding.expenseAmount.error = null

        binding.expenseComment.removeTextChangedListener(textWatcher)
        binding.expenseComment.error = null

        expense = expItem
        if (expItem == null) {
            binding.expenseDate.addTextChangedListener(textWatcher)
            binding.expenseDate.setText(dateSelected.toShowDateFormat(expenseDateFormatter))
            binding.expenseAmount.addTextChangedListener(textWatcher)
            binding.expenseAmount.setText("")
            binding.expenseAmount.requestFocus()
            binding.expenseAmount.post {
                binding.nestedScrollView.scrollTo(0, 0)
            }
            binding.expenseComment.addTextChangedListener(textWatcher)
            binding.expenseComment.setText("")
            binding.deleteExpense.visibility = View.GONE
            binding.saveExpense.setText(R.string.save)
        } else {
            dateSelected = try {
                LocalDate.parse(expItem.date)
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }
            categoryId = expItem.category
            binding.expenseDate.setText(dateSelected.toShowDateFormat(expenseDateFormatter))
            binding.expenseAmount.setText(expItem.amount.toString())
            binding.expenseAmount.clearFocus()
            binding.expenseCategory.check(expItem.category)
            binding.expenseCategory.post {
                val chip =
                    binding.expenseCategory.findViewById<Chip>(expItem.category)
                binding.nestedScrollView.scrollTo(0, chip.top)
            }
            binding.expenseComment.setText(expItem.comment)
            binding.deleteExpense.visibility = View.VISIBLE
            binding.saveExpense.setText(R.string.update)
            binding.expenseDate.addTextChangedListener(textWatcher)
            binding.expenseAmount.addTextChangedListener(textWatcher)
            binding.expenseComment.addTextChangedListener(textWatcher)
        }
    }

    private fun isFormValid(): Boolean {
        return binding.expenseDate.error == null &&
                binding.expenseAmount.error == null &&
                binding.expenseCategory.isSelected
    }

    companion object {
        const val TAG = "ExpenseDialog"
    }
}