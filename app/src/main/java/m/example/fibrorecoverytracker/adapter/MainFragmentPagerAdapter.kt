package m.example.fibrorecoverytracker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import m.example.fibrorecoverytracker.CalendarFragment
import m.example.fibrorecoverytracker.JourneyFragment
import m.example.fibrorecoverytracker.ScoreModel
import m.example.fibrorecoverytracker.TestFragment

class MainFragmentPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle, private val model: ScoreModel) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val journeyFragment = JourneyFragment()
        val calendarFragment = CalendarFragment()
        return if (position == 0) journeyFragment else calendarFragment
    }
}