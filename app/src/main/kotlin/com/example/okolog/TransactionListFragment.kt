package com.example.okolog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.okolog.data.AppDatabase
import com.example.okolog.data.Category
import com.example.okolog.data.TransactionItemModel
import com.example.okolog.data.TransactionItemModelDao
import com.example.okolog.data.TransactionWithCategory
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionListFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter
    private lateinit var currentTransactionsLiveData: LiveData<List<TransactionWithCategory>>
    private lateinit var totalTextView: TextView
    private var selectedCategoryId: Int? = null

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
        totalTextView = view.findViewById(R.id.text_total_amount)

        // 現在の年と月を取得
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // 年リストを生成（例：現在年の10年前から1年後まで）
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }

        // 月リスト（01〜12）
        val months = (1..12).map { String.format("%02d", it) }

        // アダプターセット
        yearSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        monthSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months).apply {
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
                // 表示レコードをフィルター
                // すべての場合selectedCategoryIdはnull
                val filtered = selectedCategoryId?.let { id ->
                    transactions.filter { it.transaction.categoryId == id }
                } ?: transactions

                adapter.submitList(filtered)

                // 合計金額の計算と表示
                val total = filtered.sumOf {
                    if (it.transaction.isExpense) {
                        -it.transaction.amount.toLong()
                    } else {
                        it.transaction.amount.toLong()
                    }
                }

                val formatted = "合計: %,d円".format(total)
                totalTextView.text = formatted

                // 合計金額によって文字色の変更
                val color = if (total < 0) {
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                } else {
                    ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                }
                totalTextView.setTextColor(color)
            }
        }

        // Spinner選択変更時にデータをロード
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                loadSelectedMonth()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                loadSelectedMonth()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // レコード一覧RecyclerViewセットアップ
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter(
            onDeleteClick  = { transactionWithCategory ->
                lifecycleScope.launch {
                    db.transactionItemModelDao().delete(transactionWithCategory.transaction)
                }
            },
            onEditClick = { transactionWithCategory ->
                showEditTransactionDialog(transactionWithCategory.transaction)
            }
        )
        recyclerView.adapter = adapter

        // カテゴリ一覧RecyclerViewセットアップ
        val categoryRecyclerView = view.findViewById<RecyclerView>(R.id.category_tab_list)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        db.categoryDao().getAll().observe(viewLifecycleOwner) { categories ->
            val activeCategories = categories.filter { !it.isDisabled }
            categoryRecyclerView.adapter =
                CategoryTabAdapter(activeCategories) { selectedCategory ->
                    selectedCategoryId = selectedCategory?.id
                    loadSelectedMonth()
                }
        }

        // 初期データロード
        loadSelectedMonth()
    }

    fun showEditTransactionDialog(transaction: TransactionItemModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_transaction, null)

        val dateText = dialogView.findViewById<TextView>(R.id.date_text)
        val btnPickDate = dialogView.findViewById<Button>(R.id.btn_pick_date)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_type)
        val radioExpense = dialogView.findViewById<RadioButton>(R.id.radio_expense)
        val radioIncome = dialogView.findViewById<RadioButton>(R.id.radio_income)
        val etMemo = dialogView.findViewById<EditText>(R.id.et_memo)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)

        // 日付を「yyyy/MM/dd」形式に変換して表示
        val displayDate = transaction.date.replace("-", "/")
        dateText.text = displayDate

        etAmount.setText(transaction.amount.toString())
        if (transaction.isExpense) {
            radioExpense.isChecked = true
        } else {
            radioIncome.isChecked = true
        }
        etMemo.setText(transaction.memo ?: "")

        // カテゴリ一覧を取得してスピナーにセット
        lifecycleScope.launch {
            val activeCategories = db.categoryDao().getAllOnce().filter { !it.isDisabled }
            val categories = mutableListOf<Category>()

            // 有効カテゴリをまず追加
            categories.addAll(activeCategories)

            // categoryIdがnullなら、通常は有効カテゴリ一覧だけをスピナーにセットし、選択なしにする
            // 編集対象カテゴリ
            if (transaction.categoryId == null) {
                // 選択インデックスは-1(選択なし)
                spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerCategory.setSelection(-1)  // 選択なし
            } else {
                // 編集対象カテゴリが無効でリストに無ければ追加する
                val currentCategory = db.categoryDao().getById(transaction.categoryId)
                if (currentCategory != null
                    && currentCategory.isDisabled
                    && categories.none { it.id == currentCategory.id }
                ) {
                    categories.add(currentCategory)
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter

                // 編集対象カテゴリのインデックスを取得して選択
                val selectedIndex = categories.indexOfFirst { it.id == transaction.categoryId }
                if (selectedIndex >= 0) spinnerCategory.setSelection(selectedIndex)
            }

            // 日付選択ボタン押下時にDatePicker表示
            btnPickDate.setOnClickListener {
                val parts = dateText.text.toString().split("/")
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()

                DatePickerDialog(requireContext(), { _, y, m, d ->
                    dateText.text = "%04d/%02d/%02d".format(y, m + 1, d)
                }, year, month, day).show()
            }

            AlertDialog.Builder(requireContext())
                .setTitle("取引を編集")
                .setView(dialogView)
                .setPositiveButton("保存") { _, _ ->
                    val newDate = dateText.text.toString().replace("/", "-")
                    val newAmount = etAmount.text.toString().toIntOrNull() ?: 0
                    val newIsExpense = radioExpense.isChecked
                    val newMemo = etMemo.text.toString()

                    // 金額チェック（9999999以下）
                    if (newAmount > 9999999) {
                        Toast.makeText(requireContext(), "金額は9999999以下で入力してください", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // メモ文字数チェック（20文字以内）
                    if (newMemo.length > 20) {
                        Toast.makeText(requireContext(), "メモは20文字以内で入力してください", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val selectedCategoryIndex = spinnerCategory.selectedItemPosition

                    lifecycleScope.launch {
                        if (selectedCategoryIndex in categories.indices) {
                            val newCategoryId = categories[selectedCategoryIndex].id

                            val updatedTransaction = transaction.copy(
                                date = newDate,
                                amount = newAmount,
                                isExpense = newIsExpense,
                                memo = newMemo,
                                categoryId = newCategoryId
                            )
                            db.transactionItemModelDao().update(updatedTransaction)

                            Toast.makeText(
                                requireContext(),
                                "取引を更新しました",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "カテゴリが選択されていません",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("キャンセル", null)
                .show()
        }
    }
}
