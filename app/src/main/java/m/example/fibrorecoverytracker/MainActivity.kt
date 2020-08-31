package m.example.fibrorecoverytracker

import Score
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.*
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var database: DatabaseReference = Firebase.database.reference
    private var scoreMap = TreeMap<LocalDate, Score>()
    private var dates: ArrayList<LocalDate> = ArrayList()
    private lateinit var adapter: MyAdapter
    private val initialScore = -1000f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        initCharts()
        adapter = MyAdapter(dates, ::onDateClick)
        val recyclerView = findViewById<RecyclerView>(R.id.score_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        getAllDates()
    }

    private fun initCharts() {
        class DateValueFormatter : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return Constants.DATE_FORMATTER.format(dates[value.toInt()])
            }
        }

        var lineChart = findViewById<LineChart>(R.id.lineChart)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.legend.isEnabled = false

        var xAxis = lineChart.xAxis
        xAxis.valueFormatter = DateValueFormatter();
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelRotationAngle = 315F

        val set1 = LineDataSet(ArrayList<Entry>(), "")
        set1.setDrawIcons(false)
        set1.enableDashedLine(10f, 5f, 0f)
        set1.enableDashedHighlightLine(10f, 5f, 0f)
//        set1.color = Color.DKGRAY
        set1.setCircleColor(Color.DKGRAY)
        set1.lineWidth = 1f
        set1.circleRadius = 3f
        set1.setDrawCircleHole(false)
        set1.valueTextSize = 9f
        set1.setDrawFilled(true)
        set1.formLineWidth = 1f
        set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        set1.formSize = 15f
//        set1.fillAlpha = 100
//        set1.fillColor = android.R.color.holo_purple
        if (Utils.getSDKInt() >= 18) {
            val drawable =
                ContextCompat.getDrawable(this, android.R.color.holo_purple)
            set1.fillDrawable = drawable
        } else {
            set1.fillColor = Color.DKGRAY
        }
        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)
        val data = LineData(dataSets)
        lineChart.data = data
    }

    private fun redrawCharts() {
        val values: ArrayList<Entry> = ArrayList()
        var score = initialScore
        var index = 0

        for (entry in scoreMap) {
            score += entry.value.total.toFloat()
            values.add(Entry(index.toFloat(), score))
            index++
        }

        var lineChart = findViewById<LineChart>(R.id.lineChart)
        val dataSet = lineChart.data.getDataSetByIndex(0) as LineDataSet
        dataSet.values = values
        lineChart.data.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
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
                    var date = LocalDate.parse(it.key, Constants.DATE_FORMATTER)
                    if (it.value is Score) {
                        scoreMap[date] = it.value
                    }
                }

                dates.clear()
                dates.addAll(scoreMap.keys.toTypedArray())

                adapter.notifyDataSetChanged()
                redrawCharts()
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