package com.example.androidkotlin

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class SearchableActivity : AppCompatActivity() {
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        supportActionBar?.hide()

        queue = Volley.newRequestQueue(this)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        val result = super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        return result
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                Log.d("TEST_TAG", "Search query: $query")
                val url = "https://httpbin.org/get?query=$query"
                val respL = Response.Listener<JSONObject> { response ->
                    Log.d("TEST_TAG", "Response is: $response")
                }
                val respEL = Response.ErrorListener {
                    Log.e("TEST_TAG", "That didn't work! ${it.message}")
                }
                val req = JsonObjectRequest(Request.Method.GET, url, null, respL, respEL)
                queue.add(req)
            }
        }
    }
}
