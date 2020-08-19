package m.example.fibrorecoverytracker

import Score
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var database: DatabaseReference = Firebase.database.reference
    private var scoreMap = TreeMap<LocalDate, Score>(Collections.reverseOrder())
    private var dates: ArrayList<LocalDate> = ArrayList()
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val recyclerView = findViewById<RecyclerView>(R.id.score_recycler_view)
        adapter = MyAdapter(dates, ::onDateClick)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        getAllDates()
    }

    fun track(view: View) {
        val intent = Intent(this, TrackActivity::class.java).apply {
//            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getAllDates() {
        val progressListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<Map<String, Score>> =
                    object : GenericTypeIndicator<Map<String, Score>>() {}
                dataSnapshot.getValue(t)?.forEach {
//                    println(it.key)
                    var date = LocalDate.parse(it.key, Constants.DATE_FORMATTER)
                    var score = it.value as Score
                    scoreMap[date] = score
                }

//                var data = dataSnapshot.value as HashMap<String, Score>
//                for (k in data.keys) {
//                    scoreMap.put(k, data[k] as Score)
//                }
//                scoreMap = dataSnapshot.value as HashMap<String, Score>
                dates.clear()
                dates.addAll(scoreMap.keys.toTypedArray())

                adapter.notifyDataSetChanged()
            }
        }

        database.addValueEventListener(progressListener)
    }

    private fun onDateClick(date: LocalDate) {
        if (scoreMap.containsKey(date)) {
            var score: Score = scoreMap[date]!!
            val intent = Intent(this, TrackActivity::class.java).apply {
                putExtra(TrackActivity.EXTRA_DATE, date)
                putExtra(TrackActivity.EXTRA_SCORE, scoreMap.get(date))
            }
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Oops! Something went wrong", Toast.LENGTH_LONG)
        }
    }

    class MyAdapter(
        private val myDataset: List<LocalDate>,
        private val listener: (LocalDate) -> Unit
    ) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.date_list_item, parent, false) as View
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val date = myDataset[position]
            val dateTextHolder = holder.view.findViewById<TextView>(R.id.dateText)
            dateTextHolder.text = Constants.DATE_FORMATTER.format(date)
            dateTextHolder.setOnClickListener { listener(date) }
        }

        override fun getItemCount() = myDataset.size
    }
}