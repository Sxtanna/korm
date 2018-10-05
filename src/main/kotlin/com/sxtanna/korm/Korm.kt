package com.sxtanna.korm

import com.sxtanna.korm.base.KormPuller
import com.sxtanna.korm.base.KormPusher
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.RefType
import com.sxtanna.korm.data.Reflect
import com.sxtanna.korm.reader.KormReader
import com.sxtanna.korm.writer.KormWriter
import java.io.*
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.superclasses

@Suppress("UNCHECKED_CAST")
class Korm(val reader: KormReader = KormReader(), val writer: KormWriter = KormWriter()) {
    constructor(reader: KormReader): this(reader, KormWriter())
    constructor(writer: KormWriter): this(KormReader(), writer)

    init {
        reader.korm = this
        writer.korm = this
    }


    private val pullers = mutableMapOf<KClass<*>, KormPuller<*>>()
    private val pushers = mutableMapOf<KClass<*>, KormPusher<*>>()


    // writer

    /**
     * Push any [data] to it's [Korm] representation as a [String]
     *
     * @param data The instance to push
     * @return The [data] as a Korm string
     */
    fun push(data: Any): String {
        return this.writer.write(data)
    }

    /**
     * Push any [data] to it's [Korm] representation into a [File]
     *
     * **Wraps [file] in a [FileWriter]**
     *
     * @param data The instance to push.
     * @param file The file to write it to.
     */
    fun push(data: Any, file: File) {
        this.writer.write(data, file)
    }

    /**
     * Push any [data] to it's [Korm] representation into a [Writer]
     *
     * @param data The instance to push
     * @param writer The writer to write it to
     */
    fun push(data: Any, writer: Writer) {
        this.writer.write(data, writer)
    }

    /**
     * Push any [data] to it's [Korm] representation into an [OutputStream]
     *
     * **Wraps [stream] in an [OutputStreamWriter]**
     *
     * @param data The instance to push
     * @param stream The output stream to write it to
     */
    fun push(data: Any, stream: OutputStream) {
        this.writer.write(data, stream)
    }


    // reader
    fun pull(file: File): KormReader.ReaderContext {
        return this.reader.read(file)
    }

    fun pull(text: String): KormReader.ReaderContext {
        return this.reader.read(text)
    }

    fun pull(reader: Reader): KormReader.ReaderContext {
        return this.reader.read(reader)
    }

    fun pull(stream: InputStream, charset: Charset = Charset.defaultCharset()): KormReader.ReaderContext {
        return this.reader.read(stream, charset)
    }


    // directly
    fun <T : Any> pull(text: String, to: KClass<T>): T {
        return checkNotNull(pull(text).to(to)) { "Result is null" }
    }

    inline fun <reified T : Any> pull(text: String): T {
        return pull(text, T::class)
    }

    fun <T : Any> pull(reader: Reader, to: KClass<T>): T {
        return checkNotNull(pull(reader).to(to)) { "Result is null" }
    }

    inline fun <reified T : Any> pull(reader: Reader): T {
        return pull(reader, T::class)
    }


    // with reference
    fun <T : Any> pullRef(text: String, to: RefType<T>): T {
        return checkNotNull(pull(text).toRef(to)) { "Result is null" }
    }

    inline fun <reified T : Any> pullRef(text: String): T {
        return pullRef(text, RefType.of())
    }

    fun <T : Any> pullRef(reader: Reader, to: RefType<T>): T {
        return checkNotNull(pull(reader).toRef(to)) { "Result is null" }
    }

    inline fun <reified T : Any> pullRef(reader: Reader): T {
        return pullRef(reader, RefType.of())
    }


    // pull / push
    fun <T : Any> pullWith(clazz: KClass<T>, puller: KormPuller<T>) {
        pullers[clazz] = puller
    }

    fun <T : Any> pushWith(clazz: KClass<T>, pusher: KormPusher<T>) {
        pushers[clazz] = pusher
    }

    /**
     * Dear lord, please don't call [KormPuller.pull]
     */
    inline fun <reified T : Any> pullWith(crossinline pull: KormPuller<T>.(reader: KormReader.ReaderContext, types: MutableList<KormType>) -> T?) {
        pullWith(T::class, object : KormPuller<T> {

            override fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): T? {
                return pull.invoke(this, reader, types)
            }

        })
    }

    /**
     * Dear lord, please don't call [KormPusher.push]
     */
    inline fun <reified T : Any> pushWith(crossinline push: KormPusher<T>.(writer: KormWriter.WriterContext, data: T?) -> Unit) {
        pushWith(T::class, object : KormPusher<T> {

            override fun push(writer: KormWriter.WriterContext, data: T?) {
                push.invoke(this, writer, data)
            }

        })
    }


    fun <T : Any> pullerOf(clazz: KClass<T>): KormPuller<T>? {
        return pullers[clazz] as? KormPuller<T>
    }

    fun <T : Any> pusherOf(clazz: KClass<T>): KormPusher<T>? {
        return pushers[clazz] as? KormPusher<T>
    }

}