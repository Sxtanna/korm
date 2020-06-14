package com.sxtanna.korm.comp.lexer

import com.sxtanna.korm.comp.TokenData
import com.sxtanna.korm.comp.TokenType
import com.sxtanna.korm.comp.TokenType.*
import com.sxtanna.korm.data.Data

internal class Lexer(private val input: String)
{
	
	private var line = 0
	private var char = 0
	
	private val tokens = mutableListOf<TokenData>()
	
	
	fun exec(): List<TokenData>
	{
		if (tokens.isNotEmpty())
		{
			return tokens
		}
		
		val stream = CharStream()
		
		stream.forEach()
		{ c ->
			when (c)
			{
				' ',
				'\r'            ->
				{
					char++
				}
				'\n'            ->
				{
					newLine()
				}
				','             -> add(COMMA, ",")
				'{'             -> add(BRACE_L, "{")
				'}'             -> add(BRACE_R, "}")
				'['             -> add(BRACK_L, "[")
				']'             -> add(BRACK_R, "]")
				':'             ->
				{
					if (tokens.isNotEmpty())
					{
						val last = tokens.last()
						
						if (last.type in retrospect)
						{
							last.type = SYMBOL
						}
					}
					
					add(ASSIGN, ":")
				}
				'\'', '`', '\"' ->
				{
					
					val type = if (c == '`') COMPLEX else if (c == '\'') CHAR else TEXT
					
					val data = buildString()
					{
						while (stream.hasNext)
						{
							val next = stream.next()
							
							if (next != c)
							{
								append(next)
								continue
							}
							
							if (stream.peek(-2) == '\\')
							{
								append(next)
								continue
							}
							
							break
						}
					}
					
					add(type, data.replace("\\\"", "\""))
					char += 2
				}
				'/'             ->
				{
					if (stream.peek(0) == '/')
					{ // line comment, skip this line
						while (stream.hasNext)
						{
							if (stream.next() == '\n') break
						}
					}
					
					if (stream.peek(0) == '*')
					{ // block comment, skip until end
						while (stream.hasNext)
						{
							if (stream.next() == '*' && stream.peek(0) == '/')
							{
								stream.move(1)
								break
							}
						}
					}
				}
				else            ->
				{
					var type = SYMBOL
					
					val data = buildString()
					{
						append(c)
						
						if (c.isDigit() || c == '-')
						{
							
							type = INT
							
							while (stream.hasNext)
							{
								val next = stream.next()
								
								if (!next.isDigit())
								{
									
									if (next == ']' || next == ',' || next == ' ' || next == ':' || next == '\n')
									{
										stream.move(-1)
										break
									}
									
									if (next == '.')
									{
										type = DEC
									}
									else
									{
										append(next)
										
										type = SYMBOL
										readSymbol(stream)
										
										break
									}
								}
								
								append(next)
							}
							
						}
						else
						{
							readSymbol(stream)
						}
					}
					
					if (data == "true" || data == "false")
					{
						type = BOOL
					}
					
					add(type, data)
				}
			}
		}
		
		return tokens
	}
	
	
	private fun newLine()
	{
		char = 0
		line++
	}
	
	
	private fun add(type: TokenType, data: String)
	{
		tokens += TokenData(line, char, Data(data, type))
		
		char += data.length
	}
	
	private fun StringBuilder.readSymbol(stream: CharStream)
	{
		while (stream.hasNext)
		{
			val next = stream.next()
			
			if (!next.isLetterOrDigit() && next != '_' && next != '-' && next != '.')
			{
				stream.move(-1)
				break
			}
			
			append(next)
		}
	}
	
	
	private inner class CharStream
	{
		
		private var index = 0
		private val chars = input.toCharArray()
		
		val hasNext: Boolean
			get() = index < chars.size
		
		
		fun move(amount: Int)
		{
			index += amount
		}
		
		fun peek(amount: Int = 1): Char?
		{
			return chars.getOrNull(index + amount)
		}
		
		fun next(amount: Int = 1): Char
		{
			val char = chars[index]
			
			move(amount)
			
			return char
		}
		
		fun forEach(block: (Char) -> Unit)
		{
			while (hasNext)
			{
				block(chars[index++])
			}
		}
		
	}
	
	
	companion object
	{
		private val retrospect = setOf(INT, DEC, BOOL, CHAR, TEXT)
	}
	
}