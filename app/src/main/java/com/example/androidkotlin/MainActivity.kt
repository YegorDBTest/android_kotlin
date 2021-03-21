package com.example.androidkotlin

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory
import kotlin.math.roundToInt


data class ItemData(var title: String, var description: String, var favorite: Boolean)


class ItemsAdapter(private var itemsData: List<ItemData>): RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemLayoutView: View =  LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, null)
        return ItemViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.title.text = itemsData[position].title
        holder.description.text = itemsData[position].description
        holder.favorite.isChecked = itemsData[position].favorite
        holder.favorite.setOnClickListener {
            itemsData[position].favorite = holder.favorite.isChecked
        }
    }

    override fun getItemCount(): Int = itemsData.size

    class ItemViewHolder(itemLayoutView: View): RecyclerView.ViewHolder(itemLayoutView) {
        val title: TextView = itemLayoutView.findViewById(R.id.itemTitle)
        val description: TextView = itemLayoutView.findViewById(R.id.itemDescription)
        val favorite: ToggleButton = itemLayoutView.findViewById(R.id.itemFavorite)
    }
}


class ItemsFragment(private val data: List<ItemData>) : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.items_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.itemsRecyclerView)
        val layoutManager: RecyclerView.LayoutManager  = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager;
        val adapter: RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> = ItemsAdapter(data)
        recyclerView.adapter = adapter;
    }
}


class ItemsFragmentsAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
    val fragments = listOf(
            FragmentItem("first", ItemsFragment(List(20) {
                ItemData((it * 10 + 1).toString(), (it * 100 + 1).toString(), it % 2 == 1)
            })),
            FragmentItem("second", ItemsFragment(List(20) {
                ItemData((it * 10).toString(), (it * 100).toString(), it % 2 == 0)
            })),
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position].fragment

    data class FragmentItem(val tabName: String, val fragment: Fragment)
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
        setSupportActionBar(findViewById(R.id.toolbar1))

        val viewPager: ViewPager2 = findViewById(R.id.pager)
        val adapter = ItemsFragmentsAdapter(this)
        viewPager.adapter = adapter
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.fragments[position].tabName
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
        }

        return super.onCreateOptionsMenu(menu)
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
