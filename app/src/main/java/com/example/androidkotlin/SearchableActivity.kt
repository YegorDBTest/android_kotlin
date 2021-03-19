package com.example.androidkotlin

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


class SearchableActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        supportActionBar?.hide()

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
                Log.d("TEST_TAG", "query $query")
            }
        }
    }
}
