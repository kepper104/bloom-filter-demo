import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BloomViewModel(
    var windowState: WindowState
) {
    var insertedElementText by mutableStateOf("")
    var queriedElementText by mutableStateOf("")

    var elementCheckResultText by mutableStateOf("Пока ничего")
    var elementCheckResultColor by mutableStateOf(Color.Gray)
    var elementCheckResultStatus by mutableStateOf(GuessStatus.None)

//    private var usedHashFunctions = listOf(::hashByLength)
//    private var usedHashFunctions = listOf(::hashByLength, ::hashByCharSum)
    private var usedHashFunctions = listOf(::hashByLength, ::hashByCharSum, ::hashByCharAvg)


    var tableSize = 10

    var tableParents by mutableStateOf(List(tableSize) { TableParent(enabled = false, highlighted = false) })
    var tableChildren by mutableStateOf(List(tableSize){ List(10) {TableChild()} })

    var tableActual by mutableStateOf(ActualTable())


    fun clearAll() {
        insertedElementText = ""
        queriedElementText = ""
        elementCheckResultText = "Пока ничего"
        elementCheckResultColor = Color.Gray
        elementCheckResultStatus = GuessStatus.None
        tableParents = List(tableSize) { TableParent(enabled = false, highlighted = false) }
        tableChildren = List(tableSize){ List(10) {TableChild()} }
        tableActual = ActualTable()
    }

    private fun clearTableChildrenHighlighting() {
        tableChildren = tableChildren.map { childrenRow ->
            childrenRow.map { it.copy(highlighted = false) }
        }
    }

    private fun clearTableParentHighlighting() {
        tableParents = tableParents.map { it.copy(highlighted = false) }
    }

    fun insertElement() {
        if (insertedElementText.trim().isEmpty()) return

        insertedElementText = insertedElementText.lowercase()

        var inserted = false

        tableActual = tableActual.copy(
            elements = tableActual.elements.map { element ->
                if (element == null && !inserted) {
                    inserted = true
                    insertedElementText
                } else {
                    element
                }
            }
        )

        for(hashFunction in usedHashFunctions) {
            val tableParentIdToEnable = hashFunction(insertedElementText, tableSize)

            // Enable according to hashes new parent table cells
            tableParents = tableParents.mapIndexed { index, tableParent ->
                if (index == tableParentIdToEnable){
                    tableParent.copy(enabled = true)
                } else {
                    tableParent
                }
            }

            // Add (enable and populate with data) currently entered element into tableChildren
            tableChildren = tableChildren.mapIndexed { rowIndex, row ->
                if (rowIndex == tableParentIdToEnable){
                    var isInserted = false
                    row.map { bloomChild ->
                        if (!bloomChild.added && !isInserted){
                            isInserted = true
                            bloomChild.copy(added = true, text = insertedElementText, highlighted = false)
                        } else {
                            bloomChild
                        }
                    }
                } else {
                    row
                }
            }
        }
        insertedElementText = ""
    }

    private fun isElementPresentInTableParents(): Boolean? {
        if (queriedElementText.trim().isEmpty()) return null

        return usedHashFunctions.all { hashFunction ->
            val mustBeEnabledId = hashFunction(queriedElementText, tableSize)
            tableParents[mustBeEnabledId].enabled
        }
    }

    private fun isElementPresentInTableActual(): Boolean {
        return tableActual.elements.any { it == queriedElementText }
    }

    private fun updateBloomPredictionSuccessStatus() {
        val existsInTableParents = isElementPresentInTableParents()
        val existsInTableActual = isElementPresentInTableActual()

        if (existsInTableActual && existsInTableParents == true) {
            elementCheckResultStatus = GuessStatus.TruePositive
        } else if (!existsInTableActual && existsInTableParents == true) {
            elementCheckResultStatus = GuessStatus.FalsePositive
        } else if (!existsInTableActual && existsInTableParents == false) {
            elementCheckResultStatus = GuessStatus.TrueNegative
        } else if (existsInTableActual && existsInTableParents == false){
            elementCheckResultStatus = GuessStatus.FalseNegative
        } else {
            println("ERROR. Unexpected values: $existsInTableParents, $existsInTableActual")
        }
    }

    private fun updateFilterResult() {
        val isPresentInTableParens = isElementPresentInTableParents() ?: return

        elementCheckResultColor =
            if (isPresentInTableParens)
                PrettyColors.Yellow.color
            else
                Color.DarkGray

        elementCheckResultText =
            if (isPresentInTableParens)
                "Элемент `$queriedElementText` возможно присутствует"
            else
                "Элемент `$queriedElementText` точно отсутствует"
    }

    private fun clearCheckValues() {
        elementCheckResultText = "Пока ничего"
        elementCheckResultColor = Color.Gray
        elementCheckResultStatus = GuessStatus.None
        clearTableChildrenHighlighting()
        clearTableParentHighlighting()
        queriedElementText = ""
    }

    fun checkElement() {
        queriedElementText = queriedElementText.lowercase()

        if (queriedElementText.trim().isEmpty()) {
            clearCheckValues()
            return
        }

        highlightTableChildren(queriedElementText)
        highlightTableParents(queriedElementText)
        updateBloomPredictionSuccessStatus()
        updateFilterResult()

        queriedElementText = ""
    }

    private fun highlightTableChildren(queriedText: String) {
        clearTableChildrenHighlighting()

        for(hashFunction in usedHashFunctions) {
            val tableParentIdToSearch = hashFunction(queriedText, tableSize)

            highlightTableChild(tableParentIdToSearch, queriedText)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun highlightTableParents(queriedText: String) {
        GlobalScope.launch {
            clearTableParentHighlighting()

            // To make arrows reappear when calling elementCheck with same parameters
            delay(300L)

            for(hashFunction in usedHashFunctions) {
                val tableParentIdToSearch = hashFunction(queriedText, tableSize)

                tableParents = tableParents.mapIndexed { index, tableParent ->
                    if (index == tableParentIdToSearch){
                        tableParent.copy(highlighted = true)
                    } else {
                        tableParent
                    }
                }
            }
        }
    }

    private fun highlightTableChild(tableParentToCheck: Int, stringToHighlight: String) {
        tableChildren = tableChildren.mapIndexed { rowIndex, row ->
            row.map { tableChild ->
                if (rowIndex == tableParentToCheck && tableChild.text == stringToHighlight){
                    tableChild.copy(highlighted = true)
                } else {
                    tableChild
                }
            }

        }
    }
}