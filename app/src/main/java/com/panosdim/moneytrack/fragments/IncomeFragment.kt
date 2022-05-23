package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.IncomeAdapter
import com.panosdim.moneytrack.databinding.FragmentIncomeBinding
import com.panosdim.moneytrack.dialogs.IncomeDialog
import com.panosdim.moneytrack.dialogs.IncomeFilterDialog
import com.panosdim.moneytrack.dialogs.IncomeSortDialog
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.*
import com.panosdim.moneytrack.viewmodel.IncomeViewModel

class IncomeFragment : Fragment() {
    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private val incomeAdapter =
        IncomeAdapter(mutableListOf()) { incomeItem: Income ->
            incomeItemClicked(
                incomeItem
            )
        }
    private val incomeDialog: IncomeDialog = IncomeDialog()
    private val incomeSortDialog: IncomeSortDialog = IncomeSortDialog()
    private val incomeFilterDialog: IncomeFilterDialog = IncomeFilterDialog()
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { this })

    private fun incomeItemClicked(incItem: Income) {
        incomeDialog.showNow(childFragmentManager, IncomeDialog.TAG)
        incomeDialog.showForm(incItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.income.observe(viewLifecycleOwner) { list ->
            binding.rvIncome.adapter =
                IncomeAdapter(list) { incomeItem: Income ->
                    incomeItemClicked(
                        incomeItem
                    )
                }
        }

        binding.incSwipeRefresh.setOnRefreshListener {
            viewModel.refreshIncome()
            binding.incSwipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val incomeRV = binding.rvIncome
        incomeRV.setHasFixedSize(true)
        incomeRV.layoutManager = LinearLayoutManager(binding.root.context)
        incomeRV.adapter = incomeAdapter
        incomeRV.addOnScrollListener(ExtendedFloatingActionButtonScrollListener(binding.addNewIncome))

        binding.addNewIncome.setOnClickListener {
            incomeDialog.showNow(childFragmentManager, IncomeDialog.TAG)
            incomeDialog.showForm(null)
        }

        binding.filterIncome.setOnClickListener {
            incomeFilterDialog.showNow(childFragmentManager, IncomeFilterDialog.TAG)
        }

        viewModel.isFilterSet.observe(viewLifecycleOwner) {
            if (it) {
                binding.filterIncome.iconTint =
                    requireContext().resolveColorStateList(R.attr.colorIncome)
                binding.filterIncome.setTextColor(requireContext().resolveColorAttr(R.attr.colorIncome))
            } else {
                binding.filterIncome.iconTint =
                    requireContext().resolveColorStateList(R.attr.colorPrimary)
                binding.filterIncome.setTextColor(requireContext().resolveColorAttr(R.attr.colorPrimary))
            }
        }

        binding.sortIncome.setOnClickListener {
            incomeSortDialog.showNow(childFragmentManager, IncomeSortDialog.TAG)
        }

        binding.searchComments.setOnClickListener {
            binding.inputContainer.isVisible = true
            binding.buttonsContainer.isVisible = false
            binding.mtEditText.requestFocus()
            binding.mtEditText.showKeyboard()
        }

        binding.mtArrow.setOnClickListener {
            binding.inputContainer.isVisible = false
            binding.buttonsContainer.isVisible = true
            binding.mtEditText.setText("")
            binding.mtEditText.hideKeyboard()
            viewModel.filterComment = null
            viewModel.refreshIncome()
        }

        binding.mtClear.setOnClickListener {
            binding.mtEditText.setText("")
            binding.mtEditText.requestFocus()
            binding.mtEditText.showKeyboard()
            viewModel.filterComment = null
            viewModel.refreshIncome()
        }

        binding.mtEditText.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.filterComment = binding.mtEditText.text.unaccent().trim()
                binding.mtEditText.hideKeyboard()
                viewModel.refreshIncome()
            }
            false
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}