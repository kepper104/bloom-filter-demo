import androidx.compose.ui.graphics.Color

enum class PrettyColors(val color: Color) {
    Yellow(Color(254, 228, 64)),
    LightGreen(Color(62, 255, 0)),
    Green(Color(65, 142, 62)),
    Red(Color(237, 49, 69)),
    SpringGreen(Color(0, 240, 168)),
}

data class TableChild(
    val added: Boolean = false,
    val text: String = "NONE",
    val highlighted: Boolean = false,
)

data class TableParent(
    val enabled: Boolean,
    val highlighted: Boolean,
)

data class ActualTable(
    val tableSize: Int = 10,
    val elements: List<String?> = List(tableSize) {null}
)

enum class GuessStatus(
    val text: String,
    val color: Color,
) {
    None("Сначала сделайте запрос", Color.Gray),
    TruePositive("OK. Элемент присутствует, фильтр угадал", PrettyColors.LightGreen.color),
    FalsePositive("ОШИБКА. Элемент отсутствует, но по фильтру присутствует", PrettyColors.Red.color),
    TrueNegative("ОК. Элемент отсутствует, по фильтру тоже отсутствует", PrettyColors.Green.color),
    FalseNegative("ОШИБКА. Элемент присутствует, по фильтру отсутствует " +
            "(может произойти если попробовать удалить значение из фильтра)", Color.Blue
    ),
}

// true positive - is present, thinks it is present (ALWAYS)
// false positive - is not present, thinks it is present (SOMETIMES)
// true negative - is not present, thinks it is not present (ALWAYS)
// false negative - is present, thinks it is not present (NEVER)
