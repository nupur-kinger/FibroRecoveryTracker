package m.example.fibrorecoverytracker

import Score
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import m.example.fibrorecoverytracker.listener.ScoreChangeListener
import java.time.LocalDate
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JourneyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JourneyFragment : Fragment() {
    private val model: ScoreModel by activityViewModels()

    private var scoreMap = TreeMap<LocalDate, Score>()
    private var dates: ArrayList<LocalDate> = ArrayList()
    private var chartsList = mutableListOf<View>()
    private val initialScore = 0F

    private lateinit var sleepBarChart: BarChart
    private lateinit var overallProgressLineChart: LineChart

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_journey, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCharts(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model.scoreMap.observe(viewLifecycleOwner, Observer<TreeMap<LocalDate, Score>> { scoreMap ->
            run {
                this.scoreMap = scoreMap
                dates.clear()
                dates.addAll(scoreMap.keys.toTypedArray())
                redrawCharts()
            }
        })
        redrawCharts()
    }

    private fun initCharts(rootView: View) {
        overallProgressLineChart = rootView.findViewById(R.id.overallProgressLineChart)
        sleepBarChart = rootView.findViewById(R.id.sleepBarChart)

        chartsList.add(overallProgressLineChart)
        chartsList.add(sleepBarChart)

        initLineChart()
        initSleepBarChart()

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels
        for (chart in chartsList) {
            chart.layoutParams.width = width - width/8
        }
    }

    private fun initSleepBarChart() {
        class DateValueFormatter : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return Constants.DATE_FORMATTER.format(dates[value.toInt()])
            }
        }

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

        overallProgressLineChart.setTouchEnabled(true)
        overallProgressLineChart.setPinchZoom(true)
        overallProgressLineChart.legend.isEnabled = false
        overallProgressLineChart.axisRight.setDrawLabels(false)

        var xAxis = overallProgressLineChart.xAxis
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
                ContextCompat.getDrawable(context!!, android.R.color.holo_purple)
            set1.fillDrawable = drawable
        } else {
            set1.fillColor = Color.DKGRAY
        }
        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)
        val data = LineData(dataSets)
        overallProgressLineChart.data = data
    }

    private fun redrawCharts() {
        redrawSleepBarChart()
        redrawLineChart()
    }

    private fun redrawSleepBarChart() {
        val values: ArrayList<BarEntry> = ArrayList()
        var index = 0

        for (entry in scoreMap) {
            values.add(BarEntry(index.toFloat(), entry.value.sleepScore.toFloat()))
            index++
        }

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

        val dataSet = overallProgressLineChart.data.getDataSetByIndex(0) as LineDataSet
        dataSet.values = values
        overallProgressLineChart.data.notifyDataChanged()
        overallProgressLineChart.notifyDataSetChanged()
        overallProgressLineChart.invalidate()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JourneyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JourneyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}