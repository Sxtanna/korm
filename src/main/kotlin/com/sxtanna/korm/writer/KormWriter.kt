package com.sxtanna.korm.writer

import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.base.KormPusher
import com.sxtanna.korm.data.Reflect
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.data.custom.KormCustomPush
import com.sxtanna.korm.data.custom.KormList
import com.sxtanna.korm.writer.base.Options
import com.sxtanna.korm.writer.base.WriterOptions
import java.io.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Suppress("MemberVisibilityCanBePrivate")
class KormWriter(private val indent: Int, private val options: WriterOptions) {
    constructor() : this(2, Options.min())


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
        WriterContext(data, writer).eval()
    }


    inner class WriterContext internal constructor(private val data: Any, private val writer: Writer) : Exec<Unit> {

        private var nameCount = 0

        private val writingName: Boolean
            get() = nameCount > 0


        private var currentIndent = 0


        override fun eval() {
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
                    val quoted = name.not() || string.any { it.isWhitespace() }

                    if (quoted) {
                        writeDoubleQuote()
                    }

                    writer.write(string)

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

            if (custom != null) custom.push(inst, this)
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
                custom.push(inst, this)
            }
            else {

                val fields = Reflect.access(inst::class)

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
            val puller = Reflect.findAnnotation<KormCustomPush>(clazz)
            if (puller != null) {
                return extractFrom(puller.pusher)
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