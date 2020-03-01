package com.sxtanna.korm.legacy

import com.sxtanna.korm.comp.Token
import com.sxtanna.korm.comp.Type
import com.sxtanna.korm.comp.lexer.Lexer
import com.sxtanna.korm.data.Data
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LexerTests
{
	
	private lateinit var lexer: Lexer
	
	
	@Test
	internal fun testBasicSymbol()
	{
		lex("key1")
		
		assertTokens(Token(Data("key1", Type.SYMBOL)))
	}
	
	@Test
	internal fun testComplexSymbol()
	{
		lex("`complex key`: 21")
		
		assertTokens(Token(0, 0, Data("complex key", Type.COMPLEX)),
		             Token(0, 13, Data(":", Type.ASSIGN)),
		             Token(0, 15, Data("21", Type.INT)))
	}
	
	
	@Test
	internal fun testBraces()
	{
		lex("{}}{")
		
		assertTokens(Token(0, 0, Data("{", Type.BRACE_L)),
		             Token(0, 1, Data("}", Type.BRACE_R)),
		             Token(0, 2, Data("}", Type.BRACE_R)),
		             Token(0, 3, Data("{", Type.BRACE_L)))
	}
	
	@Test
	internal fun testBracks()
	{
		lex("[]][")
		
		assertTokens(Token(0, 0, Data("[", Type.BRACK_L)),
		             Token(0, 1, Data("]", Type.BRACK_R)),
		             Token(0, 2, Data("]", Type.BRACK_R)),
		             Token(0, 3, Data("[", Type.BRACK_L)))
	}
	
	
	@Test
	internal fun testAssign()
	{
		lex(":")
		
		assertTokens(Token(Data(":", Type.ASSIGN)))
	}
	
	
	@Test
	internal fun testQuoted()
	{
		lex("'A' 'B' 'C' \"Hello World\" \"Goodbye World\"")
		
		assertTokens(Token(0, 0, Data("A", Type.CHAR)),
		             Token(0, 4, Data("B", Type.CHAR)),
		             Token(0, 8, Data("C", Type.CHAR)),
		             Token(0, 12, Data("Hello World", Type.TEXT)),
		             Token(0, 26, Data("Goodbye World", Type.TEXT)))
	}
	
	@Test
	internal fun testNumbers()
	{
		lex("1 2 10 12 -4 -16 1.6 14.678 -10.5 -100")
		
		assertTokens(Token(0, 0, Data("1", Type.INT)),
		             Token(0, 2, Data("2", Type.INT)),
		             Token(0, 4, Data("10", Type.INT)),
		             Token(0, 7, Data("12", Type.INT)),
		             Token(0, 10, Data("-4", Type.INT)),
		             Token(0, 13, Data("-16", Type.INT)),
		             Token(0, 17, Data("1.6", Type.DEC)),
		             Token(0, 21, Data("14.678", Type.DEC)),
		             Token(0, 28, Data("-10.5", Type.DEC)),
		             Token(0, 34, Data("-100", Type.INT)))
	}
	
	
	/**
	 * Retrospect is the lexer's ability to look backwards and reassign the type of a token
	 *
	 * (probably shouldn't be done in the lexer, but idc)
	 */
	@Test
	internal fun testRetrospect()
	{
		lex("key: 'A' 1: 'B' 1.4: 'C' true: 'D' 'E': 'F' \"Good\": 'H'")
		
		assertTokens(Token(0, 0, Data("key", Type.SYMBOL)),
		             Token(0, 3, Data(":", Type.ASSIGN)),
		             Token(0, 5, Data("A", Type.CHAR)),
		
		             Token(0, 9, Data("1", Type.INT), Type.SYMBOL),
		             Token(0, 10, Data(":", Type.ASSIGN)),
		             Token(0, 12, Data("B", Type.CHAR)),
		
		             Token(0, 16, Data("1.4", Type.DEC), Type.SYMBOL),
		             Token(0, 19, Data(":", Type.ASSIGN)),
		             Token(0, 21, Data("C", Type.CHAR)),
		
		             Token(0, 25, Data("true", Type.BOOL), Type.SYMBOL),
		             Token(0, 29, Data(":", Type.ASSIGN)),
		             Token(0, 31, Data("D", Type.CHAR)),
		
		             Token(0, 35, Data("E", Type.CHAR), Type.SYMBOL),
		             Token(0, 38, Data(":", Type.ASSIGN)),
		             Token(0, 40, Data("F", Type.CHAR)),
		
		             Token(0, 44, Data("Good", Type.TEXT), Type.SYMBOL),
		             Token(0, 50, Data(":", Type.ASSIGN)),
		             Token(0, 52, Data("H", Type.CHAR)))
	}
	
	@Test
	internal fun testEscapes()
	{
		
		lex(""" "Hello \"World\"" """)
		println(lexer.exec())
		
	}
	
	
	private fun lex(input: String)
	{
		lexer = Lexer(input)
	}
	
	private fun assertTokens(vararg tokens: Token)
	{
		val actual = lexer.exec().toTypedArray()
		
		println("Comparing - \nExpect:${tokens.joinToString("\n")}\n\nActual:${actual.joinToString("\n")}")
		
		assertArrayEquals(tokens, actual)
	}
	
}