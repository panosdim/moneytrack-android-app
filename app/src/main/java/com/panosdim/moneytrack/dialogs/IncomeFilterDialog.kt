package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.databinding.DialogIncomeFilterBinding
import com.panosdim.moneytrack.utils.fromEpochMilli
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.utils.toShowDateFormat
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.util.Pair as APair


class IncomeFilterDialog : BottomSheetDialogFragment() {
    private var _binding: DialogIncomeFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private var rangeDateSelected: APair<Long, Long>? = null
    private val rangeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIncomeFilterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.incomeFilterAmount.setLabelFormatter { value: Float ->
            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0 €", symbols)
            moneyFormat.format(value)
        }

        binding.setIncomeFilters.setOnClickListener {
            viewModel.filterAmount = binding.incomeFilterAmount.values
            rangeDateSelected?.let {
                val startDate = fromEpochMilli(it.first!!)
                val endDate = fromEpochMilli(it.second!!)
                viewModel.filterDate = Pair(startDate, endDate)
            }

            viewModel.refreshIncome()
            dismiss()
        }

        binding.incomeFilterDate.setOnClickListener {
            //Date Picker
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setOpenAt(LocalDate.now().toEpochMilli())
            builder.setCalendarConstraints(constraintsBuilder.build())
            rangeDateSelected?.let {
                builder.setSelection(rangeDateSelected)
            }
            builder.setTitleText("Select Range")

            val picker: MaterialDatePicker<APair<Long, Long>> = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                rangeDateSelected = selection
                rangeDateSelected?.let {
                    val startDate = fromEpochMilli(it.first!!).toShowDateFormat(rangeDateFormatter)
                    val endDate = fromEpochMilli(it.second!!).toShowDateFormat(rangeDateFormatter)
                    binding.incomeFilterDate.setText(
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

        binding.chipThisMonth.setOnClickListener {
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

            rangeDateSelected =
                androidx.core.util.Pair(startOfMonth.toEpochMilli(), endOfMonth.toEpochMilli())
            val startDate = startOfMonth.toShowDateFormat(rangeDateFormatter)
            val endDate = endOfMonth.toShowDateFormat(rangeDateFormatter)
            binding.incomeFilterDate.setText(
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
            binding.incomeFilterDate.setText(
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
            binding.incomeFilterDate.setText(
                requireContext().getString(
                    R.string.date_filter,
                    startDate,
                    endDate
                )
            )
        }

        viewModel.income.value?.let { list ->
            val min = list.minByOrNull { it.amount }
            val max = list.maxByOrNull { it.amount }
            if (min != null && max != null) {
                binding.incomeFilterAmount.values = listOf(min.amount, max.amount)
            }
        }

        binding.clearIncomeFilters.setOnClickListener {
            viewModel.clearFilters()
            viewModel.income.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                binding.incomeFilterAmount.values = listOf(min?.amount, max?.amount)
            }
            binding.incomeFilterDate.setText("")
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
            binding.incomeFilterAmount.values = it
        } ?: kotlin.run {
            viewModel.income.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                binding.incomeFilterAmount.values = listOf(min?.amount, max?.amount)
            }
        }

        viewModel.filterDate?.let {
            val startDate = it.first.toShowDateFormat(rangeDateFormatter)
            val endDate = it.second.toShowDateFormat(rangeDateFormatter)
            binding.incomeFilterDate.setText(
                requireContext().getString(
                    R.string.date_filter,
                    startDate,
                    endDate
                )
            )
        } ?: kotlin.run {
            rangeDateSelected = null
            binding.incomeFilterDate.setText("")
        }
    }

    companion object {
        const val TAG = "IncomeFilterDialog"
    }
}