package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.databinding.FragmentDashboardBinding
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.utils.resolveColorAttr
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import java.time.LocalDate
import java.time.Month

class DashboardFragment : Fragment(), OnChartValueSelectedListener {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val expensesViewModel: ExpensesViewModel by viewModels(ownerProducer = { this })
    private val incomeViewModel: IncomeViewModel by viewModels(ownerProducer = { this })
    private lateinit var monthExpensesPerCategories: List<PieEntry>
    private val today = LocalDate.now()
    private var selectedMonth = today.month
    private var selectedYear = today.year
    private var startOfMonth = today.withDayOfMonth(1)
    private var endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
    private var startOfYear = today.withDayOfMonth(1).withMonth(1)
    private var endOfYear = today.withMonth(12).withDayOfMonth(31)
    private var incomeList: List<Income> = mutableListOf()
    private var expensesList: List<Expense> = mutableListOf()
    private var categoriesList: List<Category> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        expensesViewModel.expenses.observe(viewLifecycleOwner) {
            expensesList = it
            initializeSavings()
            initializeChart()
        }

        expensesViewModel.categories.observe(viewLifecycleOwner) {
            categoriesList = it
            initializeSavings()
            initializeChart()
        }

        incomeViewModel.income.observe(viewLifecycleOwner) {
            incomeList = it
            initializeSavings()
            initializeChart()
        }

        return root
    }

    // Fix bug that when reselect again the Dashboard fragment the dropdowns have only one value to
    // select. See https://github.com/material-components/material-components-android/issues/2012#issuecomment-868181589
    override fun onResume() {
        super.onResume()

        val monthAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            Month.values()
        )
        binding.overviewMonth.setAdapter(monthAdapter)
        // Workaround as there is a bug in AutoCompleteView filter setting
        binding.overviewMonth.setText(selectedMonth.name, false)

        binding.overviewMonth.setOnItemClickListener { parent, _, position, _ ->
            selectedMonth = parent.getItemAtPosition(position) as Month
            val s = LocalDate.of(selectedYear, selectedMonth, 1)
            startOfMonth = s.withDayOfMonth(1)
            endOfMonth = s.withDayOfMonth(s.lengthOfMonth())
            startOfYear = s.withDayOfMonth(1).withMonth(1)
            endOfYear = s.withMonth(12).withDayOfMonth(31)
            initializeSavings()
            initializeChart()
        }

        val currentYear = today.year
        val years = IntArray(4) { currentYear - it }
        val yearAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            years.toTypedArray()
        )
        binding.overviewYear.setAdapter(yearAdapter)
        // Workaround as there is a bug in AutoCompleteView filter setting
        binding.overviewYear.setText(selectedYear.toString(), false)

        binding.overviewYear.setOnItemClickListener { parent, _, position, _ ->
            selectedYear = parent.getItemAtPosition(position) as Int
            val s = LocalDate.of(selectedYear, selectedMonth, 1)
            startOfYear = s.withDayOfMonth(1).withMonth(1)
            endOfYear = s.withMonth(12).withDayOfMonth(31)
            startOfMonth = s.withDayOfMonth(1)
            endOfMonth = s.withDayOfMonth(s.lengthOfMonth())
            initializeSavings()
            initializeChart()
        }
    }

    private fun initializeSavings() {
        val totalMonthIncome = incomeList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }.sumOf { it.amount.toBigDecimal() }

        val totalMonthExpenses = expensesList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }.sumOf { it.amount.toBigDecimal() }

        val totalMonthSavings = totalMonthIncome - totalMonthExpenses

        binding.txtMonthSavings.text = moneyFormat(totalMonthSavings)
        binding.txtMonthSavings.setTextColor(calculateColor(totalMonthSavings.toFloat()))

        val totalYearIncome = incomeList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfYear) || date.isEqual(startOfYear)) &&
                    (date.isBefore(endOfYear) || date.isEqual(endOfYear))
        }.sumOf { it.amount.toBigDecimal() }

        val totalYearExpenses = expensesList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfYear) || date.isEqual(startOfYear)) &&
                    (date.isBefore(endOfYear) || date.isEqual(endOfYear))
        }.sumOf { it.amount.toBigDecimal() }

        val totalYearSavings = totalYearIncome - totalYearExpenses

        binding.txtYearSavings.text = moneyFormat(totalYearSavings)
        binding.txtYearSavings.setTextColor(calculateColor(totalYearSavings.toFloat()))
    }

    private fun calculateColor(value: Float): Int {
        @ColorInt val positiveSavings = requireContext().resolveColorAttr(R.attr.colorIncome)
        @ColorInt val negativeSavings = requireContext().resolveColorAttr(R.attr.colorExpense)
        return if (value < 0) negativeSavings else positiveSavings
    }

    private fun initializeChart() {
        monthExpensesPerCategories = expensesList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }
            .groupBy { it.category }
            .map { (k, v) ->
                PieEntry(
                    v.sumOf { it.amount.toBigDecimal() }.toFloat(),
                    categoriesList.find { it.id == k }?.category ?: ""
                )

            }
            .sortedBy { it.value }

        val set = PieDataSet(monthExpensesPerCategories, "Expenses Per Categories")

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())

        set.colors = colors

        val data = PieData(set)
        with(binding.chart) {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setOnChartValueSelectedListener(this@DashboardFragment)
            data.setValueFormatter(PercentFormatter(this))
            this.data = data
            onTouchListener.setLastHighlighted(null)
            highlightValues(null)
            centerText = ""
            invalidate()
        }
    }

    override fun onNothingSelected() {
        binding.chart.centerText = SpannableString("")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null && h != null) {
            val catEntry = monthExpensesPerCategories[h.x.toInt()]
            val centerString = SpannableString(catEntry.label + "\n" + moneyFormat(e.y))
            centerString.setSpan(RelativeSizeSpan(2f), 0, catEntry.label.length, 0)
            centerString.setSpan(
                RelativeSizeSpan(3f),
                catEntry.label.length + 1,
                centerString.length,
                0
            )
            centerString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.holo_red_dark
                    )
                ),
                catEntry.label.length + 1,
                centerString.length,
                0
            )
            binding.chart.centerText = centerString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}