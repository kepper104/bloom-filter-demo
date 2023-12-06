fun hashByLength(element: String, modulo: Int): Int {
    return element.length % modulo
}

//fun middleSquare(element: String, digitsToPick: Int): String {
//    var elementSquared =
//}

fun hashByCharSum(element: String, modulo: Int): Int {
    var sum = 0

    for (char: Char in element) {
        sum += char.code
    }
    return sum % modulo
}