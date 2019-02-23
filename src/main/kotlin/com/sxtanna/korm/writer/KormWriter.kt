package com.sxtanna.korm.writer

import com.sxtanna.korm.Korm
import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.base.KormPusher
import com.sxtanna.korm.data.Reflect
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.data.custom.KormCustomPush
import com.sxtanna.korm.data.custom.KormList
import com.sxtanna.korm.writer.base.Options
import com.sxtanna.korm.writer.base.WriterOptions
import java.io.*
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * This other thing literally takes any object and spits out a serialized korm representation
 */
@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
class KormWriter(private val indent: Int, private val options: WriterOptions) {
    constructor() : this(2, Options.min())

    @Transient
    internal lateinit var korm: Korm


    fun context(writer: Writer): WriterContext {
        return WriterContext(Unit, writer)
    }

    fun write(data: Any): String {
        return StringWriter().apply { write(data, this) }.toString()
    }

    fun write(data: Any, file: File) {
        write(data, FileWriter(file))
    }

    fun write(data: Any, stream: OutputStream) {
        write(data, OutputStreamWriter(stream))
    }

    fun write(data: Any, writer: Writer) {
        WriterContext(data, writer).exec()
    }

    fun build(lenient: Boolean = false, block: KormBuilder.() -> Unit): String {
        return builder(lenient, block).toString()
    }

    fun builder(lenient: Boolean = false, block: KormBuilder.() -> Unit): KormBuilder {
        return KormBuilder(lenient).apply(block)
    }


    inner class KormBuilder(private val lenient: Boolean) {

        private val text = StringWriter()
        private val cont = WriterContext(0, text)

        private val record = BuilderRecord()


        fun dsl(block: KormDSLBuilder.() -> Unit) {
            KormDSLBuilder().apply(block)
        }


        fun <T : Any> data(data: T) {
            data("", data)
        }

        fun <T : Any> data(name: String, data: T) {
            val nameIsBlank = name.isBlank()
            checkIfMoreIsAllowed(nameIsBlank)

            if (nameIsBlank.not()) {
                cont.writeIndent()
                cont.writeName(name)
                record.hasWrittenWithName = true
            }
            else {
                record.hasWrittenWithoutName = true
            }

            if (nameIsBlank) cont.writeIndent()
            cont.writeData(data, name.isNotBlank())
            cont.writeNewLine()
        }

        fun data(block: KormBuilder.() -> Unit) {
            data("", block)
        }

        fun data(name: String, block: KormBuilder.() -> Unit) {
            val nameIsBlank = name.isBlank()
            checkIfMoreIsAllowed(nameIsBlank)

            if (nameIsBlank.not()) {
                cont.writeIndent()
                cont.writeName(name)
                record.hasWrittenWithName = true
            }
            else {
                record.hasWrittenWithoutName = true
            }

            if (nameIsBlank) cont.writeIndent()
            cont.writeHashOpen()

            cont.writeNewLine()

            cont.indentMore()
            block()
            cont.indentLess()

            cont.writeIndent()
            cont.writeHashClose()
            cont.writeNewLine()
        }


        fun <T : Any?> list(name: String = "", list: List<T>) {
            list<T>(name) {
                addAll(list)
            }
        }

        fun <T : Any?> list(name: String = "", vararg list: T) {
            list<T>(name) {
                addAll(*list)
            }
        }

        fun <K : Any?, V : Any?> hash(name: String = "", hash: Map<K, V>) {
            hash<K, V>(name) {
                putAll(hash)
            }
        }


        fun <T : Any?> list(name: String = "", block: KormListBuilder<T>.() -> Unit) {
            val nameIsBlank = name.isBlank()
            checkIfMoreIsAllowed(nameIsBlank)

            if (nameIsBlank.not()) {
                cont.writeIndent()
                cont.writeName(name)
                record.hasWrittenWithName = true
            }
            else {
                record.hasWrittenWithoutName = true
            }

            val builder = KormListBuilder(block)
            if (nameIsBlank) cont.writeIndent()
            cont.writeList(builder.list)

            builder.list.clear()

            cont.writeNewLine()
        }

        fun <K : Any?, V : Any?> hash(name: String = "", block: KormHashBuilder<K, V>.() -> Unit) {
            val nameIsBlank = name.isBlank()
            checkIfMoreIsAllowed(nameIsBlank)

            if (nameIsBlank.not()) {
                cont.writeIndent()
                cont.writeName(name)
                record.hasWrittenWithName = true
            }
            else {
                record.hasWrittenWithoutName = true
            }

            val builder = KormHashBuilder(block)
            if (nameIsBlank) cont.writeIndent()
            cont.writeHash(builder.hash as Map<Any?, Any?>)

            builder.hash.clear()

            cont.writeNewLine()
        }


        private fun checkIfMoreIsAllowed(newNameBlank: Boolean) {
            if (lenient) return

            if (record.hasWrittenWithoutName) {
                throw IllegalStateException("You cannot write more data to a document with a loose key")
            }

            if (record.hasWrittenWithName && newNameBlank) {
                throw IllegalStateException("You can only write named values into this document")
            }
        }


        override fun toString(): String {
            return text.toString()
        }


        inner class KormListBuilder<T : Any?>(block: KormListBuilder<T>.() -> Unit) {

            internal val list = mutableListOf<T>()

            init {
                block()
            }


            fun add(element: T) = apply {
                this.list.add(element)
            }

            fun addAll(vararg element: T) = apply {
                this.list.addAll(element)
            }

            fun addAll(collection: Collection<T>) = apply {
                this.list.addAll(collection)
            }

        }

        inner class KormHashBuilder<K : Any?, V : Any?>(block: KormHashBuilder<K, V>.() -> Unit) {

            internal val hash = mutableMapOf<K, V>()

            init {
                block()
            }


            fun put(key: K, value: V) = apply {
                this.hash[key] = value
            }

            fun putAll(hash: Map<K, V>) = apply {
                this.hash.putAll(hash)
            }

        }



        @Suppress("RemoveRedundantBackticks")
        inner class KormDSLBuilder {

            operator fun <T : Any> String.invoke(data: T) {
                data(this, data)
            }

            operator fun <T : Any?> String.invoke(list: List<T>) {
                list(this, list)
            }

            operator fun <T : Any?> String.invoke(vararg element: T) {
                list(this, *element)
            }

            operator fun <K : Any?, V : Any?> String.invoke(vararg entry: Pair<K, V>) {
                hash(this, entry.toMap())
            }

            operator fun String.invoke(block: KormDSLBuilder.() -> Unit) {
                data(this) { block() }
            }


            infix fun <T : Any?, O : Any?> T.`_`(other: O): Pair<T, O> {
                return Pair(this, other)
            }

        }


        private inner class BuilderRecord {

            var hasWrittenWithName = false
            var hasWrittenWithoutName = false

        }

    }

    inner class WriterContext internal constructor(private val data: Any, private val writer: Writer) : Exec<Unit> {

        private var nameCount = 0

        private val writingName: Boolean
            get() = nameCount > 0


        private var currentIndent = 0


        override fun exec() {
            if (Reflect.isKormType(data::class)) {
                writeData(data)
            } else {
                indentLess() // hot fix
                writeFields(data, Reflect.findAnnotation<KormList>(data::class)?.props?.toList())
            }

            writer.flush()
            writer.close()
        }


        // util functions
        fun indentLess() {
            currentIndent -= indent
        }

        fun indentMore() {
            currentIndent += indent
        }


        fun writeIndent() {
            writer.write(" ".repeat(currentIndent.coerceAtLeast(0)))
        }

        fun writeComma() {
            writer.write(",")
        }

        fun writeSpace() {
            writer.write(" ")
        }

        fun writeNewLine() {
            writer.write("\n")
        }


        fun writeComplexTick() {
            writer.write("`")
        }

        fun writeSingleQuote() {
            writer.write("'")
        }

        fun writeDoubleQuote() {
            writer.write("\"")
        }


        fun writeListOpen() {
            writer.write("[")
        }

        fun writeListClose() {
            writer.write("]")
        }

        fun writeHashOpen() {
            writer.write("{")
        }

        fun writeHashClose() {
            writer.write("}")
        }


        // write types
        fun writeList(list: List<Any?>) {
            writeListOpen()

            if (list.isEmpty()) {
                writeSpace()
            }
            else {
                list.forEachIndexed { i, it ->
                    if (it == null) return@forEachIndexed

                    val kormType = Reflect.isKormType(it::class)

                    if (i == 0) {
                        // wtf is the purpose of this???
                        if (options.listEntryOnNewLine) {
                            writeNewLine()
                            indentMore()
                            writeIndent() // if issues arise, the change was `if (kormType) writeIndent()`
                        } else if (kormType.not() && options.complexListEntryOnNewLine) {
                            writeNewLine()
                            indentMore()
                            writeIndent()
                        }
                    }

                    writeData(it, listed = true)

                    if (i < list.lastIndex) {
                        writeComma()

                        // wtf is the purpose of this??? pt. 2
                        if (options.listEntryOnNewLine) {
                            writeNewLine()
                            writeIndent() // if issues arise, the change was `if (kormType) writeIndent()`
                        } else if (kormType.not() && options.complexListEntryOnNewLine) {
                            writeNewLine()
                            writeIndent()
                        } else {
                            writeSpace()
                        }

                        return@forEachIndexed
                    }

                    if (options.listEntryOnNewLine) {
                        if (list.size > 1 && options.trailingCommas) {
                            writeComma()
                        }

                        writeNewLine()

                        if (i == list.lastIndex) {
                            indentLess()
                            writeIndent()
                        }
                    } else if (kormType.not()) {
                        if (options.complexListEntryOnNewLine) {
                            if (list.size > 1 && options.trailingCommas) {
                                writeComma()
                            }

                            writeNewLine()
                        }
                        if (i == list.lastIndex) {
                            indentLess()

                            if (options.complexListEntryOnNewLine) {
                                writeIndent()
                            }
                        }
                    }
                }
            }

            writeListClose()
        }

        fun writeHash(hash: Map<Any?, Any?>) {
            val entries = hash.entries

            var cur = 0
            val max = entries.size - 1

            writeHashOpen()

            if (entries.isNotEmpty()) {
                indentMore()

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) {
                        writeNewLine()
                    }
                } else {
                    if (options.hashEntryOnNewLine) {
                        writeNewLine()
                    }
                }
            } else {
                writeSpace()
            }

            entries.forEach {

                val name = it.key
                val data = it.value

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) {
                        writeIndent()
                    } else if (cur == 0) {
                        writeSpace()
                    }
                } else {
                    if (options.hashEntryOnNewLine) {
                        writeIndent()
                    } else if (cur == 0) {
                        writeSpace()
                    }
                }

                writeName(name ?: "null")
                writeData(data ?: "null", true)

                if (cur++ < max) {
                    if (options.commaAfterHashEntry) {
                        writeComma()
                    }

                    if (writingName) {
                        if (options.complexKeyEntryOnNewLine) {
                            writeNewLine()
                        } else {
                            writeSpace()
                        }
                    } else {
                        if (options.hashEntryOnNewLine) {
                            writeNewLine()
                        } else {
                            writeSpace()
                        }
                    }
                }
            }

            if (entries.isNotEmpty()) {
                if (options.trailingCommas && (options.hashEntryOnNewLine && hash.size > 1)) {
                    writeComma()
                }

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) {
                        writeNewLine()
                    } else {
                        writeSpace()
                    }
                } else {
                    if (options.hashEntryOnNewLine) {
                        writeNewLine()
                    } else {
                        writeSpace()
                    }
                }

                indentLess()

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) {
                        writeIndent()
                    }
                } else {
                    if (options.hashEntryOnNewLine) {
                        writeIndent()
                    }
                }
            }

            writeHashClose()
        }

        fun writeBase(inst: Any, name: Boolean = false) {
            when (inst) {
                is Char -> {
                    writeSingleQuote()
                    writer.write(inst.toString())
                    writeSingleQuote()
                }
                is Enum<*>, is Number, is Boolean -> {
                    writer.write(inst.toString())
                }
                is CharSequence, is UUID -> {
                    val string = inst.toString()
                    val quoted = name.not() || string.any { it.isWhitespace() } || string.any { it == '\'' || it == '"' }

                    if (quoted) {
                        writeDoubleQuote()
                    }

                    writer.write(string.replace("\"", "\\\""))

                    if (quoted) {
                        writeDoubleQuote()
                    }
                }
                else -> {
                    if (name) {
                        writeComplexTick()
                    }

                    writeData(inst)

                    if (name) {
                        writeComplexTick()
                    }
                }
            }
        }


        // backing functions
        fun writeName(inst: Any) {
            nameCount++
            writeBase(inst, true)
            nameCount--

            writer.write(":")

            if (options.spaceAfterAssign) {
                writeSpace()
            }
        }

        fun writeData(inst: Any, named: Boolean = false, listed: Boolean = false) {
            val clazz = inst::class

            @Suppress("UNCHECKED_CAST")
            val custom = getCustomPush(clazz) as? KormPusher<Any>

            if (custom != null) custom.push(this, inst)
            else {
                when {
                    Reflect.isBaseType(clazz) -> {
                        writeBase(inst)
                    }
                    Reflect.isHashType(clazz) -> {
                        writeHash(Reflect.toHashable(inst) ?: return)
                    }
                    Reflect.isListType(clazz) -> {
                        writeList(Reflect.toListable(inst) ?: return)
                    }
                    else -> { // write class fields as a hash

                        val asList = Reflect.findAnnotation<KormList>(inst::class)

                        if (asList == null) {

                            if (named.not() && listed.not()) {
                                if (writingName) {
                                    if (options.complexKeyEntryOnNewLine) {
                                        writeIndent()
                                    }
                                } else {
                                    if (options.hashEntryOnNewLine) {
                                        writeIndent()
                                    }
                                }
                            }

                            writeHashOpen()

                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) {
                                    writeNewLine()
                                } else {
                                    writeSpace()
                                }
                            } else {
                                if (options.hashEntryOnNewLine) {
                                    writeNewLine()
                                } else {
                                    writeSpace()
                                }
                            }

                            writeFields(inst)

                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) {
                                    writeNewLine()
                                } else {
                                    writeSpace()
                                }
                            } else {
                                if (options.hashEntryOnNewLine) {
                                    writeNewLine()
                                } else {
                                    writeSpace()
                                }
                            }
                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) {
                                    writeIndent()
                                }
                            } else {
                                if (options.hashEntryOnNewLine) {
                                    writeIndent()
                                }
                            }

                            writeHashClose()
                        } else {
                            writeFields(inst, asList.props.toList())
                        }
                    }
                }
            }
        }

        fun writeFields(inst: Any, props: List<String>? = null) {
            val clazz = inst::class

            @Suppress("UNCHECKED_CAST")
            val custom = getCustomPush(clazz) as? KormPusher<Any>

            if (custom != null) {
                custom.push(this, inst)
            }
            else {

                val fields = Reflect.access(inst::class).filter { it.isInnerRef.not() }

                if (props != null) {
                    writeList(props.map { name -> fields.find { it.name == name }?.get(inst) })
                } else {
                    if (fields.isNotEmpty()) {
                        indentMore()
                    }

                    for ((index, field) in fields.withIndex()) {

                        val name = field.name
                        val data = field[inst] ?: continue

                        if (writingName) {
                            if (options.complexKeyEntryOnNewLine) {
                                writeIndent()
                            }
                        } else {
                            if (options.hashEntryOnNewLine) {
                                writeIndent()
                            }
                        }

                        writeName(name)
                        writeData(data, true)

                        if (index < fields.lastIndex) {
                            if (options.commaAfterHashEntry) {
                                writeComma()
                            }

                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) {
                                    writeNewLine()
                                } else {
                                    writeSpace()
                                }
                            } else {
                                if (options.hashEntryOnNewLine) {
                                    writeNewLine()
                                } else {
                                    writeSpace()
                                }
                            }
                        }
                    }

                    if (fields.isNotEmpty()) {
                        if (options.trailingCommas && fields.size > 1) {
                            writeComma()
                        }

                        indentLess()
                    }
                }
            }
        }


        fun <T : Any> getCustomPush(clazz: KClass<T>): KormPusher<T>? {
            val storedPusher = korm.pusherOf(clazz)
            if (storedPusher != null) {
                return storedPusher
            }

            val pusher = Reflect.findAnnotation<KormCustomPush>(clazz)
            if (pusher != null) {
                return extractFrom(pusher.pusher)
            }

            val codec = Reflect.findAnnotation<KormCustomCodec>(clazz)
            if (codec != null) {
                return extractFrom(codec.codec)
            }

            return null
        }


        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> extractFrom(clazz: KClass<out KormPusher<*>>): KormPusher<T>? {
            return clazz.let { it.objectInstance ?: it.createInstance() } as? KormPusher<T>
        }

    }

}