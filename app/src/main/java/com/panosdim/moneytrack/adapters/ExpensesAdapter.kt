package com.panosdim.moneytrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.databinding.RowExpenseBinding
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.moneyFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpensesAdapter(
    private val expensesList: List<Expense>,
    private val categoriesList: List<Category>,
    private val clickListener: (Expense) -> Unit
) :
    RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = RowExpenseBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        with(holder) {
            with(expensesList[position]) {
                val category = categoriesList.find { it.id == this.category }?.category ?: ""
                val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd-MM-yyyy")
                val date = LocalDate.parse(date)

                binding.expDate.text = date.format(dateFormatter)
                binding.expAmount.text = moneyFormat(amount)
                binding.expComment.text = comment
                binding.expCategory.text = category
                binding.expComment.isVisible = comment.isNotEmpty()

                holder.itemView.setOnClickListener { clickListener(this) }
            }
        }
    }

    override fun getItemCount() = expensesList.size

    inner class ExpenseViewHolder(val binding: RowExpenseBinding) :
        RecyclerView.ViewHolder(binding.root)
}