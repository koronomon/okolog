package com.example.okolog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import android.widget.DatePicker


class MainActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // activity_main.xmlを指定
        setContentView(R.layout.activity_main)

        val dateButton: Button = findViewById(R.id.btn_pick_date)

        dateButton.setOnClickListener {
            val dateFragment = DatePick()
            dateFragment.show(supportFragmentManager, "datePicker")
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val dateText:TextView =findViewById(R.id.date_text)
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