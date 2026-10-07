package com.tuapp.calculadora

class CalculatorLogic {
    var display = "0"
    var expression = ""

    private var firstNumber: Double? = null
    private var currentOperation: String? = null
    private var isNewNumber = true
    private var hasCalculated = false

    private fun formatNumber(number: Double): String {
        return if (number.isNaN()) {
            "Error"
        } else if (number % 1.0 == 0.0) {
            number.toLong().toString()
        } else {
            number.toString()
        }
    }

    fun onNumberClick(number: String): String {
        if (hasCalculated) {
            display = "0"
            expression = ""
            firstNumber = null
            currentOperation = null
            hasCalculated = false
        }

        if (isNewNumber || display == "0") {
            display = number
            isNewNumber = false
        } else {
            display += number
        }

        if (currentOperation != null && firstNumber != null) {
            val firstStr = formatNumber(firstNumber!!)
            expression = "$firstStr $currentOperation $display"
        } else {
            expression = ""
        }

        return display
    }

    fun onOperatorClick(operator: String): String {
        val currentNum = display.toDoubleOrNull() ?: 0.0

        if (firstNumber != null && currentOperation != null && !isNewNumber && !hasCalculated) {
            val intermediate = calculate(firstNumber!!, currentNum, currentOperation!!)
            display = formatNumber(intermediate)
            firstNumber = intermediate
        } else {
            firstNumber = currentNum
        }

        currentOperation = operator
        isNewNumber = true
        hasCalculated = false

        val firstStr = formatNumber(firstNumber!!)
        expression = "$firstStr $operator"

        return display
    }

    fun onEqualsClick(): String {
        if (firstNumber == null || currentOperation == null) {
            return display
        }

        val secondNumber = display.toDoubleOrNull() ?: 0.0
        val firstStr = formatNumber(firstNumber!!)
        val secondStr = formatNumber(secondNumber)

        expression = "$firstStr $currentOperation $secondStr ="

        val result = calculate(firstNumber!!, secondNumber, currentOperation!!)
        display = formatNumber(result)

        firstNumber = null
        currentOperation = null
        isNewNumber = true
        hasCalculated = true

        return display
    }

    private fun calculate(num1: Double, num2: Double, op: String): Double {
        return when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            "%" -> num1 * (num2 / 100.0)
            else -> num2
        }
    }

    fun onClearClick(): String {
        display = "0"
        expression = ""
        firstNumber = null
        currentOperation = null
        isNewNumber = true
        hasCalculated = false
        return display
    }

    fun onDeleteClick(): String {
        if (hasCalculated) {
            return onClearClick()
        }

        if (display.length > 1) {
            display = display.dropLast(1)
        } else {
            display = "0"
            isNewNumber = true
        }

        if (currentOperation != null && firstNumber != null) {
            val firstStr = formatNumber(firstNumber!!)
            expression = if (display == "0" && isNewNumber) {
                "$firstStr $currentOperation"
            } else {
                "$firstStr $currentOperation $display"
            }
        } else {
            expression = ""
        }

        return display
    }
}