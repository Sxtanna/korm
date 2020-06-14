package com.sxtanna.korm.legacy

import com.sxtanna.korm.comp.TokenData
import com.sxtanna.korm.comp.TokenType
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
		
		assertTokens(TokenData(Data("key1", TokenType.SYMBOL)))
	}
	
	@Test
	internal fun testComplexSymbol()
	{
		lex("`complex key`: 21")
		
		assertTokens(TokenData(0, 0, Data("complex key", TokenType.COMPLEX)),
		             TokenData(0, 13, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 15, Data("21", TokenType.INT)))
	}
	
	
	@Test
	internal fun testBraces()
	{
		lex("{}}{")
		
		assertTokens(TokenData(0, 0, Data("{", TokenType.BRACE_L)),
		             TokenData(0, 1, Data("}", TokenType.BRACE_R)),
		             TokenData(0, 2, Data("}", TokenType.BRACE_R)),
		             TokenData(0, 3, Data("{", TokenType.BRACE_L)))
	}
	
	@Test
	internal fun testBracks()
	{
		lex("[]][")
		
		assertTokens(TokenData(0, 0, Data("[", TokenType.BRACK_L)),
		             TokenData(0, 1, Data("]", TokenType.BRACK_R)),
		             TokenData(0, 2, Data("]", TokenType.BRACK_R)),
		             TokenData(0, 3, Data("[", TokenType.BRACK_L)))
	}
	
	
	@Test
	internal fun testAssign()
	{
		lex(":")
		
		assertTokens(TokenData(Data(":", TokenType.ASSIGN)))
	}
	
	
	@Test
	internal fun testQuoted()
	{
		lex("'A' 'B' 'C' \"Hello World\" \"Goodbye World\"")
		
		assertTokens(TokenData(0, 0, Data("A", TokenType.CHAR)),
		             TokenData(0, 4, Data("B", TokenType.CHAR)),
		             TokenData(0, 8, Data("C", TokenType.CHAR)),
		             TokenData(0, 12, Data("Hello World", TokenType.TEXT)),
		             TokenData(0, 26, Data("Goodbye World", TokenType.TEXT)))
	}
	
	@Test
	internal fun testNumbers()
	{
		lex("1 2 10 12 -4 -16 1.6 14.678 -10.5 -100")
		
		assertTokens(TokenData(0, 0, Data("1", TokenType.INT)),
		             TokenData(0, 2, Data("2", TokenType.INT)),
		             TokenData(0, 4, Data("10", TokenType.INT)),
		             TokenData(0, 7, Data("12", TokenType.INT)),
		             TokenData(0, 10, Data("-4", TokenType.INT)),
		             TokenData(0, 13, Data("-16", TokenType.INT)),
		             TokenData(0, 17, Data("1.6", TokenType.DEC)),
		             TokenData(0, 21, Data("14.678", TokenType.DEC)),
		             TokenData(0, 28, Data("-10.5", TokenType.DEC)),
		             TokenData(0, 34, Data("-100", TokenType.INT)))
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
		
		assertTokens(TokenData(0, 0, Data("key", TokenType.SYMBOL)),
		             TokenData(0, 3, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 5, Data("A", TokenType.CHAR)),
		
		             TokenData(0, 9, Data("1", TokenType.INT), TokenType.SYMBOL),
		             TokenData(0, 10, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 12, Data("B", TokenType.CHAR)),
		
		             TokenData(0, 16, Data("1.4", TokenType.DEC), TokenType.SYMBOL),
		             TokenData(0, 19, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 21, Data("C", TokenType.CHAR)),
		
		             TokenData(0, 25, Data("true", TokenType.BOOL), TokenType.SYMBOL),
		             TokenData(0, 29, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 31, Data("D", TokenType.CHAR)),
		
		             TokenData(0, 35, Data("E", TokenType.CHAR), TokenType.SYMBOL),
		             TokenData(0, 38, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 40, Data("F", TokenType.CHAR)),
		
		             TokenData(0, 44, Data("Good", TokenType.TEXT), TokenType.SYMBOL),
		             TokenData(0, 50, Data(":", TokenType.ASSIGN)),
		             TokenData(0, 52, Data("H", TokenType.CHAR)))
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
	
	private fun assertTokens(vararg tokens: TokenData)
	{
		val actual = lexer.exec().toTypedArray()
		
		println("Comparing - \nExpect:${tokens.joinToString("\n")}\n\nActual:${actual.joinToString("\n")}")
		
		assertArrayEquals(tokens, actual)
	}
	
}