package com.sxtanna.korm.data

import com.sxtanna.korm.data.KormType.*

/**
 * The three basic key->value types used in Korm
 *
 * - [BaseType]
 * - [ListType]
 * - [HashType]
 */
sealed class KormType
{
	
	abstract val key: Data
	
	
	/**
	 * Attempts to cast this [KormType] to a [BaseType]
	 */
	fun asBase() = this as? BaseType
	
	/**
	 * Attempts to cast this [KormType] to a [ListType]
	 */
	fun asList() = this as? ListType
	
	/**
	 * Attempts to cast this [KormType] to a [HashType]
	 */
	fun asHash() = this as? HashType
	
	
	/**
	 * Represents the most basic key->value
	 *
	 * ```key: "value"```
	 */
	data class BaseType(override val key: Data, val data: Any) : KormType()
	{
		
		override fun toString(): String
		{
			val text =
				"""
				|Base<${data::class.simpleName ?: "Unknown"}>
				|[
				|	k=$key
				|	v=$data
				|]
				""".trimMargin()
			return text
		}
		
		
		fun dataAsString(): String
		{
			return data.toString()
		}
		
		fun dataAsInt(): Int?
		{
			return data as? Int
		}
		
		fun dataAsIntOr(default: Int): Int
		{
			return dataAsInt() ?: default
		}
		
		fun dataAsLong(): Long?
		{
			return data as? Long
		}
		
		fun dataAsLongOr(default: Long): Long
		{
			return dataAsLong() ?: default
		}
		
		fun dataAsFloat(): Float?
		{
			return data as? Float
		}
		
		fun dataAsFloatOr(default: Float): Float
		{
			return dataAsFloat() ?: default
		}
		
		fun dataAsDouble(): Double?
		{
			return data as? Double
		}
		
		fun dataAsDoubleOr(default: Double): Double
		{
			return dataAsDouble() ?: default
		}
		
	}
	
	/**
	 * Represents a key with a list as it's value
	 *
	 * ```key: ["v0", "v1", "v3"]```
	 */
	data class ListType(override val key: Data, val data: List<Any>) : KormType()
	{
		
		override fun toString(): String
		{
			val list = if (data.none { it is KormType })
			{
				data.toString()
			}
			else
			{
				data.joinToString(", ", "\n---", "\n---")
			}
			val text =
				"""
				|List<${data::class.simpleName}>
				|[
				|	k=$key
				|	v=$list
				|]
				""".trimMargin()
			return text
		}
		
		
		fun forEach(function: (Any) -> Unit)
		{
			this.data.forEach(function)
		}
		
	}
	
	/**
	 * Represents a key with a hash as it's value
	 *
	 * ```
	 * key: {
	 *   key0: "value0"
	 *   key1: "value1"
	 * }
	 * ```
	 */
	data class HashType(override val key: Data, val data: List<KormType>) : KormType()
	{
		
		override fun toString(): String
		{
			val text =
				"""
				|Hash<$key>
				|{
				|${data.joinToString("\n")}
				|}
				""".trimMargin()
			return text
		}
		
		
		fun forEach(function: (KormType) -> Unit)
		{
			this.data.forEach(function)
		}
		
	}
	
}