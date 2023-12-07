@file:Suppress("FunctionName")

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {
    val windowState = rememberWindowState(size = DpSize(600.dp, 1000.dp))
    val viewModel = BloomViewModel(windowState)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Bloom Filter Demo",
        icon = painterResource("logo.png"),
        state = viewModel.windowState
    ) {
        App(viewModel)
    }
}

//  <a href="https://www.flaticon.com/free-icons/filter" title="filter icons">Filter icons created by Freepik - Flaticon</a>

@Composable
@Preview
fun App(viewModel: BloomViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        InsertElementRow(viewModel)
        QueryElementRow(viewModel)

        BloomFilterVisualization(viewModel)
    }
    showPopup(viewModel)
}


@Composable
fun QueryElementRow(viewModel: BloomViewModel){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = viewModel.queriedElementText,
            onValueChange = { viewModel.queriedElementText = it },
            label = { Text("Query Element") },

            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Query Element")
            },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                viewModel.checkElement()
            },
            modifier = Modifier.width(100.dp)
        ) {
            Text("Query")
        }
    }
}

@Composable
fun InsertElementRow(viewModel: BloomViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = viewModel.insertedElementText,
            onValueChange = { viewModel.insertedElementText = it },
            label = { Text("Element") },
            leadingIcon = {
                Icon(Icons.Default.Add, contentDescription = "Add Element")
            },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                viewModel.insertElement()
            },
            modifier = Modifier.width(100.dp)
        ) {
            Text("Add")
        }
    }
}

@Composable
fun showPopup(viewModel: BloomViewModel) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = viewModel.showElementResult,
        enter = slideInVertically {
            with(density) { -200.dp.roundToPx() }
        },
        exit = slideOutVertically(),
        modifier = Modifier.offset(0.dp, )
    ) {
        ElementResultPopup(
            message = viewModel.showElementText,
            onDismiss = { viewModel.showElementResult = false }
        )
    }
}

@Composable
fun ElementResultPopup(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .width(280.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = message)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDismiss) {
                Text(text = "Dismiss")
            }
        }
    }
}


@Composable
fun BloomFilterVisualization(viewModel: BloomViewModel) {
    val density = LocalDensity.current

    LazyColumn (
        Modifier.fillMaxWidth()
    ) {
        item {
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.LightGray)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                for (i in 0 until viewModel.tableSize) {
                    Row {
                        IconButton(
                            onClick = {},
                        ) {
                            Icon(Icons.Default.ArrowForward, "Arrow", tint = if (viewModel.bloomTable[i].maybeContainsGivenString) Color.Black else Color.Transparent)
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor =
                                if (false) { // if you want to change button color using viewModel.bloomTable[i].maybeContainsGivenString
                                    Color.Yellow
                                } else {
                                    if (viewModel.bloomTable[i].enabled)
                                        Color.Green
                                    else
                                        Color.Gray

                                }),
                                modifier = Modifier.size(100.dp, 50.dp),
                                onClick = {},
                            ) {
                            Text("ID $i")
                        }

                        Spacer(Modifier.width(15.dp))

                        for (bloomElement in viewModel.elementVisualizationTable[i]) {
                            AnimatedVisibility(
                                visible = bloomElement.added,
                                enter = slideInHorizontally {
                                    with(density) { 20.dp.roundToPx() }
                                },
                                exit = slideOutHorizontally (),

                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(backgroundColor = if (bloomElement.highlighted) Color(11, 102, 35) else Color.Gray),
                                    modifier = Modifier.size(100.dp, 50.dp),
                                    onClick = {},
                                ) {
                                    Text(bloomElement.text)
                                }

                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}