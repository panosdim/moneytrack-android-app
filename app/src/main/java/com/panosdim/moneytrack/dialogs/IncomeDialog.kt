package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.databinding.DialogIncomeBinding
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.*
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class IncomeDialog : BottomSheetDialogFragment() {
    private var _binding: DialogIncomeBinding? = null
    private val binding get() = _binding!!
    private var income: Income? = null
    private val textWatcher = generateTextWatcher(::validateForm)
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val incomeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val sqlDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var dateSelected: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIncomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.incomeAmount.filters = arrayOf<InputFilter>(
            DecimalDigitsInputFilter(
                5,
                2
            )
        )

        binding.incomeComment.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                income?.let {
                    updateIncome(it)
                } ?: kotlin.run {
                    saveIncome()
                }
            }
            false
        }

        binding.incomeDate.setOnClickListener {
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
                binding.incomeDate.setText(dateSelected.toShowDateFormat(incomeDateFormatter))
            }

            picker.show(childFragmentManager, picker.toString())
        }

        binding.saveIncome.setOnClickListener {
            binding.prgIndicator.visibility = View.VISIBLE
            binding.saveIncome.isEnabled = false
            binding.deleteIncome.isEnabled = false

            income?.let {
                updateIncome(it)
            } ?: kotlin.run {
                saveIncome()
            }
        }

        binding.deleteIncome.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.delete_income_title))
                .setMessage(resources.getString(R.string.delete_income_supporting_text))
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    dialog.dismiss()
                    deleteIncome()
                }
                .show()
        }

        @Suppress("DEPRECATION")
        this.dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return root
    }

    override fun onPause() {
        super.onPause()
        dateSelected = LocalDate.now()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateIncome(income: Income) {
        // Check if we change something in the object
        if (income.date == dateSelected.format(sqlDateFormatter) &&
            income.amount == binding.incomeAmount.text.toString().toFloat() &&
            income.comment == binding.incomeComment.text.toString()
        ) {
            dismiss()
        } else {
            // Update Income
            income.date = dateSelected.format(sqlDateFormatter)
            income.amount = binding.incomeAmount.text.toString().toFloat()
            income.comment = binding.incomeComment.text.toString()

            viewModel.updateIncome(income).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteIncome.isEnabled = true
                            binding.saveIncome.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteIncome.isEnabled = true
                            binding.saveIncome.isEnabled = true
                        }
                        is Resource.Loading -> {
                            binding.prgIndicator.visibility = View.VISIBLE
                            binding.deleteIncome.isEnabled = false
                            binding.saveIncome.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun deleteIncome() {
        income?.let {
            viewModel.removeIncome(it).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteIncome.isEnabled = true
                            binding.saveIncome.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.prgIndicator.visibility = View.GONE
                            binding.deleteIncome.isEnabled = true
                            binding.saveIncome.isEnabled = true
                        }
                        is Resource.Loading -> {
                            binding.prgIndicator.visibility = View.VISIBLE
                            binding.deleteIncome.isEnabled = false
                            binding.saveIncome.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun saveIncome() {
        val newIncome = Income(
            null,
            dateSelected.format(sqlDateFormatter),
            binding.incomeAmount.text.toString().toFloat(),
            binding.incomeComment.text.toString()
        )

        viewModel.addIncome(newIncome).observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource) {
                    is Resource.Success -> {
                        dismiss()
                        binding.prgIndicator.visibility = View.GONE
                        binding.deleteIncome.isEnabled = true
                        binding.saveIncome.isEnabled = true
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resource.message,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.prgIndicator.visibility = View.GONE
                        binding.deleteIncome.isEnabled = true
                        binding.saveIncome.isEnabled = true
                    }
                    is Resource.Loading -> {
                        binding.prgIndicator.visibility = View.VISIBLE
                        binding.deleteIncome.isEnabled = false
                        binding.saveIncome.isEnabled = false
                    }
                }
            }
        }
    }

    private fun validateForm() {
        val incomeDate = binding.incomeDate
        val incomeAmount = binding.incomeAmount
        val saveIncome = binding.saveIncome
        saveIncome.isEnabled = true
        incomeDate.error = null
        incomeAmount.error = null

        // Store values.
        val date = incomeDate.text.toString()
        val salary = incomeAmount.text.toString()

        // Check for a valid date.
        if (date.isEmpty()) {
            incomeDate.error = getString(R.string.error_field_required)
            saveIncome.isEnabled = false
        }

        // Check for a valid salary.
        if (salary.isEmpty()) {
            incomeAmount.error = getString(R.string.error_field_required)
            saveIncome.isEnabled = false
        }
    }

    fun showForm(incItem: Income?) {
        binding.prgIndicator.visibility = View.GONE
        binding.saveIncome.isEnabled = true
        binding.deleteIncome.isEnabled = true

        binding.incomeDate.removeTextChangedListener(textWatcher)
        binding.incomeDate.error = null

        binding.incomeAmount.removeTextChangedListener(textWatcher)
        binding.incomeAmount.error = null

        binding.incomeComment.removeTextChangedListener(textWatcher)
        binding.incomeComment.error = null

        income = incItem
        if (incItem == null) {
            binding.incomeDate.addTextChangedListener(textWatcher)
            binding.incomeDate.setText(dateSelected.toShowDateFormat(incomeDateFormatter))
            binding.incomeAmount.addTextChangedListener(textWatcher)
            binding.incomeAmount.setText("")
            binding.incomeComment.addTextChangedListener(textWatcher)
            binding.incomeComment.setText("")
            binding.deleteIncome.visibility = View.GONE
            binding.saveIncome.setText(R.string.save)
        } else {
            dateSelected = try {
                LocalDate.parse(incItem.date)
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }
            binding.incomeDate.setText(dateSelected.toShowDateFormat(incomeDateFormatter))
            binding.incomeAmount.setText(incItem.amount.toString())
            binding.incomeComment.setText(incItem.comment)
            binding.deleteIncome.visibility = View.VISIBLE
            binding.saveIncome.setText(R.string.update)
            binding.incomeDate.addTextChangedListener(textWatcher)
            binding.incomeAmount.addTextChangedListener(textWatcher)
            binding.incomeComment.addTextChangedListener(textWatcher)
        }
    }

    private fun isFormValid(): Boolean {
        return binding.incomeDate.error == null && binding.incomeAmount.error == null
    }

    companion object {
        const val TAG = "IncomeDialog"
    }
}