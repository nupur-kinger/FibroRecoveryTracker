package m.example.fibrorecoverytracker

import Score
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_track.view.*
import m.example.fibrorecoverytracker.databinding.ActivityTrackBinding
import java.time.LocalDate
import java.time.LocalDateTime

class TrackActivity : AppCompatActivity() {
    private val model: TrackActivityModel by viewModels()

    companion object {
        const val EXTRA_SCORE: String = "m.example.fibrorecoverytracker.EXTRA_SCORE"
        const val EXTRA_DATE = "m.example.fibrorecoverytracker.EXTRA_DATE"
    }

    private lateinit var binding: ActivityTrackBinding
    private lateinit var date: LocalDate
    private lateinit var uid: String

    private var database: DatabaseReference = Firebase.database.reference
    private var shortAnimationDuration: Int = 0

    private lateinit var additionalScoreTextBox: EditText
    private lateinit var sleepBar: LabelledSeekBar<Sleep>
    private lateinit var exerciseBar: LabelledSeekBar<Exercise>
    private lateinit var nutritionBar: LabelledSeekBar<Nutrition>
    private lateinit var infectionBar: LabelledSeekBar<Infection>
    private lateinit var meditationBar: LabelledSeekBar<Meditation>
    private lateinit var overeatingBar: LabelledSeekBar<Overeating>
    private lateinit var mentalStressBar: LabelledSeekBar<MentalStress>
    private lateinit var physicalStressBar: LabelledSeekBar<PhysicalStress>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid") ?: ""

        sleepBar = findViewById(R.id.sleepBar)
        exerciseBar = findViewById(R.id.exerciseBar)
        nutritionBar = findViewById(R.id.nutritionBar)
        infectionBar = findViewById(R.id.infectionBar)
        meditationBar = findViewById(R.id.meditationBar)
        overeatingBar = findViewById(R.id.overeatingBar)
        mentalStressBar = findViewById(R.id.mentalStressBar)
        physicalStressBar = findViewById(R.id.physicalStressBar)

        sleepBar.setMetric(Sleep::class.java)
        exerciseBar.setMetric(Exercise::class.java)
        nutritionBar.setMetric(Nutrition::class.java)
        infectionBar.setMetric(Infection::class.java)
        meditationBar.setMetric(Meditation::class.java)
        overeatingBar.setMetric(Overeating::class.java)
        mentalStressBar.setMetric(MentalStress::class.java)
        physicalStressBar.setMetric(PhysicalStress::class.java)

        database = Firebase.database.reference
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        var dateString: String
        if (intent.hasExtra(EXTRA_DATE)) {
            // EDIT
            date = intent.getSerializableExtra(EXTRA_DATE) as LocalDate
            dateString = Constants.DATE_FORMATTER.format(date)
            setSelections(intent.getSerializableExtra(EXTRA_SCORE) as Score)
        } else {
            // TODAY
            date = LocalDate.now()
            dateString = Constants.DATE_FORMATTER.format(date)
            fetchForDate(date)
        }

        val dateText: EditText = findViewById(R.id.dateText)
        dateText.text = SpannableStringBuilder(dateString)
        dateText.setOnClickListener {
            val today = LocalDateTime.now()
            val dpd = DatePickerDialog(
                this@TrackActivity,
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    date = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    val formattedDate = Constants.DATE_FORMATTER.format(date)
                    it.dateText.text = SpannableStringBuilder(formattedDate)
                    fetchForDate(date)
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )

            dpd.show()
        }

        additionalScoreTextBox = findViewById(R.id.additionalScore)
    }

    fun save(view: View) {
        val sleepScore = sleepBar.score()
        val exerciseScore = exerciseBar.score()
        val nutritionScore = nutritionBar.score()
        val infectionScore = infectionBar.score()
        val meditationScore = meditationBar.score()
        val overeatingScore = overeatingBar.score()
        val mentalStressScore = mentalStressBar.score()
        val physicalStressScore = physicalStressBar.score()

        val saunaScore = if (binding.saunaChip.isChecked) 1 else 0
        val physiotherapyScore = if (binding.physioChip.isChecked) 1 else 0
        val massageScore = if (binding.massageChip.isChecked) 1 else 0
        val acupunctureScore = if (binding.accupunctureChip.isChecked) 1 else 0
        val hotBathScore = if (binding.hotBathChip.isChecked) 1 else 0
        val pranayamaScore = if (binding.pranayamaChip.isChecked) 1 else 0

        val additionalScoreString = additionalScoreTextBox.text.toString()
        val additionalScore = if (additionalScoreString == "") 0 else additionalScoreString.toInt()
        val essentialsScore =
            sleepScore + exerciseScore + nutritionScore + infectionScore + meditationScore + overeatingScore + mentalStressScore + physicalStressScore
        val extrasScore =
            saunaScore + physiotherapyScore + massageScore + acupunctureScore + hotBathScore + pranayamaScore
        val total: Int = essentialsScore + extrasScore + additionalScore

        val notes = findViewById<EditText>(R.id.notes).text.toString()

        var score = Score(
            sleepScore,
            exerciseScore,
            nutritionScore,
            infectionScore,
            meditationScore,
            overeatingScore,
            mentalStressScore,
            physicalStressScore,
            saunaScore,
            physiotherapyScore,
            pranayamaScore,
            acupunctureScore,
            massageScore,
            hotBathScore,
            additionalScore,
            notes,
            total
        )

        model.saveScore(uid, date, score)
//        database.child("${Constants.DATE_FORMATTER.format(date)}").setValue(score)
        Toast.makeText(applicationContext, "Progress saved", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun fetchForDate(date: LocalDate) {
        model.fetchScore(uid, date, object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val score: Score? = dataSnapshot.getValue(Score::class.java)
                if (score == null) {
                    setDefaultSelections()
                } else {
                    setSelections(score)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // TODO
            }
        })
    }

    private fun setDefaultSelections(): Unit {
//        findViewById<RadioButton>(R.id.defaultSleep).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultExercise).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultNutrition).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultInfection).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultMeditation).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultOvereating).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultMentalStress).isChecked = true;
//        findViewById<RadioButton>(R.id.defaultPhysicalStress).isChecked = true;
    }

    private fun setSelections(score: Score) {
        sleepBar.setProgress(score.sleepScore)
        exerciseBar.setProgress(score.exerciseScore)
        nutritionBar.setProgress(score.nutritionScore)
        infectionBar.setProgress(score.infectionScore)
        meditationBar.setProgress(score.meditationScore)
        overeatingBar.setProgress(score.overeatingScore)
        mentalStressBar.setProgress(score.mentalStressScore)
        physicalStressBar.setProgress(score.physicalStressScore)

        if (score.sauna == 1) binding.saunaChip.isChecked = true
        if (score.physiotherapy == 1) binding.physioChip.isChecked = true
        if (score.massage == 1) binding.massageChip.isChecked = true
        if (score.pranayama == 1) binding.pranayamaChip.isChecked = true
        if (score.accupuncture == 1) binding.accupunctureChip.isChecked = true
        if (score.hotBath == 1) binding.hotBathChip.isChecked = true

        if (score.notes.isNotEmpty()) findViewById<EditText>(R.id.notes).setText(score.notes)
        findViewById<EditText>(R.id.additionalScore).setText(score.additional.toString())
    }
}