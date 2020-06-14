package com.sxtanna.korm.comp

enum class TokenType
{
	
	/**
	 * Represents a whole number, either Byte, Short, Int, or Long
	 */
	INT
	{
		
		override fun mapValue(input: Any): Any
		{
			return (input as? Number)?.toLong() ?: input.toString().toLong()
		}
		
	},
	/**
	 * Represents a decimal number, either Float or Double
	 */
	DEC
	{
		
		override fun mapValue(input: Any): Any
		{
			return (input as? Number)?.toDouble() ?: input.toString().toDouble()
		}
		
	},
	/**
	 * Represents a boolean value, either true or false
	 */
	BOOL
	{
		
		override fun mapValue(input: Any): Any
		{
			return (input as? Boolean) ?: input.toString().toBoolean()
		}
		
	},
	/**
	 * Represents a single character, ex. 'A'
	 */
	CHAR
	{
		
		override fun mapValue(input: Any): Any
		{
			return (input as? Char) ?: input.toString().first()
		}
		
	},
	/**
	 * Represents a collection of characters, ex. "String"
	 */
	TEXT,
	/**
	 * It's... a comma... ','
	 */
	COMMA,
	
	/**
	 * Represents the opening of a map/object '{'
	 */
	BRACE_L,
	/**
	 * Represents the closing of a map/object '}'
	 */
	BRACE_R,
	
	/**
	 * Represents the opening of a list '['
	 */
	BRACK_L,
	/**
	 * Represents the closing of a list ']'
	 */
	BRACK_R,
	
	/**
	 * Connects a key and a value
	 */
	ASSIGN,
	/**
	 * Represents the key portion of a 'Key: Value' pair
	 */
	SYMBOL,
	/**
	 * Represents a complex key
	 */
	COMPLEX;
	
	
	internal open fun mapValue(input: Any): Any
	{
		return input
	}
	
}