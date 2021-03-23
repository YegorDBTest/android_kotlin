package com.example.androidkotlin

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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


data class ItemData(var title: String, var description: String, var favorite: Boolean)


class ItemsManager {
    private var _items = MutableList(50) {
        ItemData(it.toString(), (it + 100).toString(), it % 2 == 1)
    }
    private var itemsAdapters = mutableListOf<MainItemsAdapter>()

    fun getMainItems(): MutableList<ItemData> {
        return _items
    }

    fun getFavoriteItems(): List<ItemData> {
        return _items.filter { it.favorite }
    }

    fun addAdapter(adapter: MainItemsAdapter) {
        itemsAdapters.add(adapter)
    }

    fun refreshAdapters() {
        for (adapter in itemsAdapters) {
            adapter.notifyDataSetChanged()
        }
    }
}


open class MainItemsAdapter: RecyclerView.Adapter<MainItemsAdapter.ItemViewHolder>() {

    init {
        addToItemsHolder()
    }

    private fun addToItemsHolder() {
        MainActivity.itemsManager.addAdapter(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemLayoutView: View =  LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, null)
        return ItemViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItems()[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.favorite.isChecked = item.favorite
        holder.favorite.setOnClickListener {
            item.favorite = holder.favorite.isChecked
            MainActivity.itemsManager.refreshAdapters()
        }
    }

    override fun getItemCount(): Int = getItems().size

    open fun getItems(): List<ItemData> = MainActivity.itemsManager.getMainItems()

    class ItemViewHolder(itemLayoutView: View): RecyclerView.ViewHolder(itemLayoutView) {
        val title: TextView = itemLayoutView.findViewById(R.id.itemTitle)
        val description: TextView = itemLayoutView.findViewById(R.id.itemDescription)
        val favorite: ToggleButton = itemLayoutView.findViewById(R.id.itemFavorite)
    }
}


class FavoriteItemsAdapter: MainItemsAdapter() {
    override fun getItems(): List<ItemData> = MainActivity.itemsManager.getFavoriteItems()
}


open class ItemsFragment(private val adapter: MainItemsAdapter) : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.items_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.itemsRecyclerView)
        val layoutManager: RecyclerView.LayoutManager  = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager;
        recyclerView.adapter = adapter;
    }
}


class ItemsFragmentsAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
    var fragments = listOf(
        FragmentItem("first", ItemsFragment(MainItemsAdapter())),
        FragmentItem("second", ItemsFragment(FavoriteItemsAdapter())),
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position].fragment

    data class FragmentItem(val tabName: String, val fragment: Fragment)
}


class MainActivity : AppCompatActivity() {

    companion object {
        val itemsManager = ItemsManager()
    }

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
