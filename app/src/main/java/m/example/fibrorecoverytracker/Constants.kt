package m.example.fibrorecoverytracker

import java.time.format.DateTimeFormatter

class Constants {
    companion object {
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyy")
        val POOR_PROGRESS: Double = 0.0
        val GOOD_PROGRESS: Double = 10.0
        val POOR_SCORE: Double = 30.0
        val GOOD_SCORE: Double = 80.0
    }
}

interface Metric<T : Metric<T>> {
    fun score(): Int
    fun options(): Array<T>
    fun min(): Int
    fun max(): Int
}

enum class Sleep(private val score: Int, private val label: String) : Metric<Sleep> {
    LESS_THAN_7(-2, "Less"),
    MORE_THAN_7(2, ">7"),
    MORE_THAN_8(4, ">8"),
    MORE_THAN_9(6, ">9");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = LESS_THAN_7.score
    override fun max() = MORE_THAN_9.score
}

enum class Exercise(private val score: Int, private val label: String) : Metric<Exercise> {
    NONE(-2, "None"),
    WARM_UP(-1, "Warmup"),
    MILD(0, "Mild"),
    AVERAGE(1, "Average"),
    GOOD(2, "Good");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = NONE.score
    override fun max() = GOOD.score
}

enum class Nutrition(private val score: Int, private val label: String) : Metric<Nutrition> {
    BAD(-2, "Bad"),
    AVERAGE(0, "Average"),
    GOOD(2, "Good");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = BAD.score
    override fun max() = GOOD.score
}

enum class Infection(private val score: Int, private val label: String) : Metric<Infection> {
    ANTIBIOTICS(-3, "Antibiotics"),
    YES(-2, "Yes"),
    PROBABLY(-1, "Probably"),
    NO(0, "No");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = ANTIBIOTICS.score
    override fun max() = NO.score
}

enum class Meditation(private val score: Int, private val label: String) : Metric<Meditation> {
    MIN_0(-1, "0m"),
    MIN_10(1, "10m"),
    MIN_30(3, "30m"),
    MIN_60(5, "60m");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = MIN_0.score
    override fun max() = MIN_60.score
}

enum class Overeating(private val score: Int, private val label: String) : Metric<Overeating> {
    Yes(-1, "Yes"),
    JUST_RIGHT(0, "Just right"),
    NO(1, "No");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = Yes.score
    override fun max() = NO.score
}

enum class PhysicalStress(private val score: Int, private val label: String) :
    Metric<PhysicalStress> {
    HIGH(-3, "High"),
    MILD(-1, "Mild"),
    NO(0, "No");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = HIGH.score
    override fun max() = NO.score
}

enum class MentalStress(private val score: Int, private val label: String) : Metric<MentalStress> {
    HIGH(-3, "High"),
    MILD(-1, "Mild"),
    NO(0, "No"),
    HAPPY(1, "Happy");

    override fun score() = this.score
    override fun options() = values()
    override fun toString() = this.label
    override fun min() = HIGH.score
    override fun max() = HAPPY.score
}