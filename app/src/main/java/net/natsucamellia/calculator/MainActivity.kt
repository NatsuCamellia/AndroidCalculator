package net.natsucamellia.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.natsucamellia.calculator.ui.theme.CalculatorTheme
import net.natsucamellia.calculator.ui.theme.Typography

sealed class ButtonData {
    data class Char(
        val action: () -> Unit,
        val char: kotlin.Char
    ): ButtonData()
    data class Icon(
        val action: () -> Unit,
        val icon: ImageVector
    )
}

class Expression {
    var value by mutableStateOf("")
        private set
    var result = ""
        private set

    private fun evaluate(): String {
        // Parse result and compute value
        var total = 0f
        var temp = 0f
        var number = ""
        var prevOperator = '+'

        value.forEach {
            if (it.isDigit() || it == '.') {
                number += it
            } else if (isOperator(it)) {
                // Collect number into temp
                prevOperator.let { op ->
                    when (op) {
                        '×' -> temp *= number.toFloat()
                        '÷' -> temp /= number.toFloat()
                        '%' -> temp %= number.toFloat()
                        '+' -> temp += number.toFloat()
                        '-' -> temp -= number.toFloat()
                        else -> {}
                    }.also { number = "" }
                }
                // Collect temp into total
                if (it == '+' || it == '-') {
                    total += temp
                    temp = 0f
                }
                
                prevOperator = it
            }
        }

        if (number != "") {
            prevOperator.let { op ->
                when (op) {
                    '×' -> temp *= number.toFloat()
                    '÷' -> temp /= number.toFloat()
                    '%' -> temp %= number.toFloat()
                    '+' -> temp += number.toFloat()
                    '-' -> temp -= number.toFloat()
                    else -> {}
                }.also { number = "" }
            }
        }
        total += temp
        return total.toString()
    }

    fun push(char: Char) {
        // Remove repeated operators
        if (isOperator(char) && value.isNotEmpty() && isOperator(value.last())) {
            pop()
        }
        if (char == '.') {
            if (value.isEmpty()) {
                push('0')
            } else if (isOperator(value.last())) {
                return
            }
        }
        value += char
        result = evaluate()
    }

    fun clear() {
        value = ""
        result = evaluate()
    }

    fun pop() {
        if (value.isNotEmpty())
            value = value.substring(0, value.length - 1)
        result = evaluate()
    }

    private fun isOperator(char: Char): Boolean {
        return char == '+' || char == '-' || char == '×' || char == '÷' || char == '%'
    }
}

val expression = Expression()

val buttons = listOf(
    ButtonData.Char({ expression.clear() }, 'c'),
    ButtonData.Icon({ expression.pop() }, Icons.AutoMirrored.Outlined.Backspace),
    ButtonData.Icon({ expression.push('%') }, Icons.Outlined.Percent),
    ButtonData.Char({ expression.push('÷') }, '÷'),
    ButtonData.Char({ expression.push('7') }, '7'),
    ButtonData.Char({ expression.push('8') }, '8'),
    ButtonData.Char({ expression.push('9') }, '9'),
    ButtonData.Char({ expression.push('×') }, '×'),
    ButtonData.Char({ expression.push('4') }, '4'),
    ButtonData.Char({ expression.push('5') }, '5'),
    ButtonData.Char({ expression.push('6') }, '6'),
    ButtonData.Icon({ expression.push('-') }, Icons.Outlined.Remove),
    ButtonData.Char({ expression.push('1') }, '1'),
    ButtonData.Char({ expression.push('2') }, '2'),
    ButtonData.Char({ expression.push('3') }, '3'),
    ButtonData.Icon({ expression.push('+') }, Icons.Outlined.Add),
    ButtonData.Icon({}, Icons.Outlined.Calculate),
    ButtonData.Char({ expression.push('0') }, '0'),
    ButtonData.Char({ expression.push('.') }, '.'),
    ButtonData.Char({}, '=')
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
                    }, modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(text = expression.value.ifEmpty { "0" }, style = Typography.displayLarge)
                        expression.result.let { 
                            if (it.isNotEmpty()) {
                                Text(text = it, style = Typography.titleLarge)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonPanel()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CalculatorTheme {
        Greeting("Android")
    }
}

@Composable
fun ButtonPanel(
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(buttons) {
            when (it) {
                is ButtonData.Icon -> {
                    FilledTonalButton(
                        onClick = it.action,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Icon(imageVector = it.icon, contentDescription = null)
                    }
                }
                is ButtonData.Char -> {
                    FilledTonalButton(
                        onClick = it.action,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Text(text = it.char.toString(), style = Typography.headlineMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    char: Char,
    onClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = {
            onClick(char)
        },
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.aspectRatio(1f)
    ) {
        Text(
            text = char.toString(),
            style = Typography.headlineMedium
        )
    }
}