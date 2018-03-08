package com.sxtanna.korm.writer

import com.sxtanna.korm.base.Exec
import com.sxtanna.korm.base.KormPusher
import com.sxtanna.korm.data.Reflect
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.data.custom.KormList
import com.sxtanna.korm.writer.base.Options
import com.sxtanna.korm.writer.base.WriterOptions
import java.io.*
import java.util.*
import kotlin.reflect.full.createInstance

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

        fun writeNewLine() {
            writer.write("\n")
        }


        // write types
        fun writeList(list: List<Any?>) {
            writer.write("[")

            if (list.isEmpty()) writer.write(" ")
            else {
                list.forEachIndexed { i, it ->
                    if (it == null) return@forEachIndexed

                    val clazz = it::class
                    val kormT = Reflect.isKormType(clazz)


                    if (i == 0) {
                        if (options.listEntryOnNewLine) {
                            writeNewLine()
                            indentMore()
                            writeIndent() // if issues arise, the change was `if (kormT) writeIndent()`
                        } else if (kormT.not() && options.complexListEntryOnNewLine) {
                            writeNewLine()
                            indentMore()
                            writeIndent()
                        }
                    }

                    writeData(it, listed = true)

                    if (i < list.lastIndex) {
                        writeComma()

                        if (options.listEntryOnNewLine) {
                            writeNewLine()
                            writeIndent() // if issues arise, the change was `if (kormT) writeIndent()`
                        } else if (kormT.not() && options.complexListEntryOnNewLine) {
                            writeNewLine()
                            writeIndent()
                        } else {
                            writer.write(" ")
                        }

                        return@forEachIndexed
                    }

                    if (options.listEntryOnNewLine) {
                        if (list.size > 1 && options.trailingCommas) writeComma()
                        writeNewLine()
                        if (i == list.lastIndex) {
                            indentLess()
                            writeIndent()
                        }
                    } else if (kormT.not()) {
                        if (options.complexListEntryOnNewLine) {
                            if (list.size > 1 && options.trailingCommas) writeComma()
                            writeNewLine()
                        }
                        if (i == list.lastIndex) {
                            indentLess()
                            if (options.complexListEntryOnNewLine) writeIndent()
                        }
                    }
                }
            }

            writer.write("]")
        }

        fun writeHash(hash: Map<Any?, Any?>) {
            val entries = hash.entries

            var cur = 0
            val max = entries.size - 1

            writer.write("{")

            if (entries.isNotEmpty()) {
                indentMore()

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) writeNewLine()
                } else {
                    if (options.hashEntryOnNewLine) writeNewLine()
                }
            } else {
                writer.write(" ")
            }

            entries.forEach {

                val name = it.key
                val data = it.value

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) writeIndent() else if (cur == 0) writer.write(" ")
                } else {
                    if (options.hashEntryOnNewLine) writeIndent() else if (cur == 0) writer.write(" ")
                }

                writeName(name ?: "null")
                writeData(data ?: "null", true)

                if (cur++ < max) {
                    if (options.commaAfterHashEntry) writeComma()

                    if (writingName) {
                        if (options.complexKeyEntryOnNewLine) writeNewLine() else writer.write(" ")
                    } else {
                        if (options.hashEntryOnNewLine) writeNewLine() else writer.write(" ")
                    }
                }
            }

            if (entries.isNotEmpty()) {
                if (options.hashEntryOnNewLine && hash.size > 1 && options.trailingCommas) writeComma()
                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) writeNewLine() else writer.write(" ")
                } else {
                    if (options.hashEntryOnNewLine) writeNewLine() else writer.write(" ")
                }
                indentLess()
                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) writeIndent()
                } else {
                    if (options.hashEntryOnNewLine) writeIndent()
                }
            }

            writer.write("}")
        }

        fun writeBase(inst: Any, name: Boolean = false) {
            when (inst) {
                is Char -> {
                    writer.write("'")
                    writer.write(inst.toString())
                    writer.write("'")
                }
                is Enum<*>, is Number, is Boolean -> {
                    writer.write(inst.toString())
                }
                is CharSequence, is UUID -> {

                    val string = inst.toString()
                    val complex = string.any { it.isLetterOrDigit().not() }

                    if (name) {
                        if (complex) writer.write("`")
                        writer.write(inst.toString())
                        if (complex) writer.write("`")
                    } else {
                        writer.write("\"")
                        writer.write(inst.toString())
                        writer.write("\"")
                    }
                }
                else -> {
                    if (name) writer.write("`")
                    writeData(inst)
                    if (name) writer.write("`")
                }
            }
        }


        // backing functions
        fun writeName(inst: Any) {
            nameCount++
            writeBase(inst, true)
            nameCount--

            writer.write(":")
            if (options.spaceAfterAssign) writer.write(" ")
        }

        fun writeData(inst: Any, named: Boolean = false, listed: Boolean = false) {
            val clazz = inst::class

            val custom = Reflect.findAnnotation<KormCustomCodec>(clazz)

            if (custom == null) {
                when {
                    Reflect.isBaseType(clazz) -> writeBase(inst)
                    Reflect.isHashType(clazz) -> writeHash(Reflect.toHashable(inst) ?: return)
                    Reflect.isListType(clazz) -> writeList(Reflect.toListable(inst) ?: return)
                    else -> { // write class fields as a hash

                        val asList = Reflect.findAnnotation<KormList>(inst::class)

                        if (asList == null) {

                            if (named.not() && listed.not()) {
                                if (writingName) {
                                    if (options.complexKeyEntryOnNewLine) writeIndent()
                                } else {
                                    if (options.hashEntryOnNewLine) writeIndent()
                                }
                            }

                            writer.write("{")

                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) writeNewLine() else writer.write(" ")
                            } else {
                                if (options.hashEntryOnNewLine) writeNewLine() else writer.write(" ")
                            }

                            writeFields(inst)

                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) writeNewLine() else writer.write(" ")
                            } else {
                                if (options.hashEntryOnNewLine) writeNewLine() else writer.write(" ")
                            }
                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) writeIndent()
                            } else {
                                if (options.hashEntryOnNewLine) writeIndent()
                            }
                            writer.write("}")
                        } else {
                            writeFields(inst, asList.props.toList())
                        }
                    }
                }
            }
            else {
                val codec = custom.codec.let { it.objectInstance ?: it.createInstance() } as KormPusher<Any>
                codec.push(inst, this)
            }
        }

        fun writeFields(inst: Any, props: List<String>? = null) {
            val clazz = inst::class

            val custom = Reflect.findAnnotation<KormCustomCodec>(clazz)

            if (custom == null) {

                val fields = Reflect.access(inst::class)
                //println("Fields of ${inst::class} are $fields")

                if (props != null) {
                    writeList(props.map { name -> fields.find { it.name == name }?.get(inst) })
                } else {
                    if (fields.isNotEmpty()) indentMore()

                    for ((index, field) in fields.withIndex()) {

                        val name = field.name
                        val data = field[inst] ?: continue

                        if (writingName) {
                            if (options.complexKeyEntryOnNewLine) writeIndent()
                        } else {
                            if (options.hashEntryOnNewLine) writeIndent()
                        }

                        writeName(name)
                        writeData(data, true)

                        if (index < fields.lastIndex) {
                            if (options.commaAfterHashEntry) writeComma()

                            if (writingName) {
                                if (options.complexKeyEntryOnNewLine) writeNewLine() else writer.write(" ")
                            } else {
                                if (options.hashEntryOnNewLine) writeNewLine() else writer.write(" ")
                            }
                        }
                    }

                    if (fields.isNotEmpty()) {
                        if (fields.size > 1 && options.trailingCommas) writeComma()
                        indentLess()
                    }
                }
            }
            else {
                val codec = custom.codec.let { it.objectInstance ?: it.createInstance() } as KormPusher<Any>
                codec.push(inst, this)
            }


            // legacy
            /*val fields = Reflect.fields(inst::class)
            println("Fields of ${inst::class} are $fields")

            if (fields.isNotEmpty()) indentMore()

            for ((index, field) in fields.withIndex()) {

                val name = field.name
                val data = field[inst] ?: continue

                if (writingName) {
                    if (options.complexKeyEntryOnNewLine) writeIndent()
                } else {
                    if (options.hashEntryOnNewLine) writeIndent()
                }

                writeName(name)
                writeData(data, true)

                if (index < fields.lastIndex) {
                    if (options.commaAfterHashEntry) writeComma()

                    if (writingName) {
                        if (options.complexKeyEntryOnNewLine) writeNewLine() else writer.write(" ")
                    } else {
                        if (options.hashEntryOnNewLine) writeNewLine() else writer.write(" ")
                    }
                }
            }

            if (fields.isNotEmpty()) {
                if (fields.size > 1 && options.trailingCommas) writeComma()
                indentLess()
            }*/
        }

    }

}