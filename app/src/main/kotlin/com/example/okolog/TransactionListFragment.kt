package com.example.okolog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.okolog.data.AppDatabase
import com.example.okolog.data.TransactionWithCategory
import java.util.Calendar

class TransactionListFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter
    private lateinit var currentTransactionsLiveData: LiveData<List<TransactionWithCategory>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        val yearSpinner = view.findViewById<Spinner>(R.id.year_spinner)
        val monthSpinner = view.findViewById<Spinner>(R.id.month_spinner)

        // 現在の年と月を取得
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // 年リストを生成（例：現在年の10年前から1年後まで）
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }

        // 月リスト（01〜12）
        val months = (1..12).map { String.format("%02d", it) }

        // アダプターセット
        yearSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        monthSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Spinnerの初期選択
        yearSpinner.setSelection(years.indexOf(currentYear.toString()))
        monthSpinner.setSelection(currentMonth)

        fun loadSelectedMonth() {
            val selectedYear = yearSpinner.selectedItem.toString()
            val selectedMonth = monthSpinner.selectedItem.toString()
            val targetMonth = "$selectedYear-$selectedMonth"
            // LiveData更新のためにobserveを切り替え
            if (::currentTransactionsLiveData.isInitialized) {
                currentTransactionsLiveData.removeObservers(viewLifecycleOwner)
            }
            currentTransactionsLiveData = db.transactionItemModelDao().getByMonth(targetMonth)
            currentTransactionsLiveData.observe(viewLifecycleOwner) { transactions ->
                adapter.submitList(transactions)
            }
        }

        // Spinner選択変更時にデータをロード
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadSelectedMonth()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadSelectedMonth()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // RecyclerViewセットアップ
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        // 初期データロード
        loadSelectedMonth()
    }
}
