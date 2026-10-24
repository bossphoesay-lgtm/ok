package com.example.simplecalculator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : Activity() {
    private lateinit var display: TextView
    private var current = "0"
    private var storedValue: Double? = null
    private var pendingOperator: String? = null
    private var startNewNumber = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 24, 16, 16)
            setBackgroundColor(Color.rgb(18, 22, 32))
        }

        display = TextView(this).apply {
            text = "0"
            textSize = 42f
            setTextColor(Color.WHITE)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(12, 12, 12, 20)
            maxLines = 2
        }
        root.addView(display, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.2f
        ))

        val grid = GridLayout(this).apply {
            columnCount = 4
            rowCount = 5
            useDefaultMargins = true
        }

        val keys = arrayOf(
            "AC", "±", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "−",
            "1", "2", "3", "+",
            "⌫", "0", ".", "="
        )

        for (key in keys) {
            val button = Button(this).apply {
                text = key
                textSize = 22f
                isAllCaps = false
                setTextColor(Color.WHITE)
                setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    when (key) {
                        "÷", "×", "−", "+", "=" -> Color.rgb(35, 115, 230)
                        "AC", "±", "%", "⌫" -> Color.rgb(75, 83, 100)
                        else -> Color.rgb(43, 49, 63)
                    }
                ))
                setOnClickListener { press(key) }
            }
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            grid.addView(button, params)
        }
        root.addView(grid, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 2.4f
        ))

        setContentView(root)
    }

    private fun press(key: String) {
        when (key) {
            "AC" -> {
                current = "0"
                storedValue = null
                pendingOperator = null
                startNewNumber = true
            }
            "⌫" -> {
                if (!startNewNumber) {
                    current = if (current.length <= 1 || (current.length == 2 && current.startsWith("-"))) {
                        "0"
                    } else current.dropLast(1)
                }
            }
            "±" -> {
                if (current != "0") current = if (current.startsWith("-")) current.drop(1) else "-$current"
            }
            "%" -> {
                current = format((current.toDoubleOrNull() ?: 0.0) / 100.0)
                startNewNumber = true
            }
            "+", "−", "×", "÷" -> {
                storedValue = current.toDoubleOrNull() ?: 0.0
                pendingOperator = key
                startNewNumber = true
            }
            "=" -> calculate()
            "." -> {
                if (startNewNumber) {
                    current = "0."
                    startNewNumber = false
                } else if (!current.contains(".")) {
                    current += "."
                }
            }
            else -> {
                if (key.length == 1 && key[0].isDigit()) {
                    current = if (startNewNumber || current == "0") key else current + key
                    startNewNumber = false
                }
            }
        }
        display.text = current
    }

    private fun calculate() {
        val left = storedValue ?: return
        val right = current.toDoubleOrNull() ?: return
        val result = when (pendingOperator) {
            "+" -> left + right
            "−" -> left - right
            "×" -> left * right
            "÷" -> if (right == 0.0) {
                current = "Cannot divide by zero"
                display.text = current
                storedValue = null
                pendingOperator = null
                startNewNumber = true
                return
            } else left / right
            else -> return
        }
        current = format(result)
        storedValue = null
        pendingOperator = null
        startNewNumber = true
    }

    private fun format(value: Double): String {
        if (!value.isFinite()) return "Error"
        return BigDecimal.valueOf(value).setScale(8, RoundingMode.HALF_UP)
            .stripTrailingZeros().toPlainString()
    }
}
