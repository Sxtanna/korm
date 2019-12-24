package com.sxtanna.korm.data

import com.sxtanna.korm.comp.Type

data class Data(@Transient val inputData: Any, val type: Type)
{
	
	val data: Any
		get() = type.mapValue(inputData)
	
	/**
	 * @return true if [data] is a [Number]
	 */
	fun isNumber(): Boolean
	{
		return data is Number
	}
	
	/**
	 * @return true if [data] is a [String]
	 */
	fun isString(): Boolean
	{
		return data is String
	}
	
	/**
	 * @return true if [data] is a [Boolean]
	 */
	fun isBoolean(): Boolean
	{
		return data is Boolean
	}
	
	/**
	 * @return [data] as a [Number] or null if it's not
	 */
	fun asNumber(): Number?
	{
		return data as? Number
	}
	
	/**
	 * @return [data] as a [String] or null if it's not
	 */
	fun asString(): String?
	{
		return data as? String
	}
	
	/**
	 * @return [data] as a [Boolean] or null if it's not
	 */
	fun asBoolean(): Boolean?
	{
		return data as? Boolean
	}
	
	
	override fun toString(): String
	{
		return "Data<${data::class.simpleName}, $type>[$data]"
	}
	
	
	companion object
	{
		
		/**
		 * empty [Data] for usage as object headers
		 */
		internal fun none() = Data("", Type.TEXT)
		
	}
	
}