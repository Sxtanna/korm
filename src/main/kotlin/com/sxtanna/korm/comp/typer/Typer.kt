package com.sxtanna.korm.comp.typer

import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.comp.Token
import com.sxtanna.korm.comp.Type.*
import com.sxtanna.korm.data.Data
import com.sxtanna.korm.data.KormType

internal class Typer(private val input: List<Token>) : Exec<List<KormType>>
{
	
	private val types = mutableListOf<KormType>()
	
	
	override fun exec(): List<KormType>
	{
		val tokens = input.listIterator()
		
		while (tokens.hasNext())
		{
			val token = tokens.next()
			
			when (token.type)
			{
				BOOL            ->
				{
					add(KormType.BaseType(Data.none(), parseBool(token)))
				}
				INT, DEC        ->
				{
					add(KormType.BaseType(Data.none(), parseNumber(token)))
				}
				CHAR, TEXT      ->
				{
					add(KormType.BaseType(Data.none(), parseQuoted(token)))
				}
				SYMBOL, COMPLEX ->
				{
					add(tokens.parseAssign(token))
				}
				BRACE_L         ->
				{
					add(KormType.HashType(Data.none(), tokens.parseHash()))
				}
				BRACK_L         ->
				{
					add(KormType.ListType(Data.none(), tokens.parseList()))
				}
				else            ->
				{
					throw IllegalStateException("Out of place token: $token")
				}
			}
		}
		
		return types
	}
	
	
	private fun add(type: KormType)
	{
		types += type
	}
	
	
	// data parsers
	private fun parseBool(token: Token): Boolean
	{
		check(token.type == BOOL) {
			"Token isn't a boolean type: $token"
		}
		
		return checkNotNull(token.data.asBoolean())
	}
	
	private fun parseNumber(token: Token): Number
	{
		check(token.type in arrayOf(INT, DEC)) {
			"Token isn't a number type: $token"
		}
		
		return checkNotNull(token.data.asNumber())
	}
	
	private fun parseQuoted(token: Token): Any
	{
		check(token.type in arrayOf(CHAR, TEXT)) {
			"Token isn't a quoted type: $token"
		}
		
		check(token.type == TEXT || (token.data.inputData as String).length == 1) {
			"Char type has more than 1 character"
		}
		
		return token.data.data
	}
	
	
	// korm parsers
	private fun ListIterator<Token>.parseHash(): List<KormType>
	{
		val hash = mutableListOf<KormType>()
		
		while (hasNext())
		{
			val next = next()
			
			if (next.type == COMMA) continue
			if (next.type == BRACE_R) break
			
			if (next.type == SYMBOL || next.type == COMPLEX)
			{
				hash += parseAssign(next)
				continue
			}
			
			throw IllegalStateException("Out of place token: $next")
		}
		
		return hash
	}
	
	private fun ListIterator<Token>.parseList(): List<Any>
	{
		val list = mutableListOf<Any>()
		
		while (hasNext())
		{
			val next = next()
			
			if (next.type == COMMA) continue
			if (next.type == BRACK_R) break
			
			when (next.type)
			{
				BOOL       ->
				{
					list += parseBool(next)
				}
				INT, DEC   ->
				{
					list += parseNumber(next)
				}
				CHAR, TEXT ->
				{
					list += parseQuoted(next)
				}
				SYMBOL     ->
				{
					list += next.data.data
				}
				BRACE_L    ->
				{
					list += KormType.HashType(Data.none(), parseHash())
				}
				BRACK_L    ->
				{
					list += KormType.ListType(Data.none(), parseList())
				}
				else       ->
				{
					throw IllegalStateException("Out of place token: $next")
				}
			}
		}
		
		return list
	}
	
	
	private fun ListIterator<Token>.parseKeyedHash(symbol: Token): KormType.HashType
	{
		return KormType.HashType(symbol.data, parseHash())
	}
	
	private fun ListIterator<Token>.parseKeyedList(symbol: Token): KormType.ListType
	{
		return KormType.ListType(symbol.data, parseList())
	}
	
	
	private fun ListIterator<Token>.parseAssign(symbol: Token): KormType
	{
		check(hasNext() && next().type == ASSIGN) {
			"Symbol without assignment $symbol"
		}
		
		val next = next()
		
		return when (next.type)
		{
			BOOL       ->
			{
				KormType.BaseType(symbol.data, parseBool(next))
			}
			INT, DEC   ->
			{
				KormType.BaseType(symbol.data, parseNumber(next))
			}
			CHAR, TEXT ->
			{
				KormType.BaseType(symbol.data, parseQuoted(next))
			}
			SYMBOL     ->
			{
				KormType.BaseType(symbol.data, next.data.data)
			}
			BRACE_L    ->
			{
				parseKeyedHash(symbol)
			}
			BRACK_L    ->
			{
				parseKeyedList(symbol)
			}
			else       ->
			{
				throw IllegalStateException("Out of place token: $next")
			}
		}
	}
	
}