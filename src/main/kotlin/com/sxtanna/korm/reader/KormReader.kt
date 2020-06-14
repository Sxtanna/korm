package com.sxtanna.korm.reader

import com.sxtanna.korm.Korm
import com.sxtanna.korm.base.KormPuller
import com.sxtanna.korm.comp.TokenType.COMPLEX
import com.sxtanna.korm.comp.lexer.Lexer
import com.sxtanna.korm.comp.typer.Typer
import com.sxtanna.korm.data.Data
import com.sxtanna.korm.data.KormNull
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.KormType.*
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.data.custom.KormCustomPull
import com.sxtanna.korm.data.custom.KormList
import com.sxtanna.korm.util.RefHelp
import com.sxtanna.korm.util.RefType
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.lang.ClassCastException
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

/**
 * This thing literally reads from various sources and spits out korm types
 */
@Suppress("UNCHECKED_CAST", "NAME_SHADOWING", "MemberVisibilityCanBePrivate")
class KormReader
{
	
	@Transient
	internal lateinit var korm: Korm
	
	/**
	 * Creates a [ReaderContext] for the provided [reader]
	 */
	fun context(reader: Reader): ReaderContext
	{
		return ReaderContext(reader)
	}
	
	/**
	 * Creates a [FileReader] from the given [file] and executes a [ReaderContext]
	 *
	 * - Fails silently for files that don't exist, or are directories
	 */
	fun read(file: File): ReaderContext
	{
		return if (!file.exists() || file.isDirectory)
		{
			read("")
		}
		else
		{
			read(FileReader(file))
		}
	}
	
	/**
	 * Creates a [StringReader] from the given [text] and executes a [ReaderContext]
	 */
	fun read(text: String): ReaderContext
	{
		return read(StringReader(text))
	}
	
	/**
	 * Creates a [InputStreamReader] from the given [stream] using the given [charset] and executes a [ReaderContext]
	 */
	fun read(stream: InputStream, charset: Charset = Charset.defaultCharset()): ReaderContext
	{
		return read(InputStreamReader(stream, charset))
	}
	
	/**
	 * Creates a [ReaderContext] from the given [reader] and executes it
	 */
	fun read(reader: Reader): ReaderContext
	{
		return context(reader).apply(ReaderContext::exec)
	}
	
	
	/**
	 * Takes information provided by a reader and transforms it into a list of [KormType]
	 *
	 * **Routine Process**
	 * - Lex the input with [Lexer]
	 * - Type the input with [Typer]
	 * - Use list of [KormType] to create usable objects
	 *
	 */
	inner class ReaderContext internal constructor(private val reader: Reader)
	{
		
		private val types = mutableListOf<KormType>()
		private val cache = mutableMapOf<Type, Any>()
		
		
		fun exec()
		{
			val input = reader.buffered().use { it.readText() }
			if (input.isBlank())
			{
				return
			}
			
			val lexer = Lexer(input)
			val typer = Typer(lexer.exec())
			
			types += typer.exec()
		}
		
		
		fun viewTypes(): List<KormType>
		{
			return types
		}
		
		fun viewTypesAsString(): String
		{
			return viewTypes().joinToString("\n")
		}
		
		
		//region || ==== Base ==== ||
		fun <T : Any> to(clazz: KClass<T>): T?
		{
			return internalTo(clazz.java, clazz.java)
		}
		
		inline fun <reified T : Any?> to(): T?
		{
			return internalTo(T::class.java, T::class.java)
		}
		
		fun <T : Any> to(type: Type): T?
		{
			val clazz = if (type !is ParameterizedType)
			{
				type
			}
			else
			{
				type.rawType
			}
			
			return internalTo(RefHelp.nonPrimitive(clazz as Class<*>), type)
		}
		
		
		fun <T : Any> toRef(type: RefType<T>): T?
		{
			val type = type.type()
			
			val clazz = if (type !is ParameterizedType)
			{
				type
			}
			else
			{
				type.rawType
			}
			
			return internalTo(RefHelp.nonPrimitive(clazz as Class<*>), type)
		}
		
		inline fun <reified T : Any?> toRef(): T?
		{
			val type = RefType.of<T>().type()
			
			val clazz = if (type !is ParameterizedType)
			{
				type
			}
			else
			{
				type.rawType
			}
			
			return internalTo(RefHelp.nonPrimitive(clazz as Class<*>), type)
		}
		
		@JvmSynthetic
		@PublishedApi
		internal fun <T> internalTo(clazz: Class<*>, type: Type): T?
		{
			if (types.isEmpty())
			{
				return null
			}
			
			if (!RefHelp.isKormType(clazz))
			{
				return mapInstanceJ(clazz) as? T
			}
			
			val korm = if (types.size == 1)
			{
				types.single()
			}
			else
			{
				HashType(Data.none(), types)
			}
			
			return RefHelp.ensureIs(mapKormToType(korm, type) ?: return null, clazz) as? T
		}
		//endregion
		
		
		//region || ==== List ==== ||
		fun <T : Any> toList(clazz: KClass<T>): List<T>
		{
			return internalToList(clazz.java, false)
		}
		
		inline fun <reified T : Any?> toList(): List<T>
		{
			return internalToList(T::class.java, null is T)
		}
		
		
		fun <T : Any> toListRef(ref: RefType<T>): List<T>
		{
			return internalToList(ref.type(), false)
		}
		
		inline fun <reified T : Any?> toListRef(): List<T>
		{
			return internalToList(RefType.of<T>().type(), null is T)
		}
		
		
		@JvmSynthetic
		@PublishedApi
		internal fun <T> internalToList(eType: Type, nullable: Boolean): List<T>
		{
			val type = checkNotNull(types.singleOrNull() as? ListType) {
				"This does not represent a list"
			}
			
			val data = mapListData(type, List::class, eType) ?: return emptyList()
			
			return if (nullable)
			{
				data as List<T>
			}
			else
			{
				data.filterNotNull() as List<T>
			}
		}
		//endregion
		
		
		//region || ==== Hash ==== ||
		fun <K : Any, V : Any> toHash(kType: KClass<K>, vType: KClass<V>): Map<K, V>
		{
			return internalToHash(kType.java, false, vType.java, false)
		}
		
		inline fun <reified K : Any?, reified V : Any?> toHash(): Map<K, V>
		{
			return internalToHash(K::class.java, null is K, V::class.java, null is V)
		}
		
		
		fun <K : Any, V : Any> toHashRef(kRef: RefType<K>, vRef: RefType<V>): Map<K, V>
		{
			return internalToHash(kRef.type(), false, vRef.type(), false)
		}
		
		inline fun <reified K : Any?, reified V : Any?> toHashRef(): Map<K, V>
		{
			return internalToHash(RefType.of<K>().type(), null is K, RefType.of<V>().type(), null is V)
		}
		
		
		@JvmSynthetic
		@PublishedApi
		internal fun <K, V> internalToHash(kType: Type, kNullable: Boolean, vType: Type, vNullable: Boolean): Map<K, V>
		{
			val type = checkNotNull(types.singleOrNull() as? HashType) {
				"This does not represent a hash"
			}
			
			var data = mapHashData(type, Map::class, kType, vType) ?: return emptyMap()
			
			if (!kNullable)
			{
				data = data.filterKeys { it != null }
			}
			if (!vNullable)
			{
				data = data.filterValues { it != null }
			}
			
			return data as Map<K, V>
		}
		//endregion
		
		
		fun <T : Any> mapInstanceJ(clazz: Class<out T>, types: MutableList<KormType> = this.types, caller: KormPuller<*>? = null): T?
		{
			if (types.isEmpty())
			{
				return null
			}
			
			val custom = getCustomPull(clazz, caller)
			
			return if (custom != null)
			{
				custom.pull(this, types)
			}
			else
			{
				val instance = RefHelp.newInstance(clazz) ?: return null
				cache[clazz] = instance
				
				val asList = RefHelp.findAnnotation<KormList>(clazz)?.props?.toList()
				
				if (asList == null)
				{
					val fields = RefHelp.fields(clazz)
					
					for (field in fields)
					{
						if (field.isSynthetic)
						{
							field.set(instance, cache[field.genericType] ?: continue)
							continue
						}
						
						val list = if (types.size == 1 && types[0].asHash()?.key?.asString() == "")
						{
							types[0].asHash()?.data
						}
						else
						{
							types
						}
						
						val korm = list?.find { it.key.data.toString() == field.name } ?: continue
						
						if (list is MutableList)
						{
							list.remove(korm)
						}
						
						val data = mapKormToType(korm, field.genericType) ?: continue
						field.set(instance, data)
						// RefHelp.assign(field, instance, data)
					}
				}
				else
				{
					val data = (types.single() as ListType).data
					
					val fields = RefHelp.fields(clazz)
					
					for ((index, field) in fields.withIndex())
					{
						field[instance] = mapDataToType(data[index], field.genericType) ?: continue
					}
				}
				
				instance
			}
		}
		
		fun <T : Any> mapInstanceK(clazz: KClass<out T>, types: MutableList<KormType> = this.types, caller: KormPuller<*>? = null): T?
		{
			return mapInstanceJ(clazz.java, types, caller)
		}
		
		// korm mappers
		fun mapKormToType(korm: KormType, type: Type): Any?
		{
			return when (korm)
			{
				is BaseType ->
				{
					if (type is WildcardType)
					{
						return mapKormToType(korm, type.upperBounds[0])
					}
					
					val custom = getCustomPull(type as Class<*>)
					if (custom != null)
					{
						return custom.pull(this, mutableListOf(korm))
					}
					
					mapDataToType(korm.data, type)
				}
				is ListType ->
				{
					when (type)
					{
						is Class<*>         ->
						{
							val custom = getCustomPull(type)
							if (custom != null)
							{
								return custom.pull(this, mutableListOf(korm))
							}
							
							if (!type.isArray)
							{
								if (RefHelp.findAnnotation<KormList>(type) != null)
								{
									return mapInstanceJ<Any>(type, mutableListOf(korm))
								}
								
								return null
							}
							
							mapList(korm.data, List::class.java, type.componentType)?.let { list ->
								val list = list as List<Any>
								Array(list.size) { list[it] }
							}
						}
						is GenericArrayType ->
						{
							mapListData(korm, List::class, type.genericComponentType)
						}
						is WildcardType     ->
						{
							mapKormToType(korm, type.upperBounds[0])
						}
						else                ->
						{
							val type = type as ParameterizedType
							
							val custom = getCustomPull(type.rawType as Class<*>)
							if (custom != null)
							{
								return custom.pull(this, mutableListOf(korm))
							}
							
							mapListData(korm, (type.rawType as Class<*>).kotlin, type.actualTypeArguments[0])
						}
					}
				}
				is HashType ->
				{
					when (type)
					{
						is Class<*>     ->
						{
							mapInstanceJ(type, korm.data.toMutableList())
						}
						is WildcardType ->
						{
							mapKormToType(korm, type.upperBounds[0])
						}
						else            ->
						{
							val type = type as ParameterizedType
							
							val (kType, vType) = when (type.rawType as Class<*>)
							{
								Pair::class.java      ->
								{
									String::class.java to type.actualTypeArguments[1]
								}
								Map.Entry::class.java ->
								{
									String::class.java to type.actualTypeArguments[1]
								}
								else                  ->
								{
									type.actualTypeArguments[0] to type.actualTypeArguments[1]
								}
							}
							
							val data = mapHashData(korm, (type.rawType as Class<*>).kotlin, kType, vType)
							
							// assign manually
							when (type.rawType as Class<*>)
							{
								Pair::class.java      ->
								{
									Pair(data?.get("first"), data?.get("second"))
								}
								Map.Entry::class.java ->
								{
									object : Map.Entry<Any?, Any?>
									{
										override val key = data?.get("key")
										override val value = data?.get("value")
									}
								}
								else                  ->
								{
									data
								}
							}
						}
					}
				}
			}
		}
		
		// why is this not being used??
		fun mapBaseData(korm: BaseType, clazz: KClass<*>): Any?
		{
			val custom = getCustomPull(clazz.java)
			if (custom != null)
			{
				return custom.pull(this, mutableListOf(korm))
			}
			
			return mapData(korm.data, clazz)
		}
		
		fun mapListData(korm: ListType, clazz: KClass<*>, type: Type): Collection<Any?>?
		{
			val data = korm.data.map {
				mapDataToType(it, type, (it as? KormType)?.key?.type == COMPLEX)
			}
			
			return mapList(data, clazz.java, type)
		}
		
		fun mapHashData(korm: HashType, clazz: KClass<*>, kType: Type, vType: Type): Map<Any?, Any?>?
		{
			val data = korm.data.associate {
				mapDataToType(it.key.data, kType, it.key.type == COMPLEX) to mapKormToType(it, vType)
			}
			
			return mapHash(data, clazz.java, kType, vType)
		}
		
		
		fun mapDataToType(data: Any?, type: Type, complex: Boolean = false): Any?
		{
			val data = data ?: return null
			
			if (data === KormNull)
			{
				return null
			}
			
			if (data is KormType)
			{
				return mapKormToType(data, type)
			}
			
			if (data is String && complex)
			{ // ffs here we go... we gotta deserialize complex keys
				return read(data).to(type)
			}
			
			when (type)
			{
				is Class<*>          ->
				{
					if (type.isInstance(data))
					{
						return data
					}
					
					return if (type.isArray)
					{ // handle array
						mapList(data, List::class.java, type.componentType)?.let { list ->
							val list = list as List<Any>
							Array(list.size) { list[it] }
						}
					}
					else
					{
						mapData(data, type.kotlin)
					}
				}
				is GenericArrayType  ->
				{
					val arg = type.genericComponentType
					
					when (data)
					{
						is Collection<*> ->
						{
							val list = data.toList()
							return Array(data.size) { mapDataToType(list[it], arg) }
						}
						is Map<*, *>     ->
						{
							
							if (data.isNotEmpty())
							{
								checkNotNull(data.keys.first() is Int) { "Cannot map a map to an array unless it's keys are Int" }
							}
							
							val data = (data as? Map<Int, Any>) ?: return null
							val size = (data.keys.max() ?: 0) + 1
							
							return Array(size) { mapDataToType(data[it], arg) }
						}
					}
				}
				is WildcardType      ->
				{
					return mapDataToType(data, type.upperBounds[0], complex)
				}
				is ParameterizedType ->
				{
					val typeArgs = type.actualTypeArguments
					val typeType = type.rawType as Class<*>
					
					if (typeType.isInstance(data))
					{
						return data
					}
					
					when
					{
						RefHelp.isSubType(typeType, Collection::class.java) ->
						{
							check(RefHelp.isListType(data.javaClass)) {
								"Cannot map $data to list"
							}
							
							return mapList(data, typeType, typeArgs[0])
						}
						RefHelp.isSubType(typeType, Map::class.java)        ->
						{
							check(RefHelp.isHashType(data.javaClass)) {
								"Cannot map $data to hash"
							}
							
							return mapHash(data, typeType, typeArgs[0], typeArgs[1])
						}
					}
				}
			}
			
			return null
		}
		
		
		// data mappers
		fun <T : Any> mapData(data: Any?, clazz: KClass<T>): T?
		{
			data ?: return null
			
			val clazz = RefHelp.nonPrimitive(clazz.java)
			
			if (data is KormType)
			{
				return mapKormToType(data, clazz) as? T
			}
			
			val value: Any? = when
			{
				clazz == UUID::class.java                          ->
				{
					UUID.fromString(data as String)
				}
				clazz == String::class                             ->
				{
					data as? String ?: data.toString()
				}
				clazz == RefHelp.nonPrimitive(Char::class.java)    ->
				{
					data as? Char ?: (data as? String)?.first() ?: data.toString().first()
				}
				clazz == RefHelp.nonPrimitive(Boolean::class.java) ->
				{
					data as? Boolean
				}
				RefHelp.isSubType(clazz, Number::class.java)       ->
				{
					var number = data as? Number ?: return null
					
					// I hate this, change it...
					number = when (RefHelp.nonPrimitive(clazz))
					{
						Byte::class.javaObjectType   -> number.toByte()
						Short::class.javaObjectType  -> number.toShort()
						Int::class.javaObjectType    -> number.toInt()
						Long::class.javaObjectType   -> number.toLong()
						Float::class.javaObjectType  -> number.toFloat()
						Double::class.javaObjectType -> number.toDouble()
						AtomicInteger::class.java    -> AtomicInteger(number.toInt())
						AtomicLong::class.java       -> AtomicLong(number.toLong())
						else                         -> number
					}
					
					number
				}
				RefHelp.isSubType(clazz, Enum::class.java)         ->
				{
					clazz.enumConstants.find { (it as Enum<*>).name.equals(data.toString(), true) }
				}
				else                                               ->
				{
					null
				}
			}
			
			return clazz.cast(value)
		}
		
		inline fun <reified T : Any> mapData(data: Any?): T?
		{
			return mapData(data, T::class)
		}
		
		fun mapList(data: Any?, clazz: Class<*>, type: Type): Collection<Any?>?
		{
			val data = data ?: return null
			val find = RefHelp.findListType(clazz) ?: return null
			
			return RefHelp.makeSequence(data).mapTo(find)
			{
				mapDataToType(it, type)
			}
		}
		
		fun mapHash(data: Any?, clazz: Class<*>, kType: Type, vType: Type): Map<Any?, Any?>?
		{
			data ?: return null
			
			val hash = RefHelp.findHashType(clazz) ?: return null
			
			(data as Map<*, *>).forEach { (k, v) ->
				val kData = k
				val vData = v
				
				val kOutP = mapDataToType(kData, kType)
				val vOutP = mapDataToType(vData, vType)
				
				hash[kOutP] = vOutP
			}
			
			return hash
		}
		
		
		// type helpers
		inline fun <reified T : Any> KormType?.map(): T?
		{
			val clazz = T::class.java
			
			return try
			{
				clazz.cast(mapKormToType(this ?: return null, clazz))
			}
			catch (ex: ClassCastException)
			{
				null
			}
		}
		
		
		fun <T : Any> getCustomPull(clazz: Class<T>, caller: KormPuller<*>? = null): KormPuller<T>?
		{
			val stored = korm.pullerOf(clazz)
			if (stored != null)
			{
				return stored
			}
			
			val puller = RefHelp.findAnnotation<KormCustomPull>(clazz)
			if (puller != null)
			{
				return extractFrom(puller.puller)
			}
			
			val codec = RefHelp.findAnnotation<KormCustomCodec>(clazz)
			if (codec != null)
			{
				return extractFrom(codec.codec)
			}
			
			var nextSuper: Class<*>? = clazz.superclass
			
			while (nextSuper != null)
			{
				val puller = getCustomPull(nextSuper)
				
				if (caller != null && caller == puller)
				{
					return null // no stack overflows
				}
				
				if (puller != null)
				{
					return puller as? KormPuller<T>
				}
				
				nextSuper = nextSuper.superclass
			}
			
			
			return null
		}
		
		
		private fun <T : Any> extractFrom(clazz: KClass<out KormPuller<*>>): KormPuller<T>?
		{
			return try
			{
				val instance = clazz.java.getDeclaredField("INSTANCE")
				instance.isAccessible = true
				
				instance.get(null) as? KormPuller<T>
			}
			catch (ex: Exception)
			{
				return clazz.java.newInstance() as? KormPuller<T>
			}
		}
		
	}
	
}