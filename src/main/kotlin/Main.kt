@file:Suppress("FunctionName")

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {
    val windowState = rememberWindowState(size = DpSize(800.dp, 1080.dp))
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


@Composable
@Preview
fun App(viewModel: BloomViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(viewModel)
        InsertElementRow(viewModel)
        QueryElementRow(viewModel)

        TableVisualization(viewModel)
        TableActualRow(viewModel)
        Footer(viewModel)
    }
}

@Composable
fun Header(viewModel: BloomViewModel) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Демонстрация Фильтра Блума",
            fontSize = 36.sp
        )
        Button(
            modifier = Modifier.width(170.dp),
            onClick = {
                viewModel.clearAll()
            }
        ) {
            Text(
                text = "Сбросить",
                fontSize = 24.sp
            )
        }
    }
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
            label = { Text("Проверка элемента") },

            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Проверка элемента")
            },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(50.dp))
        Button(
            onClick = {
                viewModel.checkElement()
            },
            modifier = Modifier.width(170.dp)
        ) {
            Text(
                text = "Проверить",
                fontSize = 24.sp
            )
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
            label = { Text("Добавление элемента") },
            leadingIcon = {
                Icon(Icons.Default.Add, contentDescription = "Add Element")
            },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(50.dp))
        Button(
            onClick = {
                viewModel.insertElement()
            },
            modifier = Modifier.width(170.dp)
        ) {
            Text(
                text = "Добавить",
                fontSize = 24.sp
            )
        }
    }
}


@Composable
fun TableActualRow(viewModel: BloomViewModel) {
    val density = LocalDensity.current

    Row (
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Настоящие элементы",
            fontSize = 20.sp
        )
        Spacer(Modifier.width(20.dp))
        for (element in viewModel.tableActual.elements) {
            AnimatedVisibility(
                visible = element != null,
                enter = slideInHorizontally {
                    with(density) { 20.dp.roundToPx() }
                },
                exit = slideOutHorizontally {
                    with(density) { 20.dp.roundToPx() }
                }
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.height(50.dp)
                ) {
                    if (element != null) {
                        Text(
                            text = element
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))
        }
    }
}
@Composable
fun Footer(viewModel: BloomViewModel) {
    Card (
        modifier = Modifier
            .fillMaxSize(),
        elevation = 8.dp,

    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier
                    .background(viewModel.elementCheckResultColor)
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Что думает фильтр",
                    fontSize = 20.sp,
                    color = if (viewModel.elementCheckResultColor == Color.DarkGray) Color.White else Color.Black

                )
                Text(
                    text = viewModel.elementCheckResultText,
                    color = if (viewModel.elementCheckResultColor == Color.DarkGray) Color.White else Color.Black

                )
            }

            Column (
                modifier = Modifier
                    .background(viewModel.elementCheckResultStatus.color)
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ситуация в реальности",
                    fontSize = 20.sp
                )
                Text(
                    text = viewModel.elementCheckResultStatus.text
                )
            }


        }
    }
}



@Composable
fun TableVisualization(viewModel: BloomViewModel) {
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
                TableRows(viewModel)
            }
        }
    }
}

@Composable
fun TableRows(viewModel: BloomViewModel) {
    val density = LocalDensity.current

    for (i in 0 until viewModel.tableSize) {
        Row {
            Row (
                modifier = Modifier.width(50.dp)
            ) {
                AnimatedVisibility(
                    visible = viewModel.tableParents[i].highlighted,
                    enter = slideInHorizontally {
                        with(density) { -20.dp.roundToPx() }
                    },
                    exit = slideOutHorizontally {
                        with(density) { 20.dp.roundToPx() }
                    }
                ) {
                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            "Arrow",
                            tint =
                                if (viewModel.tableParents[i].highlighted)
                                    Color.Black
                                else
                                    Color.Transparent
                        )
                    }
                }
            }


            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                modifier = Modifier
                    .size(100.dp, 50.dp)
                    .border(
                        6.dp,
                        if (viewModel.tableParents[i].enabled)
                            PrettyColors.SpringGreen.color
                        else
                            Color.Transparent,
                        RoundedCornerShape(7.dp)
                    ),
                onClick = {},
                shape = RoundedCornerShape(7.dp)
            ) {
                Text("ID $i")
            }

            Spacer(Modifier.width(15.dp))

            for (bloomElement in viewModel.tableChildren[i]) {
                AnimatedVisibility(
                    visible = bloomElement.added,
                    enter = slideInHorizontally {
                        with(density) { 20.dp.roundToPx() }
                    },
                    exit = slideOutHorizontally (),

                    ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor =
                            if (bloomElement.highlighted)
                                PrettyColors.Green.color
                            else
                                Color.Gray
                        ),
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

//  https://www.flaticon.com/free-icons/filter Filter icons created by Freepik - Flaticon
