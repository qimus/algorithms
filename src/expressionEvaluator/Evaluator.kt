package expressionEvaluator

import java.util.*
import kotlin.NoSuchElementException

enum class TokenType {
    BRACKET_LEFT, BRACKET_RIGHT,
    OP_PLUS, OP_MINUS, OP_MUL, OP_DIV,
    NUMBER
}

class Token(
    val type: TokenType,
    val value: String,
    val priority: Int = 0
) {

    override fun toString(): String {
        return "Token(type=$type, value='$value')"
    }
}

object Evaluator {

    fun eval(expression: String): Double {
        val tokens = tokenize(expression)
        val postfixTokens = infixToPostfix(tokens)
        for (token in postfixTokens) {
        //    print(token.value + " ")
        }

        return evaluatePostfix(postfixTokens)
    }

    private fun tokenize(expression: String): List<Token> {
        var pos = 0
        val tokenList: MutableList<Token> = mutableListOf()
        while (pos < expression.length) {
            var char = expression[pos]
            when (char) {
                '(' -> {
                    tokenList.add(Token(TokenType.BRACKET_LEFT, char.toString()))
                    pos++
                }
                ')' -> {
                    tokenList.add(Token(TokenType.BRACKET_RIGHT, char.toString()))
                    pos++
                }
                '*' -> {
                    tokenList.add(Token(TokenType.OP_MUL, char.toString(), 20))
                    pos++
                }
                '/' -> {
                    tokenList.add(Token(TokenType.OP_DIV, char.toString(), 20))
                    pos++
                }
                '+' -> {
                    tokenList.add(Token(TokenType.OP_PLUS, char.toString(), 10))
                    pos++
                }
                '-' -> {
                    tokenList.add(Token(TokenType.OP_MINUS, char.toString(), 10))
                    pos++
                }
                else -> {
                    if (char in '0'..'9' || char == '.') {
                        var numberStr = ""
                        do {
                            numberStr += char
                            pos++
                            if (pos >= expression.length) break
                            char = expression[pos]
                        } while (char in '0'..'9' || char == '.')
                        tokenList.add(Token(TokenType.NUMBER, numberStr))
                    } else {
                        if (char != ' ') {
                            throw EvalException("Unknown char: $char")
                        } else {
                            pos++
                        }
                    }
                }
            }
        }

        return tokenList
    }

    private fun infixToPostfix(tokens: List<Token>): List<Token> {
        val operatorStack = Stack<Token>()
        val postfix = Stack<Token>()
        val iterator = tokens.iterator()

        while (iterator.hasNext()) {
            val token = iterator.next()
            when (token.type) {
                TokenType.NUMBER -> postfix.add(token)
                TokenType.BRACKET_LEFT -> operatorStack.add(token)
                TokenType.BRACKET_RIGHT -> {
                    try {
                        while (operatorStack.last().type != TokenType.BRACKET_LEFT) {
                            postfix.add(operatorStack.pop())
                        }
                        operatorStack.pop()
                    } catch (e: NoSuchElementException) {
                        throw EvalException("Extra end parentheses found")
                    }
                }
                else -> {
                    while (!operatorStack.isEmpty() && operatorStack.last().priority >= token.priority) {
                        postfix.add(operatorStack.pop())
                    }
                    operatorStack.add(token)
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            if (operatorStack.last().type == TokenType.BRACKET_LEFT) {
                throw EvalException("Extra open parentheses found")
            }
            postfix.add(operatorStack.removeLast())
        }

        return postfix.toList()
    }

    private fun evaluatePostfix(tokens: List<Token>): Double {
        val evalStack = Stack<Double>()
        for (token in tokens) {
            when (token.type) {
                TokenType.NUMBER -> evalStack.add(token.value.toDouble())
                else -> {
                    val var1 = evalStack.pop()
                    when (token.type) {
                        TokenType.OP_PLUS -> evalStack.add(var1 + evalStack.pop())
                        TokenType.OP_MINUS -> evalStack.add(evalStack.pop() - var1)
                        TokenType.OP_MUL -> evalStack.add(var1 * evalStack.pop())
                        TokenType.OP_DIV -> evalStack.add(evalStack.pop() / var1)
                    }
                }
            }
        }

        return evalStack.last()
    }
}

fun main() {
    val expression = "1 + (112 + 9) * (41*5-1) "
    println("$expression = ${Evaluator.eval(expression)}")
}
