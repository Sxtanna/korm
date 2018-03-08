package com.sxtanna.korm.comp.lexer

import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.comp.Token
import com.sxtanna.korm.comp.Type
import com.sxtanna.korm.comp.Type.*
import com.sxtanna.korm.data.Data

internal class Lexer(private val input: String): Exec<List<Token>> {

    private val tokens = mutableListOf<Token>()


    override fun eval(): List<Token> {
        if (tokens.isNotEmpty()) return tokens

        val stream = CharStream()

        stream.forEach {

            when(it) {
                ' ', '\n', '\r' -> return@forEach
                ',' -> add(COMMA, ",")
                '{' -> add(BRACE_L, "{")
                '}' -> add(BRACE_R, "}")
                '[' -> add(BRACK_L, "[")
                ']' -> add(BRACK_R, "]")
                ':' -> {

                    if (tokens.isNotEmpty()) {
                        val last = tokens.last()

                        if (last.type in RETROSPECT) {
                            last.type = SYMBOL
                        }
                    }

                    add(ASSIGN, ":")

                }
                '\'', '`', '\"' -> {

                    val type = if (it == '`') COMPLEX else if (it == '\'') CHAR else TEXT

                    val data = buildString {
                        while (stream.hasNext) {
                            val next = stream.next()

                            if (next == it) {
                                if (stream.peek(-2) == '\\') {
                                    append(next)
                                    continue
                                }
                                break
                            }

                            append(next)
                        }
                    }.replace("\\\"", "\"")

                    add(type, data)
                }
                '/' -> {

                    if (stream.peek(0) == '/') { // line comment, skip this line
                        while (stream.hasNext) {
                            if (stream.next() == '\n') break
                        }
                    }
                    else if (stream.peek(0) == '*' && stream.peek(1) == '*') { // block comment, skip until end
                        while (stream.hasNext) {
                            if (stream.next() == '*' && stream.peek(0) == '/') {
                                stream.move(1)
                                break
                            }
                        }
                    }

                }
                else -> {

                    var type = SYMBOL

                    val data = buildString {

                        append(it)

                        if (it.isDigit() || it == '-') {

                            type = INT

                            while (stream.hasNext) {
                                val next = stream.next()

                                if (next.isDigit().not()) {
                                    if (next == '.') type = DEC else {
                                        stream.move(-1)
                                        break
                                    }
                                }

                                append(next)
                            }

                        }
                        else {

                            while (stream.hasNext) {
                                val next = stream.next()

                                if (next.isLetterOrDigit().not() && next !in arrayOf('_', '-')) {
                                    stream.move(-1)
                                    break
                                }

                                append(next)
                            }

                        }

                    }

                    if (data in arrayOf("true", "false")) {
                        type = BOOL
                    }

                    add(type, data)
                }
            }
        }

        return tokens
    }


    private fun add(type: Type, data: String) {
        tokens += Token(Data(data, type))
    }


    private inner class CharStream {

        private var index = 0

        val hasNext: Boolean
            get() = index < input.length


        fun move(amount: Int) {
            index += amount
        }

        fun peek(amount: Int = 1): Char? {
            return input.getOrNull(index + amount)
        }

        fun next(amount: Int = 1): Char {
            val char = input[index]

            move(amount)

            return char
        }

        fun forEach(block: (Char) -> Unit) {
            val chars = input.toCharArray()

            while (hasNext) block(chars[index++])
        }

    }


    private companion object Data {

        val RETROSPECT = setOf(INT, DEC, BOOL, CHAR, TEXT)

    }

}