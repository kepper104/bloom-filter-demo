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

    var elementQueryText by mutableStateOf("Пока ничего")
    var elementQueryColor by mutableStateOf(Color.Gray)
    var elementQueryStatus by mutableStateOf(GuessStatus.None)

//    private var usedHashFunctions = listOf(::hashByLength)
//    private var usedHashFunctions = listOf(::hashByLength, ::hashByCharSum)
    private var usedHashFunctions = listOf(::hashByLength, ::hashByCharSum, ::hashByCharAvg)


    var tableSize = 10

    var bloomTable by mutableStateOf(List(tableSize) { BloomBoolean(enabled = false, maybeContainsGivenString = false) })
    var elementVisualizationTable by mutableStateOf(List(tableSize){ List(10) {BloomElement()} })

    var realTable by mutableStateOf(RealTable())


    fun clearAll() {
        insertedElementText = ""
        queriedElementText = ""
        elementQueryText = "Пока ничего"
        elementQueryColor = Color.Gray
        bloomTable = List(tableSize) { BloomBoolean(enabled = false, maybeContainsGivenString = false) }
        elementVisualizationTable = List(tableSize){ List(10) {BloomElement()} }
        realTable = RealTable()
        elementQueryStatus = GuessStatus.None
    }

    private fun clearElementHighlighting() {
        elementVisualizationTable = elementVisualizationTable.map { row ->
            row.map { b ->
                b.copy(highlighted = false)
            }
        }
    }

    private fun clearTableCellHighlighting() {
        bloomTable = bloomTable.map { b ->
            b.copy(maybeContainsGivenString = false)
        }
    }


    fun insertElement() {
        if (insertedElementText.trim() == "") return

        insertedElementText = insertedElementText.lowercase()

        var inserted = false
        realTable = realTable.copy(
            elements = realTable.elements.map { el ->
                if (el == null && !inserted) {
                    inserted = true
                    insertedElementText
                } else {
                    el
                }
            }
        )

        for(hashFunction in usedHashFunctions) {
            val idToEnable = hashFunction(insertedElementText, tableSize)
            bloomTable = bloomTable.mapIndexed { index, b ->
                if (index == idToEnable){
                    b.copy(enabled = true)
                } else {
                    b
                }
            }

            elementVisualizationTable = elementVisualizationTable.mapIndexed { rowIndex, row ->
                if (rowIndex == idToEnable){
                    var insertedVar = false
                    row.map { b ->
                        if (!b.added && !insertedVar){
                            insertedVar = true
                            b.copy(added = true, text = insertedElementText, highlighted = false)
                        } else {
                            b
                        }
                    }
                } else {
                    row
                }
            }
        }
        insertedElementText = ""
    }

    private fun isElementMaybePresent(): Boolean? {
        if (queriedElementText.trim() == "") return null

        var isGood = true
        for(hashFunction in usedHashFunctions) {
            val mustBeEnabledId = hashFunction(queriedElementText, tableSize)
            if(!bloomTable[mustBeEnabledId].enabled) {
                isGood = false
                break
            }
        }

        return isGood
    }

    private fun isElementPresentInRealTable(): Boolean {
        for (element in realTable.elements) {
            if (element == queriedElementText) {
                return true
            }
        }
        return false
    }

    private fun updateBloomSuccessStatus() {
        val maybeExistsInBloomFilter = isElementMaybePresent()
        val existsInRealTable = isElementPresentInRealTable()

        if (existsInRealTable && maybeExistsInBloomFilter == true) {
            elementQueryStatus = GuessStatus.TruePositive
        } else if (!existsInRealTable && maybeExistsInBloomFilter == true) {
            elementQueryStatus = GuessStatus.FalsePositive
        } else if (!existsInRealTable && maybeExistsInBloomFilter == false) {
            elementQueryStatus = GuessStatus.TrueNegative
        } else if (existsInRealTable && maybeExistsInBloomFilter == false){
            elementQueryStatus = GuessStatus.FalseNegative
        } else {
            println("Unexpected values: $maybeExistsInBloomFilter, $existsInRealTable")
        }
    }


    private fun changeFilterPrediction() {
        val maybeExists = isElementMaybePresent() ?: return

        elementQueryColor = if (maybeExists) DataClassesEnums.Yellow.color else Color.DarkGray
        elementQueryText = if (maybeExists)
                            "Элемент $queriedElementText возможно присутствует"
                        else
                            "Элемент $queriedElementText точно отсутствует"
    }

    fun checkElement() {
        queriedElementText = queriedElementText.lowercase()

        if (queriedElementText.trim() == "") {
            clearElementHighlighting()
            clearTableCellHighlighting()
            queriedElementText = ""

            return
        }

        highlightBloomElements()
        highlightTableCells()
        updateBloomSuccessStatus()
        changeFilterPrediction()
    }

    private fun highlightBloomElements() {
        clearElementHighlighting()
        for(hashFunction in usedHashFunctions) {
            val mustBeEnabledId = hashFunction(queriedElementText, tableSize)

            highlightElementInTable(mustBeEnabledId, queriedElementText)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun highlightTableCells() {
        GlobalScope.launch {
            clearTableCellHighlighting()

            delay(300L)

            for(hashFunction in usedHashFunctions) {
                val possiblyContainingElementID = hashFunction(queriedElementText, tableSize)
                bloomTable = bloomTable.mapIndexed { index, b ->
                    if (index == possiblyContainingElementID){
                        b.copy(maybeContainsGivenString = true)
                    } else {
                        b
                    }
                }
            }
            queriedElementText = ""
        }
    }


    private fun highlightElementInTable(idToHighlight: Int, stringToHighlight: String) {
        elementVisualizationTable = elementVisualizationTable.mapIndexed { rowIndex, row ->
            row.map { b ->
                if (rowIndex == idToHighlight && b.text == stringToHighlight){
                    b.copy(highlighted = true)
                } else {
                    b
                }
            }

        }
    }
}