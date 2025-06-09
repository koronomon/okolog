package com.shirokumakuma.okolog
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shirokumakuma.okolog.data.AppDatabase
import kotlinx.coroutines.launch
import com.shirokumakuma.okolog.data.Category
import com.shirokumakuma.okolog.data.TransactionItemModel
import java.util.Date
import java.util.Locale


class RegisterFragment : Fragment() , DatePickerDialog.OnDateSetListener {

    // カテゴリボタンの変数
    private lateinit var categoryContainer: LinearLayout

    // DB
    private lateinit var db: AppDatabase


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.register_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 今日の日付を取得して表示
        val dateText: TextView = view.findViewById(R.id.date_text)
        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val today = Date()
        dateText.text = formatter.format(today)

        // 日付選択ボタンの動作登録
        val dateButton: Button = view.findViewById(R.id.btn_pick_date)
        dateButton.setOnClickListener {
            val dateFragment = DatePick { selectedDate ->
                dateText.text = selectedDate
            }
            dateFragment.show(parentFragmentManager, "datePicker")
        }

        // DB初期化
        db = AppDatabase.getDatabase(requireContext())

        // カテゴリボタン表示
        categoryContainer = view.findViewById(R.id.category_container)
        db.categoryDao().getAll().observe(viewLifecycleOwner) { categories ->
            updateCategoryButtons(categories, view)
        }

        val addCategoryButton = view.findViewById<Button>(R.id.btn_add_category)
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    // 日付選択ダイアログの登録処理オーバーライド
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val dateText: TextView = requireView().findViewById(R.id.date_text)
        val selectedDate = "$year/${month.plus(1)}/$dayOfMonth"
        dateText.text = selectedDate
    }

    // カテゴリ登録ダイアログ
    private fun showAddCategoryDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "カテゴリ名"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(requireContext())
            .setTitle("カテゴリを追加")
            .setView(editText)
            .setPositiveButton("追加") { _, _ ->
                val categoryName = editText.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    // カテゴリ登録処理
                    insertCategory(categoryName)
                } else {
                    Toast.makeText(requireContext(), "カテゴリ名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    // カテゴリ登録処理
    private fun insertCategory(name: String) {
        lifecycleScope.launch {
            val category = Category(name = name) // ID は自動生成される想定
            db.categoryDao().insert(category)
            Toast.makeText(requireContext(), "カテゴリを追加しました", Toast.LENGTH_SHORT).show()
        }
    }

    // カテゴリボタン表示処理
    private fun updateCategoryButtons(categories: List<Category>, view: View) {
        categoryContainer.removeAllViews()
        categories.forEach { category ->
            val button = Button(requireContext()).apply {
                text = category.name
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
            }
            // 短押しでレコード追加
            button.setOnClickListener {
                // 日付取得
                val dateText: TextView = view.findViewById(R.id.date_text)
                val original = dateText.text.toString()
                val converted = original.replace("/", "-")
                // 金額取得
                val amountText: EditText = view.findViewById(R.id.et_amount)
                val amount = amountText.text.toString().toIntOrNull() ?: 0
                if (amount > 9999999) {
                    Toast.makeText(requireContext(), "金額は9999999以下で入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 支出収入取得
                val expenseRadio: RadioButton = view.findViewById(R.id.radio_expense)
                // 支出にチェックあり
                val isExpense = expenseRadio.isChecked
                // メモ取得
                val memoText: EditText = view.findViewById(R.id.et_memo)

                // ここで文字数チェック
                if (memoText.text.length > 20) {
                    Toast.makeText(requireContext(), "メモは20文字以内で入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // レコード追加
                addTransaction(converted, amount, isExpense, memoText.text.toString(), category.id)

                // 金額とメモを空にする
                amountText.text = null
                memoText.text = null
            }
            // 長押しで無効化ダイアログ表示
            button.setOnLongClickListener {
                showDisableCategoryDialog(category)
                true // 長押しイベントを消費したことを示す
            }
            categoryContainer.addView(button)
        }
    }

    // レコード追加処理
    fun addTransaction(
        date: String,
        amount: Int,
        isExpense: Boolean,
        memo: String?,
        categoryId: Int
    ) {
        val transactionDao = db.transactionItemModelDao()

        lifecycleScope.launch {
            val transaction = TransactionItemModel(
                date = date,
                amount = amount,
                isExpense = isExpense,
                memo = memo,
                categoryId = categoryId
            )
            transactionDao.insert(transaction)
            // 追加完了のメッセージなどUI更新
            Toast.makeText(requireContext(), "取引を追加しました", Toast.LENGTH_SHORT).show()
        }
    }


    // カテゴリ無効化ダイアログ表示
    private fun showDisableCategoryDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("カテゴリを削除")
            .setMessage("カテゴリ${category.name} を削除しますか？")
            .setPositiveButton("はい") { _, _ ->
                disableCategory(category)
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    // カテゴリ無効化処理
    private fun disableCategory(category: Category) {
        lifecycleScope.launch {
            val disabledCategory = category.copy(isDisabled = true)
            db.categoryDao().update(disabledCategory)
            Toast.makeText(requireContext(), "${category.name} を削除しました", Toast.LENGTH_SHORT).show()
        }
    }
}
