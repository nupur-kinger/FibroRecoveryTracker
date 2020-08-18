import java.io.Serializable

data class Score(
    var sleepScore: Int,
    var exerciseScore: Int,
    var nutritionScore: Int,
    var infectionScore: Int,
    var meditationScore: Int,
    var overeatingScore: Int,
    var mentalStressScore: Int,
    var physicalStressScore: Int,

    var total: Int
) : Serializable {
    constructor() : this(0, 0, 0, 0, 0, 0, 0, 0, 0)
}