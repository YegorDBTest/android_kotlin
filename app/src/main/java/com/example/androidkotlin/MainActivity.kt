package com.example.androidkotlin

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
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
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory


data class ItemData(var title: String, var description: String, var favorite: Boolean) {
    fun toJson(): JSONObject {
        val jsn = JSONObject()
        jsn.put("title", this.title)
        jsn.put("description", this.description)
        jsn.put("favorite", this.favorite)
        return jsn
    }
}


class AdaptersManager {

    private var itemsAdapters = mutableListOf<BaseItemsAdapter>()

    fun add(adapter: BaseItemsAdapter) {
        itemsAdapters.add(adapter)
    }

    fun refresh() {
        for (adapter in itemsAdapters) {
            adapter.notifyDataSetChanged()
        }
    }
}


class ItemsManager {

    private var items =  mutableMapOf<String, ItemData>()

    fun initialize(itemsJsonString: String?) {
        if (itemsJsonString == null) {
            createItems()
        } else {
            try {
                createItemsFromJsonString(itemsJsonString)
            } catch (e: Exception) {
                Log.e("TEST_TAG", "Fail ($e) to get items data from json $itemsJsonString")
                createItems()
            }
        }
    }

    private fun createItems() {
        items = mutableMapOf()
        for (i in 0..50) {
            items[i.toString()] = ItemData(i.toString(), (i + 100).toString(), false)
        }
    }

    private fun createItemsFromJsonString(itemsJsonString: String) {
        val jsn = JSONObject(itemsJsonString)
        for (key in jsn.keys()) {
            val d = jsn.getJSONObject(key)
            items[key] = ItemData(
                d.getString("title"),
                d.getString("description"),
                d.getBoolean("favorite"),
            )
        }
    }

    fun isFavoriteItem(key: String): Boolean {
        val item = items[key]
        return item != null && item.favorite
    }

    fun getMainItems(): List<ItemData> {
        return items.values.toList()
    }

    fun getFavoriteItems(): List<ItemData> {
        return items.filterValues { it.favorite }.values.toList()
    }

    fun toJsonString() : String {
        val jsn = JSONObject()
        for (e in items.entries) {
            jsn.put(e.key, e.value.toJson())
        }
        return jsn.toString()
    }
}


abstract class BaseItemsAdapter: RecyclerView.Adapter<BaseItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemLayoutView: View =  LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, null)
        return ItemViewHolder(itemLayoutView)
    }

    class ItemViewHolder(itemLayoutView: View): RecyclerView.ViewHolder(itemLayoutView) {
        val title: TextView = itemLayoutView.findViewById(R.id.itemTitle)
        val description: TextView = itemLayoutView.findViewById(R.id.itemDescription)
        val favorite: ToggleButton = itemLayoutView.findViewById(R.id.itemFavorite)
    }
}


open class MainItemsAdapter: BaseItemsAdapter() {

    init {
        addToItemsHolder()
    }

    private fun addToItemsHolder() {
        MainActivity.adaptersManager.add(this)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItems()[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.favorite.isChecked = item.favorite
        holder.favorite.setOnClickListener {
            item.favorite = holder.favorite.isChecked
            MainActivity.adaptersManager.refresh()
        }
    }

    override fun getItemCount(): Int = getItems().size

    open fun getItems(): List<ItemData> = MainActivity.itemsManager.getMainItems()
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
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
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
        val adaptersManager = AdaptersManager()
    }

    private var favDataStore: SharedPreferences? = null
//    private lateinit var webSocketClient: WebSocketClient
//
//    private fun sendMessage() {
//        webSocketClient.send("lol")
//    }
//
//    private fun initWebSocket() {
//        val uri = URI("wss://echo.websocket.org/")
//        webSocketClient = object : WebSocketClient(uri) {
//            override fun onOpen(handshakedata: ServerHandshake?) {
//                Log.d("TEST_TAG", "onOpen")
//                sendMessage()
//            }
//
//            override fun onMessage(message: String?) {
//                Log.d("TEST_TAG", "onMessage: $message")
//            }
//
//            override fun onClose(code: Int, reason: String?, remote: Boolean) {
//                Log.d("TEST_TAG", "onClose, code: $code, reason $reason")
//            }
//
//            override fun onError(ex: Exception?) {
//                Log.e("TEST_TAG", "onError: ${ex?.message}")
//            }
//        }
//        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
//        webSocketClient.setSocketFactory(socketFactory)
//        webSocketClient.connect()
//    }

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

        val itemsDataStore = getSharedPreferences("data", Context.MODE_PRIVATE)
        val itemsJsonString = itemsDataStore?.getString("items", null)
        itemsManager.initialize(itemsJsonString)
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

    override fun onStop() {
        val itemsDataStore = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = itemsDataStore?.edit();
        editor?.putString("items", itemsManager.toJsonString());
        editor?.apply();
        super.onStop()
    }

//    override fun onResume() {
//        initWebSocket()
//    }
//
//    override fun onPause() {
//        webSocketClient.close()
//    }
}
