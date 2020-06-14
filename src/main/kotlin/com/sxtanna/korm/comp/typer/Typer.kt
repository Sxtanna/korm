package com.sxtanna.korm.comp.typer

import com.sxtanna.korm.comp.TokenData
import com.sxtanna.korm.comp.TokenType.*
import com.sxtanna.korm.data.Data
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.KormNull

internal class Typer(private val input: List<TokenData>)
{
	
	private val types = mutableListOf<KormType>()
	
	
	fun exec(): List<KormType>
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
	private fun parseBool(token: TokenData): Boolean
	{
		check(token.type == BOOL) {
			"Token isn't a boolean type: $token"
		}
		
		return checkNotNull(token.data.asBoolean())
	}
	
	private fun parseNumber(token: TokenData): Number
	{
		check(token.type in arrayOf(INT, DEC)) {
			"Token isn't a number type: $token"
		}
		
		return checkNotNull(token.data.asNumber())
	}
	
	private fun parseQuoted(token: TokenData): Any
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
	private fun ListIterator<TokenData>.parseHash(): List<KormType>
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
	
	private fun ListIterator<TokenData>.parseList(): List<Any>
	{
		val list = mutableListOf<Any>()
		
		while (hasNext())
		{
			val next = next()
			
			if (next.type == COMMA) {
				continue
			}
			if (next.type == BRACK_R)
			{
				break
			}
			
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
				COMPLEX -> {
					list += KormType.BaseType(Data.none(), KormNull)
				}
				else       ->
				{
					throw IllegalStateException("Out of place token: $next")
				}
			}
		}
		
		return list
	}
	
	
	private fun ListIterator<TokenData>.parseKeyedHash(symbol: TokenData): KormType.HashType
	{
		return KormType.HashType(symbol.data, parseHash())
	}
	
	private fun ListIterator<TokenData>.parseKeyedList(symbol: TokenData): KormType.ListType
	{
		return KormType.ListType(symbol.data, parseList())
	}
	
	
	private fun ListIterator<TokenData>.parseAssign(symbol: TokenData): KormType
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
			COMPLEX -> {
				KormType.BaseType(symbol.data, KormNull) // this should probably be something else... but it works, so who cares
			}
			else       ->
			{
				throw IllegalStateException("Out of place token: $next")
			}
		}
	}
	
}