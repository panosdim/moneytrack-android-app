package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.databinding.DialogExpensesFilterBinding
import com.panosdim.moneytrack.utils.fromEpochMilli
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.utils.toShowDateFormat
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpensesFilterDialog : BottomSheetDialogFragment() {
    private var _binding: DialogExpensesFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private var rangeDateSelected: androidx.core.util.Pair<Long, Long>? = null
    private val rangeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExpensesFilterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.expensesFilterAmount.setLabelFormatter { value: Float ->
            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0 €", symbols)
            moneyFormat.format(value)
        }

        binding.setExpensesFilters.setOnClickListener {
            viewModel.filterAmount = binding.expensesFilterAmount.values
            rangeDateSelected?.let {
                val startDate = fromEpochMilli(it.first!!)
                val endDate = fromEpochMilli(it.second!!)
                viewModel.filterDate = Pair(startDate, endDate)
            }

            if (binding.expensesFilterCategory.checkedChipIds.isNotEmpty()) {
                viewModel.filterCategory = binding.expensesFilterCategory.checkedChipIds
            } else {
                viewModel.filterCategory = null
            }

            viewModel.refreshExpenses()
            dismiss()
        }

        binding.chipThisMonth.setOnClickListener {
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

            rangeDateSelected =
                androidx.core.util.Pair(startOfMonth.toEpochMilli(), endOfMonth.toEpochMilli())
            val startDate = startOfMonth.toShowDateFormat(rangeDateFormatter)
            val endDate = endOfMonth.toShowDateFormat(rangeDateFormatter)
            binding.expensesFilterDate.setText(
                requireContext().getString(
                    R.string.date_filter,
                    startDate,
                    endDate
                )
            )
        }

        binding.chipThisYear.setOnClickListener {
            val today = LocalDate.now()
            val startOfYear = today.withDayOfMonth(1).withMonth(1)
            val endOfYear = today.withMonth(12).withDayOfMonth(31)

            rangeDateSelected =
                androidx.core.util.Pair(startOfYear.toEpochMilli(), endOfYear.toEpochMilli())
            val startDate = startOfYear.toShowDateFormat(rangeDateFormatter)
            val endDate = endOfYear.toShowDateFormat(rangeDateFormatter)
            binding.expensesFilterDate.setText(
                requireContext().getString(
                    R.string.date_filter,
                    startDate,
                    endDate
                )
            )
        }

        binding.chipPreviousMonth.setOnClickListener {
            val previousMonth = LocalDate.now().minusMonths(1)
            val startOfMonth = previousMonth.withDayOfMonth(1)
            val endOfMonth = previousMonth.withDayOfMonth(previousMonth.lengthOfMonth())

            rangeDateSelected =
                androidx.core.util.Pair(startOfMonth.toEpochMilli(), endOfMonth.toEpochMilli())
            val startDate = startOfMonth.toShowDateFormat(rangeDateFormatter)
            val endDate = endOfMonth.toShowDateFormat(rangeDateFormatter)
            binding.expensesFilterDate.setText(
                requireContext().getString(
                    R.string.date_filter,
                    startDate,
                    endDate
                )
            )
        }

        binding.expensesFilterDate.setOnClickListener {
            //Date Picker
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setOpenAt(LocalDate.now().toEpochMilli())
            builder.setCalendarConstraints(constraintsBuilder.build())
            rangeDateSelected?.let {
                builder.setSelection(rangeDateSelected)
            }
            builder.setTitleText("Select Range")

            val picker: MaterialDatePicker<androidx.core.util.Pair<Long, Long>> = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                rangeDateSelected = selection
                rangeDateSelected?.let {
                    val startDate = fromEpochMilli(it.first!!).toShowDateFormat(rangeDateFormatter)
                    val endDate = fromEpochMilli(it.second!!).toShowDateFormat(rangeDateFormatter)
                    binding.expensesFilterDate.setText(
                        requireContext().getString(
                            R.string.date_filter,
                            startDate,
                            endDate
                        )
                    )
                }
            }

            picker.show(childFragmentManager, picker.toString())
        }

        viewModel.expenses.value?.let { list ->
            val min = list.minByOrNull { it.amount }
            val max = list.maxByOrNull { it.amount }
            if (min != null && max != null) {
                binding.expensesFilterAmount.values = listOf(min.amount, max.amount)
            }
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
                binding.expensesFilterCategory.addView(chip)
            }
        }

        binding.expensesFilterCategory.post {
            binding.expensesFilterCategory.checkedChipIds.let {
                try {
                    val chip =
                        binding.expensesFilterCategory.findViewById<Chip>(it.first())
                    binding.nestedScrollView.scrollTo(0, chip.top)
                } catch (e: NoSuchElementException) {
                    binding.nestedScrollView.scrollTo(0, 0)
                }
            }
        }

        binding.clearExpensesFilters.setOnClickListener {
            viewModel.clearFilters()

            viewModel.expenses.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                binding.expensesFilterAmount.values = listOf(min?.amount, max?.amount)
            }
            binding.expensesFilterDate.setText("")
            rangeDateSelected = null

            dismiss()
        }

        @Suppress("DEPRECATION")
        this.dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        viewModel.filterAmount?.let {
            binding.expensesFilterAmount.values = it
        } ?: kotlin.run {
            viewModel.expenses.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                binding.expensesFilterAmount.values = listOf(min?.amount, max?.amount)
            }
        }

        viewModel.filterDate?.let {
            val startDate = it.first.toShowDateFormat(rangeDateFormatter)
            val endDate = it.second.toShowDateFormat(rangeDateFormatter)
            binding.expensesFilterDate.setText(
                requireContext().getString(
                    R.string.date_filter,
                    startDate,
                    endDate
                )
            )
        } ?: kotlin.run {
            rangeDateSelected = null
            binding.expensesFilterDate.setText("")
        }
    }

    companion object {
        const val TAG = "ExpensesFilterDialog"
    }
}