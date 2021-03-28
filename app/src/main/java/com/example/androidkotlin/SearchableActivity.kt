package com.example.androidkotlin

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class SearchItemsAdapter: BaseItemsAdapter() {

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = SearchableActivity.items[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.favorite.isChecked = item.favorite
        holder.favorite.setOnClickListener {
            var changed = false
            for (i in MainActivity.itemsManager.getMainItems().filter { it.title == item.title }) {
                i.favorite = holder.favorite.isChecked
                changed = true
            }
            if (changed) {
                MainActivity.itemsManager.refreshAdapters()
            }
        }
    }

    override fun getItemCount(): Int = SearchableActivity.items.size
}


class SearchableActivity : AppCompatActivity() {

    private lateinit var adapter: SearchItemsAdapter

    companion object {
        var items = mutableListOf<ItemData>()
    }

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        supportActionBar?.hide()

        queue = Volley.newRequestQueue(this)

        handleIntent(intent)

        val recyclerView: RecyclerView = findViewById(R.id.itemsRecyclerView)
        val layoutManager: RecyclerView.LayoutManager  = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = SearchItemsAdapter()
        recyclerView.adapter = adapter
    }

    override fun onNewIntent(intent: Intent) {
        val result = super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        return result
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->

                val favorite = MainActivity.itemsManager.getMainItems().any { it.title == query && it.favorite }
                items = MutableList(50) {
                    ItemData(query, "10$query", favorite)
                }
                adapter.notifyDataSetChanged()

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
