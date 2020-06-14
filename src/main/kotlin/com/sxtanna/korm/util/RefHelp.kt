package com.sxtanna.korm.util

import com.sxtanna.korm.data.KormNull
import sun.misc.Unsafe
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.ArrayDeque
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal object RefHelp
{
	
	private val unsafe: Unsafe? = try
	{
		val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
		unsafeField.isAccessible = true
		
		unsafeField[null] as Unsafe
	}
	catch (ex: Exception)
	{
		ex.printStackTrace()
		null
	}
	
	private val wrapped = mapOf(
		Char::class.javaPrimitiveType to Char::class.javaObjectType,
		Boolean::class.javaPrimitiveType to Boolean::class.javaObjectType,
		Byte::class.javaPrimitiveType to Byte::class.javaObjectType,
		Short::class.javaPrimitiveType to Short::class.javaObjectType,
		Int::class.javaPrimitiveType to Int::class.javaObjectType,
		Long::class.javaPrimitiveType to Long::class.javaObjectType,
		Float::class.javaPrimitiveType to Float::class.javaObjectType,
		Double::class.javaPrimitiveType to Double::class.javaObjectType
	                           )
	
	
	private val BASE_TYPES = setOf(Number::class.java,
	                               String::class.java,
	                               UUID::class.java,
	                               Enum::class.java,
	                               Throwable::class.java,
	                               KormNull::class.java,
	                               *wrapped.entries.flatMap { setOf(it.key, it.value) }.filterNotNull().toTypedArray())
	
	private val LIST_TYPES = setOf(Collection::class).map(KClass<*>::java)
	
	private val HASH_TYPES = setOf(Map::class, Map.Entry::class, Pair::class).map(KClass<*>::java)
	
	
	private fun isOf(types: Collection<Class<*>>, clazz: Class<*>): Boolean
	{
		return types.any { clazz == it || isSubType(clazz, it) }
	}
	
	
	fun isKormType(clazz: Class<*>): Boolean
	{
		return isBaseType(clazz) || isListType(clazz) || isHashType(clazz)
	}
	
	fun isBaseType(clazz: Class<*>): Boolean
	{
		return clazz.isPrimitive || isOf(BASE_TYPES, clazz)
	}
	
	fun isListType(clazz: Class<*>): Boolean
	{
		return clazz.isArray || isOf(LIST_TYPES, clazz)
	}
	
	fun isHashType(clazz: Class<*>): Boolean
	{
		return (clazz.isArray && clazz.componentType.isArray) || isOf(HASH_TYPES, clazz)
	}
	
	
	fun isSubType(clazz: Class<*>, of: Class<*>): Boolean
	{
		return of.isAssignableFrom(clazz)
	}
	
	fun <T : Any> nonPrimitive(clazz: Class<T>): Class<T>
	{
		return if (!clazz.isPrimitive) clazz else requireNotNull(wrapped[clazz] as? Class<T>)
	}
	
	
	fun fields(inputClazz: Class<*>): List<Field>
	{
		val fields = mutableListOf<Field>()
		
		var clazz: Class<*>? = inputClazz
		while (clazz != null)
		{
			fields.addAll(clazz.declaredFields)
			clazz = clazz.superclass
		}
		
		fields.forEach {
			it.isAccessible = true
		}
		
		fields.removeAll {
			Modifier.isStatic(it.modifiers) || Modifier.isTransient(it.modifiers)
		}
		
		return fields
	}
	
	
	fun <T> newInstance(clazz: Class<T>): T?
	{
		return try
		{
			(clazz.constructors.find { it.parameters.isEmpty() }?.newInstance() ?: unsafe?.allocateInstance(clazz)) as? T
		}
		catch (ex: Exception)
		{
			null
		}
	}
	
	inline fun <reified T : Annotation> findAnnotation(on: AnnotatedElement): T?
	{
		return on.getAnnotation(T::class.java)
	}
	
	
	fun toListable(any: Any): List<Any?>?
	{
		try
		{
			if (any::class.java.isArray)
			{
				return (any as Array<*>).toList()
			}
		}
		catch (ex: Exception)
		{
			ex.printStackTrace()
		}
		
		try
		{
			if (any is Collection<*>)
			{
				return any.toList()
			}
		}
		catch (ex: Exception)
		{
			ex.printStackTrace()
		}
		
		return null
	}
	
	fun toHashable(any: Any): Map<Any?, Any?>?
	{
		try
		{
			if (any::class.java.isArray && any::class.java.componentType.isArray)
			{
				var index = 0
				val array = any as Array<Array<*>>
				
				return array.associateBy { index++ }
			}
		}
		catch (ex: Exception)
		{
			ex.printStackTrace()
		}
		
		
		if (any is Map<*, *>)
		{
			return any as Map<Any?, Any?>
		}
		
		if (any is Map.Entry<*, *>)
		{
			return mapOf(any.key to any.value)
		}
		
		if (any is Pair<*, *>)
		{
			return mapOf("first" to any.first, "second" to any.second)
		}
		
		return null
	}
	
	
	fun findListType(clazz: Class<*>): MutableCollection<Any?>?
	{
		return when
		{
			clazz.isAssignableFrom(Set::class.java)   ->
			{
				LinkedHashSet()
			}
			clazz.isAssignableFrom(List::class.java)  ->
			{
				LinkedList()
			}
			clazz.isAssignableFrom(Queue::class.java) ->
			{
				ArrayDeque()
			}
			isSubType(clazz, Collection::class.java)  ->
			{
				newInstance(clazz) as? MutableCollection<Any?>
			}
			else                                      ->
			{
				null
			}
		}
	}
	
	fun findHashType(clazz: Class<*>): MutableMap<Any?, Any?>?
	{
		return when
		{
			clazz in HASH_TYPES               ->
			{
				LinkedHashMap()
			}
			isSubType(clazz, Map::class.java) ->
			{
				newInstance(clazz) as? MutableMap<Any?, Any?>
			}
			else                              ->
			{
				null
			}
		}
	}
	
	fun makeSequence(data: Any): Sequence<Any?>
	{
		when (data)
		{
			is Array<*>      ->
			{
				return data.asSequence()
			}
			is Collection<*> ->
			{
				return data.asSequence()
			}
		}
		
		return emptySequence()
	}
	
	
	private val arrayTypes = setOf(ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, FloatArray::class, DoubleArray::class, BooleanArray::class, CharArray::class).map(KClass<*>::java)
	
	fun <T : Any> ensureIs(data: Any, clazz: Class<T>): T
	{
		when (clazz)
		{
			in arrayTypes ->
			{
				check(data::class.java.isArray) { "Class isn't array" }
				
				val array = data as Array<Any>
				
				@Suppress("IMPLICIT_CAST_TO_ANY")
				val value = when (clazz)
				{
					ByteArray::class.java    ->
					{
						ByteArray(array.size) { array[it] as Byte }
					}
					ShortArray::class.java   ->
					{
						ShortArray(array.size) { array[it] as Short }
					}
					IntArray::class.java     ->
					{
						IntArray(array.size) { array[it] as Int }
					}
					LongArray::class.java    ->
					{
						LongArray(array.size) { array[it] as Long }
					}
					FloatArray::class.java   ->
					{
						FloatArray(array.size) { array[it] as Float }
					}
					DoubleArray::class.java  ->
					{
						DoubleArray(array.size) { array[it] as Double }
					}
					CharArray::class.java    ->
					{
						CharArray(array.size) { array[it] as Char }
					}
					BooleanArray::class.java ->
					{
						BooleanArray(array.size) { array[it] as Boolean }
					}
					else                     ->
					{
						array
					}
				}
				
				return clazz.cast(value)
			}
		}
		
		return clazz.cast(data)
	}
	
	
	data class Property(val name: String)
	{
		
		var field: Field? = null
		var kprop: KProperty<*>? = null
		
		
		val genericType: Type
			get() = this.field?.genericType ?: this.kprop?.returnType?.javaType ?: Any::class.java
		
		var isAccessible: Boolean
			get() = this.field?.isAccessible ?: this.kprop?.isAccessible ?: true
			set(value)
			{
				this.field?.isAccessible = value
				this.kprop?.isAccessible = value
			}
		
		val isInnerRef: Boolean
			get() = this.field?.isSynthetic ?: kprop?.javaField?.isSynthetic ?: false
		
		
		operator fun get(inst: Any): Any?
		{
			return field?.get(inst) ?: kprop?.getter?.call(inst)
		}
		
		operator fun set(inst: Any, data: Any): Unit?
		{
			return field?.set(inst, data) ?: (kprop as? KMutableProperty<*>)?.setter?.call(inst, data) ?: kprop?.javaField?.set(inst, data)
		}
		
		/*inline operator fun <reified A : Annotation> get(annotation: KClass<A>): A?
		{
			return kprop?.let { findAnnotation<A>(it) } ?: field?.getAnnotation(annotation.java)
		}*/
		
	}
	
}