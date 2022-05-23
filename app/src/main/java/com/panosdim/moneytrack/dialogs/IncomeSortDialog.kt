package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.databinding.DialogIncomeSortBinding
import com.panosdim.moneytrack.viewmodel.IncomeViewModel


class IncomeSortDialog : BottomSheetDialogFragment() {
    private var _binding: DialogIncomeSortBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIncomeSortBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rgIncField.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbDate -> viewModel.sortField = IncomeViewModel.SortField.DATE
                R.id.rbSalary -> viewModel.sortField = IncomeViewModel.SortField.AMOUNT
                R.id.rbComment -> viewModel.sortField = IncomeViewModel.SortField.COMMENT
            }
            viewModel.refreshIncome()
        }

        binding.rgIncDirection.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbAscending -> viewModel.sortDirection = IncomeViewModel.SortDirection.ASC
                R.id.rbDescending -> viewModel.sortDirection = IncomeViewModel.SortDirection.DESC
            }
            viewModel.refreshIncome()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "IncomeSortDialog"
    }
}