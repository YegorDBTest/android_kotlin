package com.example.androidkotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory
import kotlin.math.roundToInt


fun dpToPx(dp: Int, context: Context): Int {
    val density: Float = context.getResources().getDisplayMetrics().density
    return (dp.toFloat() * density).roundToInt()
}


class MainActivity : AppCompatActivity() {
    private lateinit var webSocketClient: WebSocketClient

    private fun sendMessage() {
        webSocketClient.send("lol")
    }

    private fun initWebSocket() {
        val uri = URI("wss://echo.websocket.org/")
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("TEST_TAG", "onOpen")
                sendMessage()
            }

            override fun onMessage(message: String?) {
                Log.d("TEST_TAG", "onMessage: $message")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("TEST_TAG", "onClose, code: $code, reason $reason")
            }

            override fun onError(ex: Exception?) {
                Log.e("TEST_TAG", "onError: ${ex?.message}")
            }
        }
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

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

        rl.addView(tv)
        wrapper.addView(rl)


        val queue = Volley.newRequestQueue(this)
        val url = "https://www.google.com"
        val respL = Response.Listener<String> { response ->
            Log.d("TEST_TAG", "Response is: ${response.substring(0, 50)}")
        }
        val respEL = Response.ErrorListener {
            Log.e("TEST_TAG", "That didn't work!")
        }
        val stringRequest = StringRequest(Request.Method.GET, url, respL, respEL)
        queue.add(stringRequest)


        val searchText: SearchView = findViewById(R.id.searchView)
        searchText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null) return false
                Log.d("TEST_TAG", "Search query is: $query")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null) return false
                Log.d("TEST_TAG", "Search text is: $newText")
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        initWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }
}
