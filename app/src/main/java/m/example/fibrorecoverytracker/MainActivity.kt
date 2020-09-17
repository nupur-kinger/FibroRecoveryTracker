package m.example.fibrorecoverytracker

import Score
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import m.example.fibrorecoverytracker.adapter.MainFragmentPagerAdapter
import m.example.fibrorecoverytracker.databinding.ActivityMainBinding
import m.example.fibrorecoverytracker.listener.ScoreChangeListener
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.vo.DateData
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), ScoreChangeListener {
    private val model: ScoreModel by viewModels()

    private var database: DatabaseReference = Firebase.database.reference
    private var scoreMap = TreeMap<LocalDate, Score>()
    private var dates: ArrayList<LocalDate> = ArrayList()
    private val initialScore = 0F

    private lateinit var calendar: sun.bob.mcalendarview.MCalendarView
    private var chartsList = mutableListOf<View>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = MainFragmentPagerAdapter(supportFragmentManager, lifecycle, model)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Journey" else "Calendar"
//            tab.text = "OBJECT ${(position + 1)}"
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

//        model.addListener(this)
        model.getScores()

//        initCharts()
//
//        calendar = findViewById(R.id.calendar)
//        calendar.setOnDateClickListener(object : OnDateClickListener() {
//            override fun onDateClick(view: View?, date: DateData) {
//                onDateClick(LocalDate.of(date.year, date.month, date.day))
//            }
//        })
//
//        getAllDates()
    }

    private fun initCharts() {
        chartsList.add(findViewById(R.id.lineChart))
        chartsList.add(findViewById(R.id.sleepBarChart))

        initLineChart()
        initSleepBarChart()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels
        for (chart in chartsList) {
            chart.layoutParams.width = width - width/6
        }
    }

    private fun initSleepBarChart() {
        class DateValueFormatter : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return Constants.DATE_FORMATTER.format(dates[value.toInt()])
            }
        }

        var sleepBarChart = findViewById<BarChart>(R.id.sleepBarChart)
        sleepBarChart.setTouchEnabled(true)
        sleepBarChart.setPinchZoom(true)
        sleepBarChart.legend.isEnabled = false
        sleepBarChart.axisRight.setDrawLabels(false)

        var xAxis = sleepBarChart.xAxis
        xAxis.valueFormatter = DateValueFormatter();
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelRotationAngle = 315F

        var dataSet1 = BarDataSet(mutableListOf(), "")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet1)
        sleepBarChart.data = BarData(dataSets)
    }

    private fun initLineChart() {
        class DateValueFormatter : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return Constants.DATE_FORMATTER.format(dates[value.toInt()])
            }
        }

        var lineChart = findViewById<LineChart>(R.id.lineChart)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.legend.isEnabled = false
        lineChart.axisRight.setDrawLabels(false)

        var xAxis = lineChart.xAxis
        xAxis.valueFormatter = DateValueFormatter();
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelRotationAngle = 315F

        val set1 = LineDataSet(ArrayList<Entry>(), "")
        set1.setDrawIcons(false)
        set1.enableDashedLine(10f, 5f, 0f)
        set1.enableDashedHighlightLine(10f, 5f, 0f)
        set1.setCircleColor(Color.DKGRAY)
        set1.lineWidth = 1f
        set1.circleRadius = 3f
        set1.setDrawCircleHole(false)
        set1.valueTextSize = 9f
        set1.setDrawFilled(true)
//        set1.formLineWidth = 1f
//        set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
//        set1.formSize = 15f
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
//        redrawSleepBarChart()
//        redrawLineChart()
    }

    private fun redrawSleepBarChart() {
        val values: ArrayList<BarEntry> = ArrayList()
        var index = 0

        for (entry in scoreMap) {
            values.add(BarEntry(index.toFloat(), entry.value.sleepScore.toFloat()))
            index++
        }

        var sleepBarChart = findViewById<BarChart>(R.id.sleepBarChart)
        val dataSet = sleepBarChart.data.getDataSetByIndex(0) as BarDataSet
        dataSet.values = values
        sleepBarChart.data.notifyDataChanged()
        sleepBarChart.notifyDataSetChanged()
        sleepBarChart.invalidate()
    }

    private fun redrawLineChart() {
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

    private fun updateCalendarView() {
        for (entry in scoreMap) {
            val date = entry.key
            val score = entry.value.total
            var dateData = DateData(date.year, date.monthValue, date.dayOfMonth)

            var color =
                when {
                    score < 0 -> Color.RED
                    score == 0 -> Color.WHITE
                    else -> Color.GREEN
                }
            calendar.markDate(dateData.setMarkStyle(MarkStyle(MarkStyle.DOT, color)))
            if (score >= 10) {
                calendar.markDate(
                    dateData.setMarkStyle(
                        MarkStyle(
                            MarkStyle.BACKGROUND,
                            Color.GREEN
                        )
                    )
                )
            } else if (score <= -5) {
                calendar.markDate(dateData.setMarkStyle(MarkStyle(MarkStyle.BACKGROUND, Color.RED)))
            }
        }
    }

    fun track(view: View) {
        val intent = Intent(this, TrackActivity::class.java).apply {
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

                updateCalendarView()
                redrawCharts()
            }
        }

        database.addValueEventListener(progressListener)
    }

    override fun onScoreChange(scoreMap: TreeMap<LocalDate, Score>) {
        this.scoreMap = scoreMap
        redrawCharts()
    }

    private fun onDateClick(date: LocalDate) {
        if (scoreMap.containsKey(date)) {
            val intent = Intent(this, TrackActivity::class.java).apply {
                putExtra(TrackActivity.EXTRA_DATE, date)
                putExtra(TrackActivity.EXTRA_SCORE, scoreMap[date])
            }
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Oops! Something went wrong", Toast.LENGTH_LONG)
        }
    }
}