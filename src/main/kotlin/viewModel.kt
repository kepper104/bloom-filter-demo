import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowState

class BloomViewModel(
    val windowState: WindowState
) {
    var insertedElementText by mutableStateOf("")
    var queriedElementText by mutableStateOf("")

    var showElementResult by mutableStateOf(false)
    var showElementText by mutableStateOf("")

//    private var usedHashFunctions = listOf(::hashByLength)

    private var usedHashFunctions = listOf(::hashByLength, ::hashByCharSum)

    var tableSize = 10

    var bloomTable by mutableStateOf(List(tableSize) { BloomBoolean(false, false) })
    var elementVisualizationTable by mutableStateOf(List(tableSize){ List(10) {BloomElement()} })



    fun insertElement() {
        if (insertedElementText.trim() == "") return

        insertedElementText = insertedElementText.lowercase()

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
                    row.mapIndexed { index, b ->
                        if (b.added == false && !insertedVar){
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

    fun isElementMaybePresent(): Boolean? {
        if (queriedElementText.trim() == "") return null


        var isGood = true
        for(hashFunction in usedHashFunctions) {
            val mustBeEnabledId = hashFunction(queriedElementText, tableSize)
            if(bloomTable[mustBeEnabledId].enabled == false) {
                isGood = false
                break
            }
        }
        return isGood
    }

    fun clearElementHighlighting() {
        elementVisualizationTable = elementVisualizationTable.mapIndexed { rowIndex, row ->
            row.mapIndexed { index, b ->
                b.copy(highlighted = false)
            }

        }
    }

    fun clearTableCellHighlighting() {
        bloomTable = bloomTable.mapIndexed { index, b ->
            b.copy(maybeContainsGivenString = false)
        }
    }

    fun showQueryResultPopup(maybeExists: Boolean) {
        println("Element '${queriedElementText}' is present: $maybeExists")

        showElementResult = true
        showElementText = if (maybeExists)
                            "Element $queriedElementText may be present"
                        else
                            "Element $queriedElementText DEFINITELY is NOT present"
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

        val elementMaybeExists = isElementMaybePresent() ?: return
        showQueryResultPopup(elementMaybeExists)

        queriedElementText = ""
    }

    fun highlightBloomElements() {
        clearElementHighlighting()
        for(hashFunction in usedHashFunctions) {
            val mustBeEnabledId = hashFunction(queriedElementText, tableSize)

            highlightElementInTable(mustBeEnabledId, queriedElementText)
        }
    }

    fun highlightTableCells() {
        clearTableCellHighlighting()
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
    }


    fun highlightElementInTable(idToHighlight: Int, stringToHighlight: String) {
        elementVisualizationTable = elementVisualizationTable.mapIndexed { rowIndex, row ->
            row.mapIndexed { index, b ->
                if (rowIndex == idToHighlight && b.text == stringToHighlight){
                    b.copy(highlighted = true)
                } else {
                    b
                }
            }

        }
    }
}

data class BloomElement(
    val added: Boolean = false,
    val text: String = "NONE",
    val highlighted: Boolean = false,
)

data class BloomBoolean(
    val enabled: Boolean,
    val maybeContainsGivenString: Boolean,
)