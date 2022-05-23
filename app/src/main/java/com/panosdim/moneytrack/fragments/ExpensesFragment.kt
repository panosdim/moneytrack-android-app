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
import com.panosdim.moneytrack.adapters.ExpensesAdapter
import com.panosdim.moneytrack.databinding.FragmentExpensesBinding
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.dialogs.ExpensesFilterDialog
import com.panosdim.moneytrack.dialogs.ExpensesSortDialog
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.*
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel

class ExpensesFragment : Fragment() {
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private val expensesAdapter =
        ExpensesAdapter(mutableListOf(), mutableListOf()) { expenseItem: Expense ->
            expenseItemClicked(expenseItem)
        }
    private val expenseDialog: ExpenseDialog = ExpenseDialog()
    private val expensesSortDialog: ExpensesSortDialog = ExpensesSortDialog()
    private val expensesFilterDialog: ExpensesFilterDialog = ExpensesFilterDialog()
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { this })

    private fun expenseItemClicked(expItem: Expense) {
        expenseDialog.showNow(childFragmentManager, ExpenseDialog.TAG)
        expenseDialog.showForm(expItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.expenses.observe(viewLifecycleOwner) { list ->
            binding.rvExpenses.adapter =
                viewModel.categories.value?.let {
                    ExpensesAdapter(list, it) { expenseItem: Expense ->
                        expenseItemClicked(expenseItem)
                    }
                }
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            binding.rvExpenses.adapter =
                viewModel.expenses.value?.let {
                    ExpensesAdapter(it, list) { expenseItem: Expense ->
                        expenseItemClicked(expenseItem)
                    }
                }
        }

        binding.expSwipeRefresh.setOnRefreshListener {
            viewModel.refreshExpenses()
            binding.expSwipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val expensesRV = binding.rvExpenses
        expensesRV.setHasFixedSize(true)
        expensesRV.layoutManager = LinearLayoutManager(binding.root.context)
        expensesRV.adapter = expensesAdapter
        expensesRV.addOnScrollListener(ExtendedFloatingActionButtonScrollListener(binding.addNewExpense))

        binding.addNewExpense.setOnClickListener {
            expenseDialog.showNow(childFragmentManager, ExpenseDialog.TAG)
            expenseDialog.showForm(null)
        }

        binding.filterExpenses.setOnClickListener {
            expensesFilterDialog.showNow(childFragmentManager, ExpensesFilterDialog.TAG)
        }

        viewModel.isFilterSet.observe(viewLifecycleOwner) {
            if (it) {
                binding.filterExpenses.iconTint =
                    requireContext().resolveColorStateList(R.attr.colorIncome)
                binding.filterExpenses.setTextColor(requireContext().resolveColorAttr(R.attr.colorIncome))
            } else {
                binding.filterExpenses.iconTint =
                    requireContext().resolveColorStateList(R.attr.colorPrimary)
                binding.filterExpenses.setTextColor(requireContext().resolveColorAttr(R.attr.colorPrimary))
            }
        }

        binding.sortExpenses.setOnClickListener {
            expensesSortDialog.showNow(childFragmentManager, ExpensesSortDialog.TAG)
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
            viewModel.refreshExpenses()
        }

        binding.mtClear.setOnClickListener {
            binding.mtEditText.setText("")
            binding.mtEditText.requestFocus()
            binding.mtEditText.showKeyboard()
            viewModel.filterComment = null
            viewModel.refreshExpenses()
        }

        binding.mtEditText.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.filterComment = binding.mtEditText.text.unaccent().trim()
                binding.mtEditText.hideKeyboard()
                viewModel.refreshExpenses()
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