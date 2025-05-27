package com.example.okolog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.Toast


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
    }

    // 日付選択ダイアログの登録処理オーバーライド
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val dateText: TextView = findViewById(R.id.date_text)
        val selectedDate = "$year/${month.plus(1)}/$dayOfMonth"
        dateText.text = selectedDate
    }
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