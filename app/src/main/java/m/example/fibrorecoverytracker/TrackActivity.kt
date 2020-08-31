package m.example.fibrorecoverytracker

import Score
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_track.view.*
import java.time.LocalDate
import java.time.LocalDateTime

class TrackActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCORE: String = "m.example.fibrorecoverytracker.EXTRA_SCORE"
        const val EXTRA_DATE = "m.example.fibrorecoverytracker.EXTRA_DATE"
    }

    private var database: DatabaseReference = Firebase.database.reference
    private lateinit var date: LocalDate
    private lateinit var extraContent: View
    private lateinit var addExtraText: View
    private var shortAnimationDuration: Int = 0
    private var extrasVisible = false

    private lateinit var sleepText: TextView
    private lateinit var additionalScoreTextBox: EditText
    private lateinit var sleepSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        sleepText = findViewById(R.id.sleepText)
        sleepSeekBar = findViewById(R.id.sleepSeekBar)

        database = Firebase.database.reference
        createConstants()
        extraContent = findViewById<View>(R.id.extraContent)
        addExtraText = findViewById<View>(R.id.addExtraText)
        extraContent.visibility = View.GONE
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        var dateString = ""
        if (intent.hasExtra(EXTRA_DATE)) {
            // EDIT
            date = intent.getSerializableExtra(EXTRA_DATE) as LocalDate
            dateString = Constants.DATE_FORMATTER.format(date)
            setSelections(intent.getSerializableExtra(EXTRA_SCORE) as Score)
        } else {
            // TODAY
            date = LocalDate.now()
            dateString = Constants.DATE_FORMATTER.format(date)
            fetchForDate(dateString)
        }

        val dateText: EditText = findViewById(R.id.dateText)
        dateText.text = SpannableStringBuilder(dateString)
        dateText.setOnClickListener {
            val today = LocalDateTime.now()
            val dpd = DatePickerDialog(
                this@TrackActivity,
                DatePickerDialog.OnDateSetListener { dateText, year, monthOfYear, dayOfMonth ->
                    date = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    val formattedDate = Constants.DATE_FORMATTER.format(date)
                    it.dateText.text = SpannableStringBuilder(formattedDate)
                    fetchForDate(formattedDate)
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )

            dpd.show()
        }

        additionalScoreTextBox = findViewById(R.id.additionalScore)
    }

    fun toggleExtras(view: View) {
        if (extrasVisible) {
            collapseExtras()
        } else {
            expandExtras()
        }
        extrasVisible = !extrasVisible
    }

    fun expandExtras() {
        extraContent.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    fun collapseExtras() {
        extraContent.apply {
            alpha = 1f
            visibility = View.GONE
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    fun save(view: View) {
        val sleepScore = sleepScore(selectedText(R.id.sleep))
        val exerciseScore = exerciseScore(selectedText(R.id.exercise))
        val nutritionScore = nutritionScore(selectedText(R.id.nutrition))
        val infectionScore = infectionScore(selectedText(R.id.infection))
        val meditationScore = meditationScore(selectedText(R.id.meditation))
        val overeatingScore = overeatingScore(selectedText(R.id.overeating))
        val mentalStressScore = mentalStressScore(selectedText(R.id.mentalStress))
        val physicalStressScore = physicalStressScore(selectedText(R.id.physicalStress))
        val saunaScore = if (findViewById<CheckBox>(R.id.sauna).isChecked) 1 else 0
        val physiotherapyScore = if (findViewById<CheckBox>(R.id.physio).isChecked) 1 else 0
        val massageScore = if (findViewById<CheckBox>(R.id.massage).isChecked) 1 else 0
        val accupunctureScore = if (findViewById<CheckBox>(R.id.accupuncture).isChecked) 1 else 0
        val hotBathScore = if (findViewById<CheckBox>(R.id.hotBath).isChecked) 1 else 0
        val pranayamaScore = if (findViewById<CheckBox>(R.id.pranayama).isChecked) 1 else 0
        val additionalScoreString = additionalScoreTextBox.text.toString()
        val additionalScore = if (additionalScoreString == "") 0 else additionalScoreString.toInt()
        val essentialsScore = sleepScore + exerciseScore + nutritionScore + infectionScore + meditationScore + overeatingScore + mentalStressScore + physicalStressScore
        val extrasScore = saunaScore + physiotherapyScore + massageScore + accupunctureScore + hotBathScore + pranayamaScore
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
            accupunctureScore,
            massageScore,
            hotBathScore,
            additionalScore,
            notes,
            total
        )

        database.child("${Constants.DATE_FORMATTER.format(date)}").setValue(score)
        Toast.makeText(applicationContext, "Progress saved", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun selectedText(id: Int) =
        findViewById<RadioButton>(findViewById<RadioGroup>(id).checkedRadioButtonId).text

    private fun fetchForDate(date: String) {
        database.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
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
        findViewById<RadioButton>(R.id.defaultSleep).isChecked = true;
        findViewById<RadioButton>(R.id.defaultExercise).isChecked = true;
        findViewById<RadioButton>(R.id.defaultNutrition).isChecked = true;
        findViewById<RadioButton>(R.id.defaultInfection).isChecked = true;
        findViewById<RadioButton>(R.id.defaultMeditation).isChecked = true;
        findViewById<RadioButton>(R.id.defaultOvereating).isChecked = true;
        findViewById<RadioButton>(R.id.defaultMentalStress).isChecked = true;
        findViewById<RadioButton>(R.id.defaultPhysicalStress).isChecked = true;
    }

    private fun setSelections(score: Score) {
        sleepInverseMap[score.sleepScore]?.isChecked = true
        exerciseInverseMap[score.exerciseScore]?.isChecked = true
        nutritionInverseMap[score.nutritionScore]?.isChecked = true
        infectionInverseMap[score.infectionScore]?.isChecked = true
        meditationInverseMap[score.meditationScore]?.isChecked = true
        overeatingInverseMap[score.overeatingScore]?.isChecked = true
        mentalStressInverseMap[score.mentalStressScore]?.isChecked = true
        physicalStressInverseMap[score.physicalStressScore]?.isChecked = true

        sleepSeekBar.progress = when (score.sleepScore) {
            -2 -> 0
            2 -> 1
            4 -> 2
            else -> 3
        }
        showProgressTexts()

        if (score.sauna == 1) findViewById<CheckBox>(R.id.sauna).isChecked = true
        if (score.physiotherapy == 1) findViewById<CheckBox>(R.id.physio).isChecked = true
        if (score.massage == 1) findViewById<CheckBox>(R.id.massage).isChecked = true
        if (score.pranayama == 1) findViewById<CheckBox>(R.id.pranayama).isChecked = true
        if (score.accupuncture == 1) findViewById<CheckBox>(R.id.accupuncture).isChecked = true
        if (score.hotBath == 1) findViewById<CheckBox>(R.id.hotBath).isChecked = true

        if (score.notes.isNotEmpty()) findViewById<EditText>(R.id.notes).setText(score.notes)
        findViewById<EditText>(R.id.additionalScore).setText(score.additional.toString())
    }

    private lateinit var sleepMap: Map<String, Int>
    private lateinit var exerciseMap: Map<String, Int>
    private lateinit var nutritionMap: Map<String, Int>
    private lateinit var infectionMap: Map<String, Int>
    private lateinit var meditationMap: Map<String, Int>
    private lateinit var overeatingMap: Map<String, Int>
    private lateinit var mentalStressMap: Map<String, Int>
    private lateinit var physicalStressMap: Map<String, Int>

    private lateinit var sleepInverseMap: Map<Int, RadioButton>
    private lateinit var exerciseInverseMap: Map<Int, RadioButton>
    private lateinit var nutritionInverseMap: Map<Int, RadioButton>
    private lateinit var infectionInverseMap: Map<Int, RadioButton>
    private lateinit var meditationInverseMap: Map<Int, RadioButton>
    private lateinit var overeatingInverseMap: Map<Int, RadioButton>
    private lateinit var mentalStressInverseMap: Map<Int, RadioButton>
    private lateinit var physicalStressInverseMap: Map<Int, RadioButton>

    @SuppressLint("WrongViewCast")
    fun createConstants() {
        sleepMap = hashMapOf(
            getString(R.string.SLEEP_9) to 6,
            getString(R.string.SLEEP_8) to 4,
            getString(R.string.SLEEP_7) to 2,
            getString(R.string.SLEEP_LESS) to -2
        )

        exerciseMap = hashMapOf(
            getString(R.string.NO) to -2,
            getString(R.string.WARMUP) to -1,
            getString(R.string.MILD) to 0,
            getString(R.string.AVERAGE) to 1,
            getString(R.string.NONE) to -2
        )

        nutritionMap = hashMapOf(
            getString(R.string.BAD) to -2,
            getString(R.string.AVERAGE) to 0,
            getString(R.string.GOOD) to 2
        )

        infectionMap = hashMapOf(
            getString(R.string.NO) to 0,
            getString(R.string.PROBABLY) to -1,
            getString(R.string.YES) to -2,
            getString(R.string.ANTIBIOTICS) to -3
        )

        meditationMap = hashMapOf(
            getString(R.string.NONE) to -1,
            getString(R.string.MEDIATATION_10) to 1,
            getString(R.string.MEDIATATION_30) to 3,
            getString(R.string.MEDIATATION_60) to 5
        )

        overeatingMap = hashMapOf(
            getString(R.string.NO) to 1,
            getString(R.string.JUST_RIGHT) to 0,
            getString(R.string.YES) to -1
        )

        mentalStressMap = hashMapOf(
            getString(R.string.HAPPY) to 1,
            getString(R.string.NO) to 0,
            getString(R.string.MILD) to -1,
            getString(R.string.HIGH) to -3
        )

        physicalStressMap = hashMapOf(
            getString(R.string.NO) to 0,
            getString(R.string.MILD) to -1,
            getString(R.string.HIGH) to -3
        )

        sleepInverseMap = hashMapOf(
            6 to findViewById<RadioButton>(R.id.sleep9),
            4 to findViewById<RadioButton>(R.id.sleep8),
            2 to findViewById<RadioButton>(R.id.sleep7),
            -2 to findViewById<RadioButton>(R.id.defaultSleep)
        )

        exerciseInverseMap = hashMapOf(
            -2 to findViewById<RadioButton>(R.id.defaultExercise),
            -1 to findViewById<RadioButton>(R.id.exerciseWarmup),
            0 to findViewById<RadioButton>(R.id.exerciseMild),
            1 to findViewById<RadioButton>(R.id.exerciseAverage),
            2 to findViewById<RadioButton>(R.id.exerciseGood)
        )

        nutritionInverseMap = hashMapOf(
            2 to findViewById<RadioButton>(R.id.nutritionGood),
            0 to findViewById<RadioButton>(R.id.nutritionAverage),
            -2 to findViewById<RadioButton>(R.id.defaultNutrition)
        )

        infectionInverseMap = hashMapOf(
            0 to findViewById<RadioButton>(R.id.defaultInfection),
            -1 to findViewById<RadioButton>(R.id.infectionProbably),
            -2 to findViewById<RadioButton>(R.id.infectionYes),
            -3 to findViewById<RadioButton>(R.id.infectionAntibiotics)
        )

        meditationInverseMap = hashMapOf(
            -1 to findViewById<RadioButton>(R.id.defaultMeditation),
            1 to findViewById<RadioButton>(R.id.meditation10),
            3 to findViewById<RadioButton>(R.id.meditation30),
            5 to findViewById<RadioButton>(R.id.meditation60)
        )

        overeatingInverseMap = hashMapOf(
            1 to findViewById<RadioButton>(R.id.overeatingNo),
            0 to findViewById<RadioButton>(R.id.overeatingJustRight),
            -1 to findViewById<RadioButton>(R.id.defaultOvereating)
        )

        mentalStressInverseMap = hashMapOf(
            1 to findViewById<RadioButton>(R.id.mshappy),
            0 to findViewById<RadioButton>(R.id.msno),
            -1 to findViewById<RadioButton>(R.id.msmild),
            -3 to findViewById<RadioButton>(R.id.defaultMentalStress)
        )

        physicalStressInverseMap = hashMapOf(
            0 to findViewById<RadioButton>(R.id.psno),
            -1 to findViewById<RadioButton>(R.id.psmild),
            -3 to findViewById<RadioButton>(R.id.defaultPhysicalStress)
        )
    }

    private fun sleepScore(s: CharSequence) = sleepMap.getOrDefault(s.toString(), 0)
    private fun exerciseScore(s: CharSequence) = exerciseMap.getOrDefault(s.toString(), 0)
    private fun nutritionScore(s: CharSequence) = nutritionMap.getOrDefault(s.toString(), 0)
    private fun infectionScore(s: CharSequence) = infectionMap.getOrDefault(s.toString(), 0)
    private fun meditationScore(s: CharSequence) = meditationMap.getOrDefault(s.toString(), 0)
    private fun overeatingScore(s: CharSequence) = overeatingMap.getOrDefault(s.toString(), 0)
    private fun mentalStressScore(s: CharSequence) = mentalStressMap.getOrDefault(s.toString(), 0)
    private fun physicalStressScore(s: CharSequence) =
        physicalStressMap.getOrDefault(s.toString(), 0)

    private fun showProgressTexts() {
        showSleepText()
    }

    private fun showSleepText() {
        sleepText.text = when (sleepSeekBar.progress) {
            0 -> "Less than 7 Hours"
            1 -> ">7 Hours"
            2 -> ">8 Hours"
            else -> ">9 Hours"
        }
    }
}