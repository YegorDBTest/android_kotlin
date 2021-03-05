package com.example.androidkotlin

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import kotlin.math.roundToInt


fun dpToPx(dp: Int, context: Context): Int {
    val density: Float = context.getResources().getDisplayMetrics().density
    return (dp.toFloat() * density).roundToInt()
}


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wrapper: LinearLayout = findViewById(R.id.linearLayout1)

        val rl = RelativeLayout(this)
        val rlParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
        )
        rl.layoutParams = rlParams
        rl.setPadding(dpToPx(15, this))

        val tv = TextView(this)
        val tvParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        tv.layoutParams = tvParams
        tv.setPadding(dpToPx(10, this))
        tv.text = "Lol"

        rl.addView(tv);
        wrapper.addView(rl);
    }
}
