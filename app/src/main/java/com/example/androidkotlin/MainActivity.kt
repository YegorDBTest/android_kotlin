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


fun dpToPx(dp: Int, context: Context): Int {
    val density: Float = context.getResources().getDisplayMetrics().density
    return (dp.toFloat() * density).roundToInt()
}


class MyAdapter(private val numbers: List<Int>): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemLayoutView: View =  LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, null)
        return MyViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemTitle.text = numbers[position].toString()
    }

    override fun getItemCount(): Int = numbers.size

    class MyViewHolder(itemLayoutView: View): RecyclerView.ViewHolder(itemLayoutView) {
        val itemTitle: TextView = itemLayoutView.findViewById(R.id.item_title)
    }
}


class MainFragmentFirst : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_fragment_first, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val wrapper: LinearLayout = view.findViewById(R.id.linearLayout1)

        val rl = RelativeLayout(view.context)
        val rlParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
        )
        rl.layoutParams = rlParams
        rl.setPadding(dpToPx(15, view.context))

        val tv = TextView(view.context)
        val tvParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        tv.layoutParams = tvParams
        tv.setPadding(dpToPx(10, view.context))
        tv.text = "Lol"

        rl.addView(tv)
        wrapper.addView(rl)
    }
}


class MainFragmentSecond : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_fragment_second, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view2)
        val layoutManager: RecyclerView.LayoutManager  = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager;
        val adapter: RecyclerView.Adapter<MyAdapter.MyViewHolder> = MyAdapter((50..70).toList())
        recyclerView.adapter = adapter;
    }
}


class MainFragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    val fragments = listOf(
            FragmentItem("first", MainFragmentFirst()),
            FragmentItem("second", MainFragmentSecond()),
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
        val adapter = MainFragmentAdapter(this)
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
