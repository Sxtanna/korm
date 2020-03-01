package com.sxtanna.korm.util

import com.sxtanna.korm.data.custom.KormNull
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.ArrayDeque
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.cast
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal object Reflect
{
	
	private val unsafe: Unsafe? = try
	{
		val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
		unsafeField?.isAccessible = true
		
		unsafeField[null] as Unsafe
	}
	catch (ex: Exception)
	{
		ex.printStackTrace()
		null
	}
	
	
	private val BASE_TYPES = setOf(Number::class, Boolean::class, Char::class, String::class, UUID::class, Enum::class, Throwable::class, KormNull::class)
	
	private val LIST_TYPES = setOf(Collection::class)
	
	private val HASH_TYPES = setOf(Map::class, Map.Entry::class, Pair::class)
	
	
	private fun isOf(types: Collection<KClass<*>>, clazz: KClass<*>): Boolean
	{
		return types.any { clazz == it || isSubType(clazz, it) }
	}
	
	
	fun isKormType(clazz: KClass<*>): Boolean
	{
		return isBaseType(clazz) || isListType(clazz) || isHashType(clazz)
	}
	
	fun isBaseType(clazz: KClass<*>): Boolean
	{
		return clazz.java.isPrimitive || isOf(BASE_TYPES, clazz)
	}
	
	fun isListType(clazz: KClass<*>): Boolean
	{
		return clazz.java.isArray || isOf(LIST_TYPES, clazz)
	}
	
	fun isHashType(clazz: KClass<*>): Boolean
	{
		return (clazz.java.isArray && clazz.java.componentType.isArray) || isOf(HASH_TYPES, clazz)
	}
	
	
	fun isSubType(clazz: KClass<*>, of: KClass<*>): Boolean
	{
		return of.java.isAssignableFrom(clazz.java)
	}
	
	fun <T : Any> nonPrimitive(clazz: KClass<T>): KClass<T>
	{
		return if (clazz.java.isPrimitive.not()) clazz else clazz.javaObjectType.kotlin
	}
	
	
	fun fields(inputClazz: KClass<*>): List<Field>
	{
		val fields = mutableListOf<Field>()
		
		var clazz: Class<*>? = inputClazz.java
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
	
	fun access(inputClazz: KClass<*>): List<Property>
	{
		
		val kprops = mutableListOf<KProperty<*>>()
		val fields = fields(inputClazz).toMutableList()
		
		var jClazz: Class<*>? = inputClazz.java
		while (jClazz != null)
		{
			val props = jClazz.kotlin.declaredMemberProperties
			fields.removeIf { field -> props.any { it.name == field.name } }
			
			kprops.addAll(props)
			jClazz = jClazz.superclass
		}
		
		kprops.forEach {
			it.isAccessible = true
		}
		kprops.removeAll {
			it.javaField?.modifiers?.let { Modifier.isStatic(it) || Modifier.isTransient(it) } ?: false
		}
		
		return fields.map { Property(it.name).apply { field = it } } + kprops.map { Property(it.name).apply { kprop = it } }
	}
	
	
	fun assign(prop: Property, instance: Any, value: Any)
	{
		try
		{
			if (prop.isAccessible.not())
			{
				prop.isAccessible = true
			}
			
			prop[instance] = value
		}
		catch (ex: Exception)
		{
			ex.printStackTrace()
		}
	}
	
	
	fun <T : Any> newInstance(clazz: KClass<T>): T?
	{
		return try
		{
			clazz.constructors.find { it.parameters.isEmpty() }?.call() ?: unsafe?.allocateInstance(clazz.java) as? T
		}
		catch (ex: Exception)
		{
			null
		}
	}
	
	inline fun <reified T : Annotation> findAnnotation(on: KAnnotatedElement): T?
	{
		return on.findAnnotation()
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
	
	
	fun findListType(clazz: KClass<*>): MutableCollection<Any?>?
	{
		return when
		{
			clazz.isSubclassOf(Set::class)      ->
			{
				LinkedHashSet()
			}
			clazz.isSubclassOf(List::class)     ->
			{
				LinkedList()
			}
			clazz.isSubclassOf(Queue::class)    ->
			{
				ArrayDeque()
			}
			isSubType(clazz, Collection::class) ->
			{
				newInstance(clazz) as? MutableCollection<Any?>
			}
			else                                ->
			{
				null
			}
		}
	}
	
	fun findHashType(clazz: KClass<*>): MutableMap<Any?, Any?>?
	{
		return when
		{
			clazz == Map::class || clazz == Pair::class || clazz == Map.Entry::class ->
			{
				LinkedHashMap()
			}
			isSubType(clazz, Map::class)                                             ->
			{
				newInstance(clazz) as? MutableMap<Any?, Any?>
			}
			else                                                                     ->
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
	
	
	private val arrayTypes = setOf(ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, FloatArray::class, DoubleArray::class, BooleanArray::class, CharArray::class)
	
	fun <T : Any> ensureIs(data: Any, clazz: KClass<T>): T
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
					ByteArray::class    ->
					{
						ByteArray(array.size) { array[it] as Byte }
					}
					ShortArray::class   ->
					{
						ShortArray(array.size) { array[it] as Short }
					}
					IntArray::class     ->
					{
						IntArray(array.size) { array[it] as Int }
					}
					LongArray::class    ->
					{
						LongArray(array.size) { array[it] as Long }
					}
					FloatArray::class   ->
					{
						FloatArray(array.size) { array[it] as Float }
					}
					DoubleArray::class  ->
					{
						DoubleArray(array.size) { array[it] as Double }
					}
					CharArray::class    ->
					{
						CharArray(array.size) { array[it] as Char }
					}
					BooleanArray::class ->
					{
						BooleanArray(array.size) { array[it] as Boolean }
					}
					else                ->
					{
						array
					}
				}
				
				return clazz.cast(value)
			}
		}
		
		return clazz.cast(data)
	}
	
	
	fun nextSuperClasses(clazz: KClass<*>): List<KClass<*>>
	{
		return clazz.superclasses
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
		
		inline operator fun <reified A : Annotation> get(annotation: KClass<A>): A?
		{
			return kprop?.let { findAnnotation<A>(it) } ?: field?.getAnnotation(annotation.java)
		}
		
	}
	
}