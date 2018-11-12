package com.sxtanna.korm.data

import com.sxtanna.korm.util.Ex
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.cast
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

@Suppress("UNCHECKED_CAST")
internal object Reflect {

    private val unsafe: Unsafe?

    init {

        var temp: Unsafe?

        try {
            val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
            unsafeField?.isAccessible = true

            temp = unsafeField[null] as Unsafe
        } catch (ex: Exception) {
            ex.printStackTrace()

            temp = null
        }

        unsafe = temp
    }


    private val BASE_TYPES = setOf(Number::class, Boolean::class, Char::class, String::class, UUID::class, Enum::class)

    private val LIST_TYPES = setOf(Collection::class)

    private val HASH_TYPES = setOf(Map::class, Map.Entry::class, Pair::class)


    private fun isOf(types: Collection<KClass<*>>, clazz: KClass<*>): Boolean {
        return types.any { clazz == it || isSubType(clazz, it) }
    }


    fun isKormType(clazz: KClass<*>): Boolean {
        return isBaseType(clazz) || isListType(clazz) || isHashType(clazz)
    }

    fun isBaseType(clazz: KClass<*>): Boolean {
        return clazz.java.isPrimitive || isOf(BASE_TYPES, clazz)
    }

    fun isListType(clazz: KClass<*>): Boolean {
        return clazz.java.isArray || isOf(LIST_TYPES, clazz)
    }

    fun isHashType(clazz: KClass<*>): Boolean {
        return (clazz.java.isArray && clazz.java.componentType.isArray) || isOf(HASH_TYPES, clazz)
    }


    fun isSubType(clazz: KClass<*>, of: KClass<*>): Boolean {
        return of.java.isAssignableFrom(clazz.java)
    }

    fun <T : Any> nonPrimitive(clazz: KClass<T>): KClass<T> {
        return if (clazz.java.isPrimitive.not()) clazz else clazz.javaObjectType.kotlin
    }


    fun fields(inputClazz: KClass<*>): List<Field> {
        val fields = mutableListOf<Field>()

        var clazz: Class<*>? = inputClazz.java
        while (clazz != null) {
            fields.addAll(clazz.declaredFields)
            clazz = clazz.superclass
        }

        fields.removeAll { Modifier.isStatic(it.modifiers) || Modifier.isTransient(it.modifiers) }
        fields.forEach { it.isAccessible = true }

        return fields
    }

    fun access(inputClazz: KClass<*>): List<Property> {

        val fields = fields(inputClazz)
        val kprops = mutableListOf<KProperty<*>>()

        var jClazz: Class<*>? = inputClazz.java
        while (jClazz != null) {
            kprops.addAll(jClazz.kotlin.declaredMemberProperties.filter { prop -> fields.none { it.name == prop.name } })
            jClazz = jClazz.superclass
        }

        kprops.removeAll { it.javaField?.modifiers?.let { Modifier.isStatic(it) || Modifier.isTransient(it) } ?: false }

        return fields.map { Property(it.name).apply { field = it } } + kprops.map { Property(it.name).apply { kprop = it } }
    }


    fun assign(prop: Property, instance: Any, value: Any) {
        try {
            if (prop.isAccessible.not()) prop.isAccessible = true

            prop[instance] = value
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    fun <T : Any> newInstance(clazz: KClass<T>): T? {
        return try {
            clazz.constructors.find { it.parameters.isEmpty() }?.call() ?: unsafe?.allocateInstance(clazz.java) as? T
        } catch (ex: Exception) {
            null
        }
    }

    fun <T : Annotation> findAnnotation(on: KClass<*>, clazz: KClass<T>): T? {
        return on.java.getAnnotation(clazz.java)
    }

    inline fun <reified T : Annotation> findAnnotation(on: KClass<*>) = findAnnotation(on, T::class)


    fun toListable(any: Any): List<Any?>? {
        try {
            if (any::class.java.isArray) {
                return (any as Array<*>).toList()
            }
        } catch (ex: Exception) {
            Ex.printException(ex, "Failed to convert array data to a list", any::class, any)
        }

        try {
            if (any is Collection<*>) {
                return any.toList()
            }
        } catch (ex: Exception) {
            Ex.printException(ex, "Failed to convert collection data to a list", any::class, any)
        }

        return null
    }

    fun toHashable(any: Any): Map<Any?, Any?>? {
        try {
            if (any::class.java.isArray && any::class.java.componentType.isArray) {
                val array = any as Array<Array<*>>

                var index = 0
                return array.associate { index++ to it }
            }
        } catch (ex: Exception) {
            Ex.printException(ex, "Failed to convert 2D array data to a map", any::class, any::class.java.componentType, any)
        }


        if (any is Map<*, *>) {
            return any as Map<Any?, Any?>
        }

        if (any is Map.Entry<*, *>) {
            return mapOf(any.key to any.value)
        }

        if (any is Pair<*, *>) {
            return mapOf("first" to any.first, "second" to any.second)
        }



        return null
    }


    fun findListType(clazz: KClass<*>): MutableCollection<Any>? {
        return when {
            clazz == Set::class -> LinkedHashSet()
            clazz == List::class || clazz == Collection::class -> LinkedList()
            clazz == Queue::class -> ArrayDeque()
            Reflect.isSubType(clazz, Set::class) || Reflect.isSubType(clazz, List::class) || Reflect.isSubType(clazz, Queue::class) -> {
                Reflect.newInstance(clazz) as? MutableCollection<Any> //throw IllegalStateException("Idk how to create $clazz :(")
            }
            else -> null //throw IllegalStateException("Idk which collection impl to use for $clazz :(")
        }
    }

    fun findHashType(clazz: KClass<*>): MutableMap<Any, Any>? {
        return when {
            clazz == Map::class || clazz == Pair::class || clazz == Map.Entry::class -> LinkedHashMap()
            Reflect.isSubType(clazz, Map::class) -> {
                Reflect.newInstance(clazz) as? MutableMap<Any, Any> //throw IllegalStateException("Idk how to create $clazz :(")
            }
            else -> null //throw IllegalStateException("Idk which hash impl to use for $clazz :(")
        }
    }


    private val arrayTypes = setOf(ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, FloatArray::class, DoubleArray::class, BooleanArray::class, CharArray::class)

    fun <T : Any> ensureIs(data: Any, clazz: KClass<T>): T {
        //println("Ensuring $data is $clazz")

        when (clazz) {
            in arrayTypes -> {
                //println("Class is in array types")
                check(data::class.java.isArray) { "Class isn't array" }

                val array = data as Array<Any>

                return clazz.cast(when (clazz) {
                    ByteArray::class -> {
                        ByteArray(array.size) { array[it] as Byte }
                    }
                    ShortArray::class -> {
                        ShortArray(array.size) { array[it] as Short }
                    }
                    IntArray::class -> {
                        IntArray(array.size) { array[it] as Int }
                    }
                    LongArray::class -> {
                        LongArray(array.size) { array[it] as Long }
                    }
                    FloatArray::class -> {
                        FloatArray(array.size) { array[it] as Float }
                    }
                    DoubleArray::class -> {
                        DoubleArray(array.size) { array[it] as Double }
                    }
                    CharArray::class -> {
                        CharArray(array.size) { array[it] as Char }
                    }
                    BooleanArray::class -> {
                        BooleanArray(array.size) { array[it] as Boolean }
                    }
                    else -> array
                })
            }
        }

        return clazz.cast(data)
    }


    data class Property(val name: String) {

        var field: Field? = null
        var kprop: KProperty<*>? = null


        val genericType: Type
            get() = this.field?.genericType ?: this.kprop?.returnType?.javaType ?: Any::class.java

        var isAccessible: Boolean
            get() = this.field?.isAccessible ?: this.kprop?.isAccessible ?: true
            set(value) {
                this.field?.isAccessible = value
                this.kprop?.isAccessible = value
            }

        val isInnerRef: Boolean
            get() = this.field?.isSynthetic ?: kprop?.javaField?.isSynthetic ?: false

        operator fun get(inst: Any): Any? {
            return field?.get(inst) ?: kprop?.getter?.call(inst)
        }

        operator fun set(inst: Any, data: Any): Unit? {
            return field?.set(inst, data) ?: (kprop as? KMutableProperty<*>)?.setter?.call(inst, data)
        }

    }

}