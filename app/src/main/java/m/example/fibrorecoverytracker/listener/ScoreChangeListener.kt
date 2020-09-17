package m.example.fibrorecoverytracker.listener

import Score
import java.time.LocalDate
import java.util.*

interface ScoreChangeListener {
    fun onScoreChange(score: TreeMap<LocalDate, Score>)
}