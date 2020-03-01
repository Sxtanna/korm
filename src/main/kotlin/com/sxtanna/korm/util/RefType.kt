package com.sxtanna.korm.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A cute little JVM Type Erasure workaround
 */
abstract class RefType<T>
{
	
	/**
	 * The full type [T]
	 */
	fun type(): Type
	{
		return (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0]
	}
	
	
	companion object
	{
		
		/**
		 * Create a new RefType using an anonymous class
		 */
		inline fun <reified T : Any?> of() = object : RefType<T>()
		{}
		
	}
	
}