package com.panosdim.moneytrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.databinding.RowIncomeBinding
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.moneyFormat

class IncomeAdapter(
    private val incomeList: List<Income>,
    private val clickListener: (Income) -> Unit
) :
    RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = RowIncomeBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return IncomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        with(holder) {
            with(incomeList[position]) {
                val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")
                val date = java.time.LocalDate.parse(date)

                binding.incDate.text = date.format(dateFormatter)
                binding.incSalary.text = moneyFormat(amount)
                binding.incComment.text = comment
                binding.incComment.isVisible = comment.isNotEmpty()
                holder.itemView.setOnClickListener { clickListener(this) }
            }
        }
    }

    override fun getItemCount() = incomeList.size

    inner class IncomeViewHolder(val binding: RowIncomeBinding) :
        RecyclerView.ViewHolder(binding.root)
}