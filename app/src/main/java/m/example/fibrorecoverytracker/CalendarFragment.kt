package m.example.fibrorecoverytracker

import Score
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import m.example.fibrorecoverytracker.listener.ScoreChangeListener
import sun.bob.mcalendarview.MCalendarView
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.vo.DateData
import java.time.LocalDate
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    private val model: ScoreModel by activityViewModels()

    private var scoreMap = TreeMap<LocalDate, Score>()
    private lateinit var calendar: MCalendarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalendar(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model.scoreMap.observe(viewLifecycleOwner, Observer<TreeMap<LocalDate, Score>> { scoreMap ->
            run {
                this.scoreMap = scoreMap
                updateCalendarView()
            }
        })
        updateCalendarView()
    }

    private fun initCalendar(rootView: View) {
        calendar = rootView.findViewById(R.id.calendar)
        calendar.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(view: View?, date: DateData) {
                onCalDateClick(LocalDate.of(date.year, date.month, date.day), rootView)
            }
        })
    }

    private fun updateCalendarView() {
        if (::calendar.isInitialized) {
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
                    calendar.markDate(
                        dateData.setMarkStyle(
                            MarkStyle(
                                MarkStyle.BACKGROUND,
                                Color.RED
                            )
                        )
                    )
                }
            }
        }
    }

    private fun onCalDateClick(date: LocalDate, view: View) {
        if (scoreMap.containsKey(date)) {
            val intent = Intent(view.context, TrackActivity::class.java).apply {
                putExtra(TrackActivity.EXTRA_DATE, date)
                putExtra(TrackActivity.EXTRA_SCORE, scoreMap[date])
            }
            startActivity(intent)
        } else {
            Toast.makeText(activity, "Oops! Something went wrong", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CalendarFragment().apply {
                arguments = Bundle()
            }
    }
}