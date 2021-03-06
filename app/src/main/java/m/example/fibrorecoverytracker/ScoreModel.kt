package m.example.fibrorecoverytracker

import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import m.example.fibrorecoverytracker.listener.ScoreChangeListener
import Score
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.util.*

class ScoreModel : ViewModel() {
    val scoreMap: MutableLiveData<TreeMap<LocalDate, Score>> by lazy {
        MutableLiveData<TreeMap<LocalDate, Score>>()
    }

    private var database: DatabaseReference = Firebase.database.reference.child("users")
    private val listeners = mutableListOf<ScoreChangeListener>()
    private var dates: ArrayList<LocalDate> = ArrayList()

    fun getScores(userId: String, callback: ()->Unit) {
        val userDataRef = database.child(userId)

        val progressListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    val scores = TreeMap<LocalDate, Score>()
                    val t: GenericTypeIndicator<Map<String, Score>> =
                        object : GenericTypeIndicator<Map<String, Score>>() {}
                    dataSnapshot.getValue(t)?.forEach {
                        var date = LocalDate.parse(it.key, Constants.DATE_FORMATTER)
                        if (it.value is Score) {
                            scores[date] = it.value
                        }
                    }

                    dates.clear()
                    dates.addAll(scores.keys.toTypedArray())

                    scoreMap.value = scores
                    notifyScoreChange()
                    callback()
                } else {
                    userDataRef.child("${Constants.DATE_FORMATTER.format(LocalDate.now())}").setValue(null)
                }
            }
        }

        userDataRef.addValueEventListener(progressListener)
    }

    private fun notifyScoreChange() {
        listeners.forEach { it.onScoreChange(scoreMap.value ?: TreeMap()) }
    }
}