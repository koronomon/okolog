package com.example.okolog
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.okolog.R
import com.example.okolog.data.AppDatabase
import kotlinx.coroutines.launch
import com.example.okolog.data.Category


class MainActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener {

    // カテゴリボタンの変数
    private lateinit var categoryContainer: LinearLayout

    // DB
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        // activity_main.xmlを指定
        setContentView(R.layout.activity_main)

        // 日付選択ボタンの動作登録
        val dateButton: Button = findViewById(R.id.btn_pick_date)
        dateButton.setOnClickListener {
            val dateFragment = DatePick()
            dateFragment.show(supportFragmentManager, "datePicker")
        }

        // DBコンテキスト初期化
        db = AppDatabase.getDatabase(applicationContext)

        // カテゴリボタンの表示
        // LiveDataを監視してカテゴリボタン描画
        categoryContainer = findViewById(R.id.category_container)
        db.categoryDao().getAll().observe(this) { categories ->
            updateCategoryButtons(categories)
        }

        // カテゴリ追加ボタン押下時イベント登録
        val addCategoryButton = findViewById<Button>(R.id.btn_add_category)
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }



    }

    // 日付選択ダイアログの登録処理オーバーライド
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val dateText: TextView = findViewById(R.id.date_text)
        val selectedDate = "$year/${month.plus(1)}/$dayOfMonth"
        dateText.text = selectedDate
    }

    // カテゴリ登録ダイアログ
    private fun showAddCategoryDialog() {
        val editText = EditText(this).apply {
            hint = "カテゴリ名"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(this)
            .setTitle("カテゴリを追加")
            .setView(editText)
            .setPositiveButton("追加") { _, _ ->
                val categoryName = editText.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    // カテゴリ登録処理
                    insertCategory(categoryName)
                } else {
                    Toast.makeText(this, "カテゴリ名を入力してください", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@MainActivity, "カテゴリを追加しました", Toast.LENGTH_SHORT).show()
        }
    }

    // カテゴリボタン表示処理
    private fun updateCategoryButtons(categories: List<Category>) {
        categoryContainer.removeAllViews()
        categories.forEach { category ->
            val button = Button(this).apply {
                text = category.name
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
            }
            // 短押しで選択
            button.setOnClickListener {
                Toast.makeText(this, "${category.name} が選ばれました", Toast.LENGTH_SHORT).show()
            }
            // 長押しで無効化ダイアログ表示
            button.setOnLongClickListener {
                showDisableCategoryDialog(category)
                true  // 長押しイベントを消費したことを示す
            }
            categoryContainer.addView(button)
        }
    }

    // カテゴリ無効化ダイアログ表示
    private fun showDisableCategoryDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("カテゴリを無効化")
            .setMessage("${category.name} を無効化しますか？")
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
            Toast.makeText(this@MainActivity, "${category.name} を無効化しました", Toast.LENGTH_SHORT).show()
        }
    }
}
