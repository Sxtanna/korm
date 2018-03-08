package com.sxtanna.korm.reader

import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.base.KormPuller
import com.sxtanna.korm.comp.Type.COMPLEX
import com.sxtanna.korm.comp.lexer.Lexer
import com.sxtanna.korm.comp.typer.Typer
import com.sxtanna.korm.data.Data
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.RefType
import com.sxtanna.korm.data.Reflect
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.data.custom.KormList
import java.io.*
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.createInstance

class KormReader {


    fun read(file: File): ReaderContext {
        return read(FileReader(file))
    }

    fun read(text: String): ReaderContext {
        return read(StringReader(text))
    }

    fun read(stream: InputStream, charset: Charset = Charset.defaultCharset()): ReaderContext {
        return read(InputStreamReader(stream, charset))
    }

    fun read(reader: Reader): ReaderContext {
        return ReaderContext(reader).apply { eval() }
    }


    inner class ReaderContext internal constructor(private val reader: Reader) : Exec<Unit> {

        private val types = mutableListOf<KormType>()


        override fun eval() {
            val input = reader.buffered().use { it.readText() }

            val lexer = Lexer(input)
            val typer = Typer(lexer.eval())

            types += typer.eval()
        }


        fun viewTypes() = types.toList()


        // directly to a class
        fun <T : Any> to(clazz: KClass<T>): T? {
            val clazz = Reflect.nonPrimitive(clazz)

            return when {
                Reflect.isKormType(clazz) -> {
                    val korm = when {
                        types.size > 1 -> KormType.HashType(Data.none(), types)
                        else -> types.single()
                    }

                    val data = mapKormToType(korm, clazz.java) ?: return null
                    //println("Data $data is ${data::class} | ${data::class.java.componentType}, ${(data as? Array<Any>)?.first()}")

                    Reflect.ensureIs(data, clazz)
                }
                else -> {
                    mapInstance(clazz)
                }
            }
        }

        inline fun <reified T : Any> to() = to(T::class)

        fun <T : Any> to(clazz: Class<T>): T? = to(clazz as Type) // who doesn't love enhanced Java Interop?

        fun <T : Any> to(type: Type): T? {
            val clazz = Reflect.nonPrimitive((when(type) {
                is ParameterizedType -> type.rawType
                else -> type
            } as Class<*>).kotlin)

            return when {
                Reflect.isKormType(clazz) -> {

                    val korm = when {
                        types.size > 1 -> KormType.HashType(Data.none(), types)
                        else -> types.single()
                    }


                    val data = mapKormToType(korm, type) ?: return null
                    //println("Data $data is ${data::class} | ${data::class.java.componentType}, ${(data as? Array<Any>)?.first()}")

                    Reflect.ensureIs(data, clazz) as? T
                }
                else -> {
                    mapInstance(clazz) as? T
                }
            }
        }


        fun <T : Any> toRef(type: RefType<T>): T? {
            val type = type.type()
            return to(type)
        }

        inline fun <reified T : Any> toRef() = toRef(RefType.of<T>())


        // to a list
        fun <T : Any> toList(clazz: KClass<T>): List<T> {
            val type = checkNotNull(types.singleOrNull() as? KormType.ListType) {
                "This does not represent a list"
            }

            return mapListData(type, List::class, clazz.java) as List<T>
        }

        inline fun <reified T : Any> toList() = toList(T::class)


        fun <T : Any> toListRef(ref: RefType<T>): List<T> {
            val type = checkNotNull(types.singleOrNull() as? KormType.ListType) {
                "This does not represent a list"
            }

            return mapListData(type, List::class, ref.type()) as List<T>
        }

        inline fun <reified T : Any> toListRef() = toListRef(RefType.of<T>())


        // to a hash
        fun <K : Any, V : Any> toHash(kType: KClass<K>, vType: KClass<V>): Map<K, V> {
            val type = checkNotNull(types.singleOrNull() as? KormType.HashType) {
                "This does not represent a hash"
            }

            return mapHashData(type, Map::class, kType.java, vType.java) as Map<K, V>
        }

        inline fun <reified K : Any, reified V : Any> toHash() = toHash(K::class, V::class)


        fun <K : Any, V : Any> toHashRef(kRef: RefType<K>, vRef: RefType<V>): Map<K, V> {
            val type = checkNotNull(types.singleOrNull() as? KormType.HashType) {
                "This does not represent a hash"
            }

            return mapHashData(type, Map::class, kRef.type(), vRef.type()) as Map<K, V>
        }

        inline fun <reified K : Any, reified V : Any> toHashRef() = toHashRef(RefType.of<K>(), RefType.of<V>())



        fun <T : Any> mapInstance(clazz: KClass<out T>, types: MutableList<KormType> = this.types): T? {
            //println("Mapping instance of $clazz")

            val custom = Reflect.findAnnotation<KormCustomCodec>(clazz)

            if (custom == null) {
                //println("Has no codec!")

                val instance = Reflect.newInstance(clazz) ?: return null

                val asList = Reflect.findAnnotation<KormList>(clazz)?.props?.toList()

                if (asList == null) {
                    for (field in Reflect.access(clazz)) {
                        val korm = types.find { it.key.data.toString() == field.name } ?: continue
                        types.remove(korm)

                        val data = mapKormToType(korm, field.genericType) ?: continue
                        //println("Data is $data")

                        Reflect.assign(field, instance, data)
                    }
                } else {
                    val data = (types.single() as KormType.ListType).data

                    val fields = Reflect.access(clazz).sortedBy { asList.indexOf(it.name) }

                    for ((index, field) in fields.withIndex()) {
                        field[instance] = mapDataToType(data[index], field.genericType) ?: continue
                    }

                }

                return instance
            }
            else {
                val codec = custom.codec.let { it.objectInstance ?: it.createInstance() } as KormPuller<T>
                //println("USING CODEC")

                return codec.pull(this, types)
            }
        }


        // korm mappers
        fun mapKormToType(korm: KormType, type: Type): Any? {
            //println("Mapping $korm to type $type")

            return when (korm) {
                is KormType.BaseType -> {

                    if (type is WildcardType) return mapKormToType(korm, type.upperBounds[0])

                    val custom = Reflect.findAnnotation<KormCustomCodec>((type as Class<*>).kotlin)

                    if (custom != null) return (custom.codec.let { it.objectInstance ?: it.createInstance() } as? KormPuller<Any>)?.pull(this, mutableListOf(korm))


                    mapDataToType(korm.data, type)
                }
                is KormType.ListType -> {
                    //println("MAPPING A LIST [0]")

                    when (type) {
                        is Class<*> -> {
                            //println("MAPPING A LIST [1]")

                            val custom = Reflect.findAnnotation<KormCustomCodec>(type.kotlin)

                            if (custom != null) return (custom.codec.let { it.objectInstance ?: it.createInstance() } as? KormPuller<Any>)?.pull(this, mutableListOf(korm))


                            if (type.isArray.not()) {
                                if (Reflect.findAnnotation<KormList>(type.kotlin) != null) {
                                    return mapInstance<Any>(type.kotlin, mutableListOf(korm))
                                }
                                return null
                            }

                            //println("Array type is ${type.componentType}")
                            mapList(korm.data, List::class, type.componentType)?.let { list ->
                                val list = list as List<Any>
                                Array(list.size) { list[it] }
                            }
                        }
                        is GenericArrayType -> {
                            //println("MAPPING A LIST [2]")
                            val arg = type.genericComponentType

                            mapListData(korm, List::class, arg)
                        }
                        is WildcardType -> {
                            //println("MAPPING A LIST [3]")
                            mapKormToType(korm, type.upperBounds[0])
                        }
                        else -> {
                            //println("MAPPING A LIST [4]")
                            val type = type as ParameterizedType

                            val custom = Reflect.findAnnotation<KormCustomCodec>((type.rawType as Class<*>).kotlin)

                            if (custom != null) return (custom.codec.let { it.objectInstance ?: it.createInstance() } as? KormPuller<Any>)?.pull(this, mutableListOf(korm))

                            mapListData(korm, (type.rawType as Class<*>).kotlin, type.actualTypeArguments[0])
                        }
                    }
                }
                is KormType.HashType -> {
                    //println("MAPPING A HASH [0]")

                    when (type) {
                        is Class<*> -> {
                            //println("MAPPING A HASH [1]")
                            mapInstance(type.kotlin, korm.data.toMutableList())
                        }
                        is WildcardType -> {
                            //println("MAPPING A HASH [2]")
                            mapKormToType(korm, type.upperBounds[0])
                        }
                        else -> {
                            //println("MAPPING A HASH [3]")
                            val type = type as ParameterizedType

                            val (kType, vType) = when(type.rawType as Class<*>) {
                                Pair::class.java -> {
                                    String::class.java to type.actualTypeArguments[1]
                                }
                                Map.Entry::class.java -> {
                                    String::class.java to type.actualTypeArguments[1]
                                }
                                else -> type.actualTypeArguments[0] to type.actualTypeArguments[1]
                            }

                            val data = mapHashData(korm, (type.rawType as Class<*>).kotlin, kType, vType)

                            // assign manually
                            when(type.rawType as Class<*>) {
                                Pair::class.java -> {
                                    Pair(data?.get("first"), data?.get("second"))
                                }
                                Map.Entry::class.java -> {
                                    object : Map.Entry<Any?, Any?> {
                                        override val key = data?.get("key")
                                        override val value = data?.get("value")
                                    }
                                }
                                else -> data
                            }
                        }
                    }
                }
            }
        }

        fun mapBaseData(korm: KormType.BaseType, clazz: KClass<*>): Any? {
            val custom = Reflect.findAnnotation<KormCustomCodec>(clazz)

            if (custom != null) return (custom.codec.let { it.objectInstance ?: it.createInstance() } as? KormPuller<Any>)?.pull(this, mutableListOf(korm))

            return mapData(korm.data, clazz)
        }

        fun mapListData(korm: KormType.ListType, clazz: KClass<*>, type: Type): Collection<Any>? {
            //println("mapListData: korm = [${korm}], clazz = [${clazz}], type = [${type}]")

            val data = korm.data.map {
                mapDataToType(it, type, (it as? KormType)?.key?.type == COMPLEX)
            }

            //println("Data is $data")

            return mapList(data, clazz, type)
        }

        fun mapHashData(korm: KormType.HashType, clazz: KClass<*>, kType: Type, vType: Type): Map<Any, Any>? {
            //println("mapHashData: korm = [${korm}], clazz = [${clazz}], kType = [${kType}], vType = [${vType}]")

            val data = korm.data.associate {
                mapDataToType(it.key.data, kType, it.key.type == COMPLEX) to mapKormToType(it, vType)
            }

            //println("HASH DATA IS $data")

            return mapHash(data, clazz, kType, vType)
        }


        fun mapDataToType(data: Any?, type: Type, complex: Boolean = false): Any? {
            //println("mapDataToType: data = [$data], type = [$type]")
            data ?: return null

            //println("Data is $data ${data::class} $complex")

            if (data is KormType) return mapKormToType(data, type)

            if (data is String && complex) { // ffs here we go... we gotta deserialize complex keys
                //println("TESTING FOR COMPLEX KEY")

                println("Evaluating complex key $data")

                return read(data).let { it.to<Any>(type) }.apply {

                    //println("RESOLVED DATA IS $this")

                }
            }

            when (type) {
                is Class<*> -> {
                    if (type.isInstance(data)) return data

                    return if (type.isArray) { // handle array
                        mapList(data, List::class, type.componentType)?.let { list ->
                            val list = list as List<Any>
                            Array(list.size) { list[it] }
                        }
                    } else {

                        mapData(data, type.kotlin)
                    }
                }
                is GenericArrayType -> {
                    val arg = type.genericComponentType

                    when (data) {
                        is Collection<*> -> {
                            val list = data.toList()
                            return Array(data.size) { mapDataToType(list[it], arg) }
                        }
                        is Map<*, *> -> {

                            if (data.isNotEmpty()) {
                                checkNotNull(data.keys.first() is Int) { "Cannot map a map to an array unless it's keys are Int" }
                            }

                            val data = (data as? Map<Int, Any>) ?: return null
                            val size = data.keys.sorted().last() + 1

                            return Array(size) { mapDataToType(data[it], arg) }
                        }
                    }
                }
                is WildcardType -> {
                    return mapDataToType(data, type.upperBounds[0], complex)
                }
                is ParameterizedType -> {
                    val typeArgs = type.actualTypeArguments
                    val typeType = (type.rawType as Class<*>).kotlin

                    if (typeType.isInstance(data)) return data

                    when {
                        Reflect.isSubType(typeType, Collection::class) -> {
                            check(Reflect.isListType(data::class)) { "Cannot map $data to list" }

                            return mapList(data, typeType, typeArgs[0])
                        }
                        Reflect.isSubType(typeType, Map::class) -> {
                            check(Reflect.isHashType(data::class)) { "Cannot map $data to hash" }

                            return mapHash(data, typeType, typeArgs[0], typeArgs[1])
                        }
                    }
                }
            }

            //println("Type doesn't conform at all")

            return null
        }


        // data mappers
        fun <T : Any> mapData(data: Any?, clazz: KClass<T>): T? {
            //println("mapData: data = [$data], clazz = [$clazz]")
            data ?: return null

            val clazz = Reflect.nonPrimitive(clazz)

            if (data is KormType) return mapKormToType(data, clazz.java) as? T

            when {
                clazz == UUID::class -> {
                    return clazz.cast(UUID.fromString(data as String))
                }
                clazz == String::class -> {
                    return clazz.cast(data as? String ?: data.toString())
                }
                clazz == Reflect.nonPrimitive(Char::class) -> {
                    return clazz.cast(data as? Char ?: (data as? String)?.first() ?: data.toString().first())
                }
                clazz == Reflect.nonPrimitive(Boolean::class) -> {
                    return clazz.cast(data as? Boolean)
                }
                Reflect.isSubType(clazz, Number::class) -> {
                    val number = data as? Number ?: return null

                    return clazz.cast(when (clazz) {
                                          Byte::class -> number.toByte()
                                          Short::class -> number.toShort()
                                          Int::class -> number.toInt()
                                          Long::class -> number.toLong()
                                          Float::class -> number.toFloat()
                                          Double::class -> number.toDouble()
                                          else -> number
                                      })
                }
                Reflect.isSubType(clazz, Enum::class) -> {
                    return clazz.java.enumConstants.find { (it as Enum<*>).name.equals(data.toString(), true) }
                }
            }

            //println("FAILED TO MAP DATA $data TO $clazz")

            return null

        }

        fun mapList(data: Any?, clazz: KClass<*>, type: Type): Collection<Any>? {
            //println("mapList: data = [$data], clazz = [$clazz], type = [$type]")
            data ?: return null

            val list = Reflect.findListType(clazz) ?: return null

            (data as Collection<*>).forEach {

                val data = it ?: return@forEach
                val outp = mapDataToType(data, type) ?: return@forEach

                list.add(outp)
            }

            return list
        }

        fun mapHash(data: Any?, clazz: KClass<*>, kType: Type, vType: Type): Map<Any, Any>? {
            //println("mapHash: data = [$data], clazz = [$clazz], kType = [$kType], vType = [$vType]")
            data ?: return null

            val hash = Reflect.findHashType(clazz) ?: return null

            (data as Map<*, *>).forEach { k, v ->

                val kData = k ?: return@forEach
                val vData = v ?: return@forEach

                val kOutP = mapDataToType(kData, kType) ?: return@forEach
                val vOutP = mapDataToType(vData, vType) ?: return@forEach

                hash[kOutP] = vOutP
            }

            //println("FINAL HASH IS $hash")

            return hash
        }

    }

}