package com.sxtanna.korm

import com.sxtanna.korm.data.RefType
import com.sxtanna.korm.reader.KormReader
import com.sxtanna.korm.writer.KormWriter
import java.io.*
import java.nio.charset.Charset
import kotlin.reflect.KClass

class Korm(val reader: KormReader = KormReader(), val writer: KormWriter = KormWriter()) {

    // writer
    fun push(data: Any): String {
        return this.writer.write(data)
    }

    fun push(data: Any, file: File) {
        this.writer.write(data, file)
    }

    fun push(data: Any, writer: Writer) {
        this.writer.write(data, writer)
    }

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

}