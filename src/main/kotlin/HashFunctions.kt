fun hashByLength(element: String, modulo: Int): Int {
    return element.length % modulo
}

fun hashByCharSum(element: String, modulo: Int): Int {
    var sum = 0

    for (char: Char in element) {
        sum += char.code
    }
    return sum % modulo
}

fun hashByCharAvg(element: String, modulo: Int): Int {
    var sum = 0

    for (char: Char in element) {
        sum += char.code
    }

    sum /= element.length

    return sum % modulo
}