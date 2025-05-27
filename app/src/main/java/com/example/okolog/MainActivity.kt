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
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener {

    // カテゴリボタンの変数
    private lateinit var categoryContainer: LinearLayout

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

        // カテゴリボタンの表示
        categoryContainer = findViewById(R.id.category_container)

        // スタブカテゴリ（仮データ）
        val stubCategories = listOf("食費", "交通", "娯楽", "医療", "通信")
        // カテゴリボタンを生成して追加
        stubCategories.forEach { category ->
            val button = Button(this).apply {
                text = category
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
            }

            // 仮のクリックイベント
            button.setOnClickListener {
                Toast.makeText(this, "$category が選ばれました", Toast.LENGTH_SHORT).show()
            }

            categoryContainer.addView(button)

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
//                    insertCategory(categoryName)
                    Toast.makeText(this, "カテゴリを登録しました", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "カテゴリ名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

//    private fun insertCategory(name: String) {
//        lifecycleScope.launch {
//            val category = Locale.Category(name = name) // ID は自動生成される想定
//            db.categoryDao().insert(category)
//            Toast.makeText(this@AddTransactionActivity, "カテゴリを追加しました", Toast.LENGTH_SHORT).show()
//        }
//    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    OkoLogTheme {
//        Greeting("Android")
//    }
//}