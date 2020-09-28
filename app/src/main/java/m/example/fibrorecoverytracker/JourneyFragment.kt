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
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_journey.*
import m.example.fibrorecoverytracker.databinding.FragmentJourneyBinding
import m.example.fibrorecoverytracker.listener.ScoreChangeListener
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors

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
    private var auth = Firebase.auth
    private val model: ScoreModel by activityViewModels()

    private var overallScoreMap = TreeMap<LocalDate, Score>()
    private var scoreMap = TreeMap<LocalDate, Score>()
    private var dates: ArrayList<LocalDate> = ArrayList()
    private var chartsList = mutableListOf<View>()
    private val initialScore = 0F

    private lateinit var sleepBarChart: BarChart
    private lateinit var overallProgressLineChart: LineChart
    private lateinit var avgProgress: TextView
    private lateinit var avgSleep: TextView
    private lateinit var avgExercise: TextView
    private lateinit var avgStress: TextView
    private lateinit var avgNutrition: TextView
    private lateinit var periodChips: ChipGroup
    private lateinit var binding: FragmentJourneyBinding
    private var uid: String? = null

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

        // Initialize all lateinit vars
        binding = FragmentJourneyBinding.bind(view)
        overallProgressLineChart = view.findViewById(R.id.overallProgressLineChart)
        sleepBarChart = view.findViewById(R.id.sleepBarChart)
        avgProgress = view.findViewById(R.id.avgProgress)
        avgSleep = view.findViewById(R.id.avgSleep)
        avgExercise = view.findViewById(R.id.avgExercise)
        avgStress = view.findViewById(R.id.avgStress)
        avgNutrition = view.findViewById(R.id.avgNutrition)
        periodChips = binding.periodChips

        initCharts()
        initPeriods()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        uid = auth.currentUser?.uid
        if (uid != null) {
            model.scoreMap.observe(
                viewLifecycleOwner,
                Observer<TreeMap<LocalDate, Score>> { scoreMap ->
                    run {
                        this.overallScoreMap = scoreMap
                        refreshScores()
                    }
                })
            refreshScores()
        }
    }

    private fun refreshScores() {
        filterForPeriod()
        dates.clear()
        dates.addAll(this.scoreMap.keys.toTypedArray())

        redrawCharts()
        calculateScoreReports()
    }

    private fun filterForPeriod() {
        val checkedChipId = binding.periodChips.checkedChipId
        if (checkedChipId == binding.chipOverall.id) {
            scoreMap = overallScoreMap
            return
        }
        val afterDate = when (checkedChipId) {
            binding.chip7d.id -> LocalDate.now().minusDays(7)
            binding.chip15d.id -> LocalDate.now().minusDays(15)
            binding.chip1m.id -> LocalDate.now().minusMonths(1)
            binding.chip2m.id -> LocalDate.now().minusMonths(2)
            else -> LocalDate.now().minusMonths(3)
        }

        scoreMap = TreeMap()
        for (entry in overallScoreMap) {
            if (entry.key.isAfter(afterDate)) {
                scoreMap[entry.key] = entry.value
            }
        }
    }

    private fun calculateScoreReports() {
        if (scoreMap.isEmpty()) {
            return
        }
        setScore(percentage(scoreMap.values.map { score -> score.sleepScore }, Sleep.LESS_THAN_7.min(), Sleep.LESS_THAN_7.max()), avgSleep)
        setScore(percentage(scoreMap.values.map { score -> score.exerciseScore }, Exercise.NONE.min(), Exercise.NONE.max()), avgExercise)
        setScore(percentage(scoreMap.values.map { score -> score.mentalStressScore }, MentalStress.HAPPY.min(), MentalStress.HAPPY.max()), avgStress)
        setScore(percentage(scoreMap.values.map { score -> score.nutritionScore }, Nutrition.BAD.min(), Nutrition.BAD.max()), avgNutrition)

        val avgProgressScore = average(scoreMap.values.map { score -> score.total })
        val color = when(avgProgressScore) {
            in Double.MIN_VALUE..Constants.POOR_PROGRESS -> "#FF0000"
            in Constants.POOR_PROGRESS..Constants.GOOD_PROGRESS -> "#E1AC00"
            else -> "#00FF00"
        }
        avgProgress.setTextColor(Color.parseColor(color))
        avgProgress.text = "${DECIMAL_FORMAT.format(avgProgressScore)}"
    }

    private fun setScore(score: Double, textView: TextView) {
        val color = when(score) {
            in 0.0..Constants.POOR_SCORE -> "#FF0000"
            in Constants.POOR_SCORE..Constants.GOOD_SCORE -> "#E1AC00"
            else -> "#00FF00"
        }
        textView.setTextColor(Color.parseColor(color))
        textView.text = "${DECIMAL_FORMAT.format(score)}%"
    }

    private fun percentage(scores: List<Int>, min: Int, max: Int): Double {
        val avg = scores.stream().collect(Collectors.averagingInt { num -> num.toInt() })
        val diff = 0-min
        return ((avg+diff)/(max+diff))*100
    }

    private fun average(scores: List<Int>): Double =
        scores.stream().collect(Collectors.averagingInt { num -> num.toInt() })

    private fun initCharts() {
        chartsList.add(overallProgressLineChart)
        chartsList.add(sleepBarChart)

        initLineChart()
        initSleepBarChart()

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels
        for (chart in chartsList) {
            chart.layoutParams.width = width - width / 8
        }
    }

    private fun initPeriods() {
        binding.chipOverall.isChecked = true
        binding.periodChips.setOnCheckedChangeListener { _, _ ->
            refreshScores()
        }
    }

    private fun initSleepBarChart() {
        sleepBarChart.setTouchEnabled(true)
        sleepBarChart.setPinchZoom(true)
        sleepBarChart.legend.isEnabled = false
        sleepBarChart.axisRight.setDrawLabels(false)

        var xAxis = sleepBarChart.xAxis
        xAxis.valueFormatter = DateValueFormatter(dates)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelRotationAngle = 315F

        var dataSet1 = BarDataSet(mutableListOf(), "")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet1)
        sleepBarChart.data = BarData(dataSets)
    }

    private fun initLineChart() {
        overallProgressLineChart.setTouchEnabled(true)
        overallProgressLineChart.setPinchZoom(true)
        overallProgressLineChart.legend.isEnabled = false
        overallProgressLineChart.axisRight.setDrawLabels(false)

        var xAxis = overallProgressLineChart.xAxis
        xAxis.valueFormatter = DateValueFormatter(dates);
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
                ContextCompat.getDrawable(context!!, android.R.color.holo_blue_light)
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
        sleepBarChart.xAxis.valueFormatter = DateValueFormatter(dates)
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
        overallProgressLineChart.xAxis.valueFormatter = DateValueFormatter(dates)
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
        private val DECIMAL_FORMAT = DecimalFormat("#.##")

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

    class DateValueFormatter(private val datesArray: ArrayList<LocalDate>) : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return Constants.DATE_FORMATTER.format(datesArray[value.toInt()])
        }
    }
}