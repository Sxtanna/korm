package com.sxtanna.korm

import com.sxtanna.korm.comp.Token
import com.sxtanna.korm.comp.Type
import com.sxtanna.korm.comp.lexer.Lexer
import com.sxtanna.korm.data.Data
import org.junit.Assert.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LexerTests {

    private lateinit var lexer: Lexer


    @Test
    internal fun testBasicSymbol() {
        lex("key1")

        assertTokens(Token(Data("key1", Type.SYMBOL)))
    }

    @Test
    internal fun testComplexSymbol() {
        lex("`complex key`: 21")

        assertTokens(Token(Data("complex key", Type.COMPLEX)),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("21", Type.INT)))
    }


    @Test
    internal fun testBraces() {
        lex("{}}{")

        assertTokens(Token(Data("{", Type.BRACE_L)),
                     Token(Data("}", Type.BRACE_R)),
                     Token(Data("}", Type.BRACE_R)),
                     Token(Data("{", Type.BRACE_L)))
    }

    @Test
    internal fun testBracks() {
        lex("[]][")

        assertTokens(Token(Data("[", Type.BRACK_L)),
                     Token(Data("]", Type.BRACK_R)),
                     Token(Data("]", Type.BRACK_R)),
                     Token(Data("[", Type.BRACK_L)))
    }


    @Test
    internal fun testAssign() {
        lex(":")

        assertTokens(Token(Data(":", Type.ASSIGN)))
    }


    @Test
    internal fun testQuoted() {
        lex("'A' 'B' 'C' \"Hello World\" \"Goodbye World\"")

        assertTokens(Token(Data("A", Type.CHAR)),
                     Token(Data("B", Type.CHAR)),
                     Token(Data("C", Type.CHAR)),
                     Token(Data("Hello World", Type.TEXT)),
                     Token(Data("Goodbye World", Type.TEXT)))
    }

    @Test
    internal fun testNumbers() {
        lex("1 2 10 12 -4 -16 1.6 14.678 -10.5 -100")

        assertTokens(Token(Data("1", Type.INT)),
                     Token(Data("2", Type.INT)),
                     Token(Data("10", Type.INT)),
                     Token(Data("12", Type.INT)),
                     Token(Data("-4", Type.INT)),
                     Token(Data("-16", Type.INT)),
                     Token(Data("1.6", Type.DEC)),
                     Token(Data("14.678", Type.DEC)),
                     Token(Data("-10.5", Type.DEC)),
                     Token(Data("-100", Type.INT)))
    }


    /**
     * Retrospect is the lexer's ability to look backwards and reassign the type of a token
     *
     * (probably shouldn't be done in the lexer, but idc)
     */
    @Test
    internal fun testRetrospect() {
        lex("key: 'A' 1: 'B' 1.4: 'C' true: 'D' 'E': 'F' \"Good\": 'H'")

        assertTokens(Token(Data("key", Type.SYMBOL)),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("A", Type.CHAR)),

                     Token(Data("1", Type.INT), Type.SYMBOL),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("B", Type.CHAR)),

                     Token(Data("1.4", Type.DEC), Type.SYMBOL),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("C", Type.CHAR)),

                     Token(Data("true", Type.BOOL), Type.SYMBOL),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("D", Type.CHAR)),

                     Token(Data("E", Type.CHAR), Type.SYMBOL),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("F", Type.CHAR)),

                     Token(Data("Good", Type.TEXT), Type.SYMBOL),
                     Token(Data(":", Type.ASSIGN)),
                     Token(Data("H", Type.CHAR)))
    }

    @Test
    internal fun testEscapes() {

        lex(""" "Hello \"World\"" """)
        println(lexer.eval())

    }


    private fun lex(input: String) {
        lexer = Lexer(input)
    }

    private fun assertTokens(vararg tokens: Token) {
        val actual = lexer.eval().toTypedArray()

        println("Comparing - \nExpect:${tokens.joinToString("\n")}\n\nActual:${actual.joinToString("\n")}")

        assertArrayEquals(tokens, actual)
    }

}