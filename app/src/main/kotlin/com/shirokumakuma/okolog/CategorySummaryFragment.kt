package com.shirokumakuma.okolog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shirokumakuma.okolog.R
import com.shirokumakuma.okolog.data.AppDatabase
import kotlinx.coroutines.launch
import java.util.*

class CategorySummaryFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: CategorySummaryAdapter
    private lateinit var totalAmountTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_category_summary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        val yearSpinner = view.findViewById<Spinner>(R.id.year_spinner)
        val monthSpinner = view.findViewById<Spinner>(R.id.month_spinner)
        totalAmountTextView = view.findViewById(R.id.text_total_summary)
        val recyclerView = view.findViewById<RecyclerView>(R.id.category_summary_recycler)

        adapter = CategorySummaryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-11

        val years = (currentYear - 10..currentYear + 1).map { it.toString() }
        val months = (1..12).map { String.format("%02d", it) }

        yearSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        monthSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        yearSpinner.setSelection(years.indexOf(currentYear.toString()))
        monthSpinner.setSelection(currentMonth)

        val loadData = {
            val year = yearSpinner.selectedItem.toString()
            val month = monthSpinner.selectedItem.toString()
            val ym = "$year-$month"

            lifecycleScope.launch {
                val categoryTotals = db.transactionItemModelDao().getCategoryTotalsByMonth(ym)
                val items = categoryTotals.map {
                    CategorySummary(it.categoryId, it.name, it.total)
                }
                val grandTotal = items.sumOf { it.totalAmount }
                adapter.submitList(items)
                totalAmountTextView.text = "合計: ¥%,d".format(grandTotal)

                // 収支に合わせて文字色付け
                if(grandTotal < 0)
                {
                    totalAmountTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    totalAmountTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                }

            }
        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                loadData()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                loadData()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        loadData()
    }
}