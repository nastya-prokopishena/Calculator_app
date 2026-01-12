package com.example.calculator

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/* ===================================================
   MainActivity
   Запускає Jetpack Compose та кореневий composable
=================================================== */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

/* ===================================================
   Enum для простої навігації між екранами
   Без Navigation Component — керування через state
=================================================== */
enum class Screen {
    START, SIMPLE, ADVANCED, ABOUT
}

/* ===================================================
   CalculatorApp
   Центральна точка керування екранами
=================================================== */
@Composable
fun CalculatorApp() {

    // rememberSaveable → стан зберігається при повороті екрана
    var currentScreen by rememberSaveable { mutableStateOf(Screen.START) }

    val context = LocalContext.current

    MaterialTheme {
        when (currentScreen) {
            Screen.START -> StartScreen(
                onSimpleClick = { currentScreen = Screen.SIMPLE },
                onAdvancedClick = { currentScreen = Screen.ADVANCED },
                onAboutClick = { currentScreen = Screen.ABOUT },
                onExitClick = { (context as? ComponentActivity)?.finish() }
            )

            Screen.SIMPLE ->
                SimpleCalculatorScreen(onBack = { currentScreen = Screen.START })

            Screen.ADVANCED ->
                AdvancedCalculatorScreen(onBack = { currentScreen = Screen.START })

            Screen.ABOUT ->
                AboutScreen(onBack = { currentScreen = Screen.START })
        }
    }
}

/* ===================================================
   StartScreen
   Просте меню вибору режиму роботи
=================================================== */
@Composable
fun StartScreen(
    onSimpleClick: () -> Unit,
    onAdvancedClick: () -> Unit,
    onAboutClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Calculator", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onSimpleClick, modifier = Modifier.fillMaxWidth()) {
            Text("Simple")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAdvancedClick, modifier = Modifier.fillMaxWidth()) {
            Text("Advanced")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAboutClick, modifier = Modifier.fillMaxWidth()) {
            Text("About")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onExitClick, modifier = Modifier.fillMaxWidth()) {
            Text("Exit")
        }
    }
}

/* ===================================================
   AutoResizeDisplay
   Компонент для відображення тексту з авто-зменшенням
   шрифту, якщо число не вміщується
=================================================== */
@Composable
fun AutoResizeDisplay(
    text: String,
    maxFontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.End,
    minFontSize: TextUnit = 20.sp
) {
    // Поточний розмір шрифту
    var fontSize by remember(text) { mutableStateOf(maxFontSize) }

    // Потрібен для уникнення нескінченного перерахунку
    var needsMeasurement by remember(text) { mutableStateOf(true) }

    Text(
        text = text.ifEmpty { "0" },
        fontSize = fontSize,
        maxLines = 1,          // однорядковий текст
        softWrap = false,      // без переносу
        textAlign = textAlign,
        color = color,
        modifier = modifier.fillMaxWidth(),

        // Викликається після вимірювання тексту
        onTextLayout = { result ->
            if (!needsMeasurement) return@Text

            // Якщо текст не помістився по ширині
            if (result.didOverflowWidth && fontSize > minFontSize) {
                fontSize = (fontSize.value * 0.9f)
                    .coerceAtLeast(minFontSize.value)
                    .sp
            } else {
                // Текст помістився — припиняємо перерахунок
                needsMeasurement = false
            }
        }
    )
}

/* ===================================================
   SIMPLE CALCULATOR
   Реалізація базових арифметичних операцій
=================================================== */
@Composable
fun SimpleCalculatorScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Орієнтація екрана
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Умовне визначення планшета
    val isTablet = configuration.smallestScreenWidthDp >= 600

    /* ---------- СТАН КАЛЬКУЛЯТОРА ---------- */

    // Текст, що відображається на екрані
    var display by rememberSaveable { mutableStateOf("0") }

    // Перше число перед операцією
    var storedValue by rememberSaveable { mutableStateOf<Double?>(null) }

    // Поточний оператор
    var operator by rememberSaveable { mutableStateOf<String?>(null) }

    // Прапорець для початку нового введення
    var isNewInput by rememberSaveable { mutableStateOf(true) }

    /* ---------- ЛОГІКА ОБЧИСЛЕНЬ ---------- */

    fun calculateBinary(second: Double): Double {
        return try {
            when (operator) {
                "+" -> storedValue!! + second
                "-" -> storedValue!! - second
                "×" -> storedValue!! * second
                "÷" -> if (second != 0.0) storedValue!! / second else second
                else -> second
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            storedValue ?: 0.0
        }
    }

    /* ---------- ОБРОБНИКИ КНОПОК ---------- */

    fun onNumberClick(number: String) {
        display =
            if (isNewInput) number
            else if (display == "0") number
            else display + number

        isNewInput = false
    }

    fun onDotClick() {
        if (isNewInput) {
            display = "0."
            isNewInput = false
        } else if (!display.contains(".")) {
            display += "."
        }
    }

    fun onOperatorClick(op: String) {
        storedValue = display.toDoubleOrNull()
        operator = op
        isNewInput = true
    }

    fun onEqualsClick() {
        val result = calculateBinary(display.toDouble())
        display = result.toString().removeSuffix(".0")
        storedValue = null
        operator = null
        isNewInput = true
    }

    fun onClear() {
        display = "0"
        storedValue = null
        operator = null
        isNewInput = true
    }

    /* ---------- АДАПТИВНІ РОЗМІРИ UI ---------- */

    val displayFontSize = when {
        isTablet -> if (isLandscape) 56.sp else 64.sp
        else -> if (isLandscape) 32.sp else 48.sp
    }

    val buttonFontSize = when {
        isTablet -> if (isLandscape) 24.sp else 28.sp
        else -> if (isLandscape) 16.sp else 24.sp
    }

    val buttonHeight = when {
        isTablet -> if (isLandscape) 80.dp else 90.dp
        else -> if (isLandscape) 48.dp else 60.dp
    }

    val paddingValue = if (isTablet) 24.dp else 16.dp
    val spacingValue = if (isTablet) 12.dp else 8.dp

    /* ---------- UI ---------- */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValue)
    ) {

        // Дисплей калькулятора з авто-масштабуванням тексту
        AutoResizeDisplay(
            text = display,
            maxFontSize = displayFontSize,
            modifier = Modifier
                .weight(0.5f)
                .padding(paddingValue)
        )

        Spacer(modifier = Modifier.height(paddingValue))

        val buttons = listOf(
            listOf("7", "8", "9", "+"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "×"),
            listOf("0", ".", "=", "÷")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacingValue)
            ) {
                row.forEach { btn ->
                    Button(
                        onClick = {
                            when (btn) {
                                "=" -> onEqualsClick()
                                "+", "-", "×", "÷" -> onOperatorClick(btn)
                                "." -> onDotClick()
                                else -> onNumberClick(btn)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight)
                    ) {
                        Text(btn, fontSize = buttonFontSize)
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacingValue))
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
        ) {
            Text("Back", fontSize = buttonFontSize)
        }
    }
}

/* ===================================================
   ADVANCED CALCULATOR
   Поки використовує ту саму реалізацію
=================================================== */
@Composable
fun AdvancedCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600

    var display by rememberSaveable { mutableStateOf("0") }
    var storedValue by rememberSaveable { mutableStateOf<Double?>(null) }
    var operator by rememberSaveable { mutableStateOf<String?>(null) }
    var isNewInput by rememberSaveable { mutableStateOf(true) }

    fun calculateBinary(second: Double): Double {
        return try {
            when (operator) {
                "+" -> storedValue!! + second
                "-" -> storedValue!! - second
                "×" -> storedValue!! * second
                "÷" -> if (second != 0.0) storedValue!! / second else throw ArithmeticException("Division by zero")
                "%" -> storedValue!! % second
                "^" -> storedValue!!.pow(second)
                else -> second
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            storedValue ?: 0.0
        }
    }

    fun onNumberClick(number: String) {
        display = if (isNewInput) {
            isNewInput = false
            number
        } else {
            if (display == "0") number else display + number
        }
    }

    fun onDotClick() {
        if (isNewInput) {
            display = "0."
            isNewInput = false
        } else if (!display.contains(".")) display += "."
    }

    fun onSignChange() {
        if (display != "0") display = if (display.startsWith("-")) display.removePrefix("-") else "-$display"
    }

    fun onOperatorClick(op: String) {
        val current = display.toDoubleOrNull()
        if (current == null) {
            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }
        if (storedValue != null && operator != null && !isNewInput) {
            val result = calculateBinary(current)
            storedValue = result
            display = result.toString().removeSuffix(".0")
        } else storedValue = current
        operator = op
        isNewInput = true
    }

    fun onEqualsClick() {
        val current = display.toDoubleOrNull()
        if (current == null) {
            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }
        if (storedValue != null && operator != null) {
            val result = calculateBinary(current)
            display = result.toString().removeSuffix(".0")
            storedValue = null
            operator = null
            isNewInput = true
        }
    }

    fun onUnary(op: String) {
        val value = display.toDoubleOrNull()
        if (value == null) {
            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }
        val result = try {
            when (op) {
                "x²" -> value * value
                "√x" -> if (value >= 0) sqrt(value) else throw ArithmeticException("Invalid input for sqrt")
                "sin" -> sin(Math.toRadians(value))
                "cos" -> cos(Math.toRadians(value))
                "tan" -> tan(Math.toRadians(value))
                "ln" -> if (value > 0) ln(value) else throw ArithmeticException("Invalid input for ln")
                "log" -> if (value > 0) log10(value) else throw ArithmeticException("Invalid input for log")
                else -> value
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            value
        }
        display = result.toString().removeSuffix(".0")
        isNewInput = true
    }

    fun onClear() {
        display = "0"
        storedValue = null
        operator = null
        isNewInput = true
    }

    // Оптимізація для різних пристроїв
    val displayFontSize = when {
        isTablet -> if (isLandscape) 48.sp else 56.sp
        else -> if (isLandscape) 28.sp else 42.sp
    }

    val buttonFontSize = when {
        isTablet -> if (isLandscape) 20.sp else 22.sp
        else -> if (isLandscape) 16.sp else 20.sp
    }

    val buttonHeight = when {
        isTablet -> if (isLandscape) 70.dp else 80.dp
        else -> if (isLandscape) 48.dp else 60.dp
    }

    val paddingValue = when {
        isTablet -> if (isLandscape) 20.dp else 24.dp
        else -> if (isLandscape) 8.dp else 16.dp
    }

    val spacingValue = when {
        isTablet -> if (isLandscape) 8.dp else 10.dp
        else -> if (isLandscape) 4.dp else 8.dp
    }

    // Розкладка кнопок для планшетів та телефонів
    val buttons = when {
        isTablet && isLandscape -> {
            // Планшет в ландшафтному режимі - 4 рядки по 7 кнопок
            listOf(
                listOf("sin", "cos", "tan", "√x", "x²", "ln", "log"),
                listOf("7", "8", "9", "÷", "C", "±", "."),
                listOf("4", "5", "6", "×", "+", "-", "^"),
                listOf("1", "2", "3", "%", "0", "=", "Back")
            )
        }
        isTablet -> {
            // Планшет в портретному режимі
            listOf(
                listOf("sin", "cos", "tan", "√x", "x²", "ln", "log"),
                listOf("7", "8", "9", "÷", "C", "±", "."),
                listOf("4", "5", "6", "×", "+", "-", "^"),
                listOf("1", "2", "3", "%", "0", "=", "Back")
            )
        }
        isLandscape -> {
            // Телефон в ландшафтному режимі
            listOf(
                listOf("sin", "cos", "tan", "√x", "x²", "ln", "log"),
                listOf("7", "8", "9", "÷", "C", "±", "."),
                listOf("4", "5", "6", "×", "+", "-", "^"),
                listOf("1", "2", "3", "%", "0", "=", "Back")
            )
        }
        else -> {
            // Телефон в портретному режимі
            listOf(
                listOf("sin", "cos", "tan", "√x"),
                listOf("x²", "ln", "log", "C"),
                listOf("7", "8", "9", "÷"),
                listOf("4", "5", "6", "×"),
                listOf("1", "2", "3", "-"),
                listOf("0", ".", "±", "+"),
                listOf("^", "=", "", "")
            )
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValue)) {
        AutoResizeDisplay(
            text = display,
            maxFontSize = displayFontSize,
            modifier = Modifier
                .weight(0.5f)
                .padding(paddingValue)
        )
        Spacer(modifier = Modifier.height(paddingValue))

        for (row in buttons) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacingValue)) {
                for (btn in row) {
                    if (btn.isNotEmpty()) {
                        Button(
                            onClick = {
                                when (btn) {
                                    "C" -> onClear()
                                    "=" -> onEqualsClick()
                                    "+", "-", "×", "÷", "%", "^" -> onOperatorClick(btn)
                                    "sin", "cos", "tan", "x²", "√x", "ln", "log" -> onUnary(btn)
                                    "±" -> onSignChange()
                                    "." -> onDotClick()
                                    "Back" -> onBack()
                                    else -> onNumberClick(btn)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) { Text(btn, fontSize = buttonFontSize) }
                    } else Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(spacingValue))
        }

        // Для телефону в портретному режимі - кнопку Back окремо
        if (!isLandscape && !isTablet) {
            Spacer(modifier = Modifier.height(paddingValue))
            Button(onClick = onBack, modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)) {
                Text("Back", fontSize = buttonFontSize)
            }
        }
    }
}

/* ===================================================
   ABOUT SCREEN
=================================================== */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Calculator App\nAuthor: Anastasiia Prokopishena\nStudent ID number: 905588")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
