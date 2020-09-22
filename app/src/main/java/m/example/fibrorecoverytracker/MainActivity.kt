package m.example.fibrorecoverytracker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import m.example.fibrorecoverytracker.adapter.MainFragmentPagerAdapter
import m.example.fibrorecoverytracker.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private val model: ScoreModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = MainFragmentPagerAdapter(supportFragmentManager, lifecycle, model)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Journey" else "Calendar"
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                //load fragment or do whatever you want
//                val fragment: Fragment = if (tab.position == 1) JourneyFragment() else CalendarFragment()
//                supportFragmentManager.beginTransaction().replace(R.id.tabContainer, fragment).commit()
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab) {}
//            override fun onTabReselected(tab: TabLayout.Tab) {}
//        })

        model.getScores()
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
}