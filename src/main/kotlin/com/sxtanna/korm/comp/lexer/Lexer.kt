package com.sxtanna.korm.comp.lexer

import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.comp.Token
import com.sxtanna.korm.comp.Type
import com.sxtanna.korm.comp.Type.*
import com.sxtanna.korm.data.Data

internal class Lexer(private val input: String) : Exec<List<Token>>
{
	
	private var line = 0
	private var char = 0
	
	private val tokens = mutableListOf<Token>()
	
	
	override fun exec(): List<Token>
	{
		if (tokens.isNotEmpty()) return tokens
		
		val stream = CharStream()
		
		stream.forEach {
			when (it)
			{
				' '             ->
				{
					char++
				}
				'\n'            ->
				{
					newLine()
				}
				'\r'            ->
				{
					// do nothing
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
						
						if (last.type in RETROSPECT)
						{
							last.type = SYMBOL
						}
					}
					
					add(ASSIGN, ":")
					
				}
				'\'', '`', '\"' ->
				{
					
					val type = if (it == '`') COMPLEX else if (it == '\'') CHAR else TEXT
					
					val data = buildString {
						while (stream.hasNext)
						{
							val next = stream.next()
							
							if (next == it)
							{
								if (stream.peek(-2) == '\\')
								{
									append(next)
									continue
								}
								break
							}
							
							append(next)
						}
					}.replace("\\\"", "\"")
					
					add(type, data)
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
					
					val data = buildString {
						
						append(it)
						
						if (it.isDigit() || it == '-')
						{
							
							type = INT
							
							while (stream.hasNext)
							{
								val next = stream.next()
								
								if (next.isDigit().not())
								{
									
									if (next == ']' || next == ',' || next == ' ' || next == ':' || next == '\n')
									{
										stream.move(-1)
										break
									}
									
									if (next == '.') type = DEC
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
					
					if (data in arrayOf("true", "false"))
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
	
	
	private fun add(type: Type, data: String)
	{
		tokens += Token(line, char, Data(data, type))
		
		char += data.length
	}
	
	private fun StringBuilder.readSymbol(stream: CharStream)
	{
		while (stream.hasNext)
		{
			val next = stream.next()
			
			if (next.isLetterOrDigit().not() && next !in arrayOf('_', '-', '.'))
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
		
		val hasNext: Boolean
			get() = index < input.length
		
		
		fun move(amount: Int)
		{
			index += amount
		}
		
		fun peek(amount: Int = 1): Char?
		{
			return input.getOrNull(index + amount)
		}
		
		fun next(amount: Int = 1): Char
		{
			val char = input[index]
			
			move(amount)
			
			return char
		}
		
		fun forEach(block: (Char) -> Unit)
		{
			val chars = input.toCharArray()
			
			while (hasNext) block(chars[index++])
		}
		
	}
	
	
	private companion object Data
	{
		
		val RETROSPECT = setOf(INT, DEC, BOOL, CHAR, TEXT)
		
	}
	
}