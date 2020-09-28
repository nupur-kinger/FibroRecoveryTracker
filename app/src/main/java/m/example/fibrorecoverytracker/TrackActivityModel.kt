package m.example.fibrorecoverytracker

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import java.time.LocalDate
import Score
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TrackActivityModel : ViewModel() {
    private var database: DatabaseReference = Firebase.database.reference.child("users")

    fun fetchScore(uid: String, date: LocalDate, listener: ValueEventListener) {
        database.child(uid).child(formatDate(date)).addListenerForSingleValueEvent(listener)
    }

    fun saveScore(uid: String, date: LocalDate, score: Score) {
        database.child(uid).child(formatDate(date)).setValue(score)
    }

    private fun formatDate(date: LocalDate): String = "${Constants.DATE_FORMATTER.format(date)}"
}