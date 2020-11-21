package com.sxtanna.korm

import com.sxtanna.korm.base.KormCodec
import com.sxtanna.korm.base.KormPuller
import com.sxtanna.korm.base.KormPusher
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.reader.KormReader
import com.sxtanna.korm.util.RefType
import com.sxtanna.korm.writer.KormWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "unused")
class Korm(val reader: KormReader = KormReader(), val writer: KormWriter = KormWriter())
{
	
	constructor(reader: KormReader) : this(reader, KormWriter())
	constructor(writer: KormWriter) : this(KormReader(), writer)
	
	init
	{
		reader.korm = this
		writer.korm = this
	}
	
	
	private val pullers = mutableMapOf<Class<*>, KormPuller<*>>()
	private val pushers = mutableMapOf<Class<*>, KormPusher<*>>()
	
	
	// writer
	
	/**
	 * Push any [data] to it's [Korm] representation as a [String]
	 *
	 * @param data The instance to push
	 * @return The [data] as a Korm string
	 */
	fun push(data: Any): String
	{
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
	fun push(data: Any, file: File)
	{
		this.writer.write(data, file)
	}
	
	/**
	 * Push any [data] to it's [Korm] representation into a [Writer]
	 *
	 * @param data The instance to push
	 * @param writer The writer to write it to
	 */
	fun push(data: Any, writer: Writer)
	{
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
	fun push(data: Any, stream: OutputStream)
	{
		this.writer.write(data, stream)
	}
	
	
	// reader
	
	/**
	 * Pull korm data from [file] into a [KormReader.ReaderContext]
	 *
	 * @param file The file to pull from
	 * @return The [KormReader.ReaderContext] with pulled data
	 *
	 * - Fails silently for files that don't exist, or are directories
	 */
	fun pull(file: File): KormReader.ReaderContext
	{
		return this.reader.read(file)
	}
	
	/**
	 * Pull korm data from [text] into a [KormReader.ReaderContext]
	 *
	 * @param text The text to pull from
	 * @return The [KormReader.ReaderContext] with pulled data
	 */
	fun pull(text: String): KormReader.ReaderContext
	{
		return this.reader.read(text)
	}
	
	/**
	 * Pull korm data from [reader] into a [KormReader.ReaderContext]
	 *
	 * @param reader The reader to pull from
	 * @return The [KormReader.ReaderContext] with pulled data
	 */
	fun pull(reader: Reader): KormReader.ReaderContext
	{
		return this.reader.read(reader)
	}
	
	/**
	 * Pull korm data from [stream], using [charset], into a [KormReader.ReaderContext]
	 *
	 * @param stream The stream to pull from
	 * @param charset The charset to use for the stream
	 * @return The [KormReader.ReaderContext] with pulled data
	 */
	fun pull(stream: InputStream, charset: Charset = Charset.defaultCharset()): KormReader.ReaderContext
	{
		return this.reader.read(stream, charset)
	}
	
	
	// directly
	
	/**
	 * Pull korm data from [text] and directly create an instance of [to] from it
	 *
	 * @param text The text to pull from
	 * @param to The class type to create
	 *
	 * @return The instance created
	 * @throws IllegalStateException if the instance could not be created
	 */
	fun <T : Any> pull(text: String, to: KClass<T>): T
	{
		return checkNotNull(pull(text).to(to)) { "Result is null" }
	}
	
	/**
	 * Pull korm data from [text] and directly create an instance of [T] from it
	 *
	 * @param text The text to pull from
	 *
	 * @return The instance created
	 * @throws IllegalStateException if the instance could not be created
	 */
	inline fun <reified T : Any> pull(text: String): T
	{
		return pull(text, T::class)
	}
	
	/**
	 * Pull korm data from [file] and directly create an instance of [T] from it
	 *
	 * @param file The file to pull from
	 *
	 * @return The instance created
	 * @throws IllegalStateException if the instance could not be created
	 */
	inline fun <reified T : Any> pull(file: File): T
	{
		return pull<T>(file.reader())
	}
	
	/**
	 * Pull korm data from [reader] and directly create an instance of [to] from it
	 *
	 * @param reader The reader to pull from
	 * @param to The class type to create
	 *
	 * @return The instance created
	 * @throws IllegalStateException if the instance could not be created
	 */
	fun <T : Any> pull(reader: Reader, to: KClass<T>): T
	{
		return checkNotNull(pull(reader).to(to)) { "Result is null" }
	}
	
	/**
	 * Pull korm data from [reader] and directly create an instance of [T] from it
	 *
	 * @param reader The text to pull from
	 *
	 * @return The instance created
	 * @throws IllegalStateException if the instance could not be created
	 */
	inline fun <reified T : Any> pull(reader: Reader): T
	{
		return pull(reader, T::class)
	}
	
	
	// with reference
	
	/**
	 * Pull korm data from [text] and directly create an instance from [to]
	 */
	fun <T : Any> pullRef(text: String, to: RefType<T>): T
	{
		return checkNotNull(pull(text).toRef(to)) { "Result is null" }
	}
	
	/**
	 * Pull korm data from [text] and directly create an instance of [T]
	 */
	inline fun <reified T : Any> pullRef(text: String): T
	{
		return pullRef(text, RefType.of())
	}
	
	/**
	 * Pull korm data from [reader] and directly create an instance from [to]
	 */
	fun <T : Any> pullRef(reader: Reader, to: RefType<T>): T
	{
		return checkNotNull(pull(reader).toRef(to)) { "Result is null" }
	}
	
	/**
	 * Pull korm data from [reader] and directly create an instance of [to]
	 */
	inline fun <reified T : Any> pullRef(reader: Reader): T
	{
		return pullRef(reader, RefType.of())
	}
	
	
	// pull / push
	
	/**
	 * Set the [KormPuller] for type [T]
	 *
	 * @param clazz The type
	 * @param puller The puller
	 */
	fun <T : Any> pullWith(clazz: Class<T>, puller: KormPuller<T>)
	{
		pullers[clazz] = puller
	}
	
	/**
	 * Set the [KormPusher] for type [T]
	 *
	 * @param clazz The type
	 * @param pusher The pusher
	 */
	fun <T : Any> pushWith(clazz: Class<T>, pusher: KormPusher<T>)
	{
		pushers[clazz] = pusher
	}
	
	
	/**
	 * Set the [KormPuller] for type [T]
	 *
	 * @param clazz The type
	 * @param puller The puller
	 */
	fun <T : Any> pullWith(clazz: KClass<T>, puller: KormPuller<T>)
	{
		pullWith(clazz.java, puller)
	}
	
	/**
	 * Set the [KormPusher] for type [T]
	 *
	 * @param clazz The type
	 * @param pusher The pusher
	 */
	fun <T : Any> pushWith(clazz: KClass<T>, pusher: KormPusher<T>)
	{
		pushWith(clazz.java, pusher)
	}
	
	/**
	 * Set the [KormPuller] for type [T]
	 *
	 * - Dear lord, please don't call [KormPuller.pull]
	 */
	inline fun <reified T : Any> pullWith(crossinline pull: KormPuller<T>.(reader: KormReader.ReaderContext, types: MutableList<KormType>) -> T?)
	{
		pullWith(T::class, object : KormPuller<T>
		{
			
			override fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): T?
			{
				return pull.invoke(this, reader, types)
			}
			
		})
	}
	
	/**
	 * Set the [KormPusher] for type [T]
	 *
	 * - Dear lord, please don't call [KormPusher.push]
	 */
	inline fun <reified T : Any> pushWith(crossinline push: KormPusher<T>.(writer: KormWriter.WriterContext, data: T?) -> Unit)
	{
		pushWith(T::class, object : KormPusher<T>
		{
			
			override fun push(writer: KormWriter.WriterContext, data: T?)
			{
				push.invoke(this, writer, data)
			}
			
		})
	}
	
	
	/**
	 * Retrieve the custom pull function for type [T]
	 *
	 * @param clazz The type
	 * @return The [KormPuller] if set, or null
	 */
	fun <T : Any> pullerOf(clazz: Class<T>): KormPuller<T>?
	{
		return pullers[clazz] as? KormPuller<T>
	}
	
	/**
	 * Retrieve the custom push function for type [T]
	 *
	 * @param clazz The type
	 * @return The [KormPusher] if set, or null
	 */
	fun <T : Any> pusherOf(clazz: Class<T>): KormPusher<T>?
	{
		return pushers[clazz] as? KormPusher<T>
	}
	
	/**
	 * Retrieve the custom pull function for type [T]
	 *
	 * @param clazz The type
	 * @return The [KormPuller] if set, or null
	 */
	fun <T : Any> pullerOf(clazz: KClass<T>): KormPuller<T>?
	{
		return pullerOf(clazz.java)
	}
	
	/**
	 * Retrieve the custom push function for type [T]
	 *
	 * @param clazz The type
	 * @return The [KormPusher] if set, or null
	 */
	fun <T : Any> pusherOf(clazz: KClass<T>): KormPusher<T>?
	{
		return pusherOf(clazz.java)
	}
	
	fun <T : Any> codecBy(clazz: Class<T>, codec: KormCodec<T>)
	{
		pullWith(clazz, codec)
		pushWith(clazz, codec)
	}
	
	fun <T : Any> codecBy(clazz: KClass<T>, codec: KormCodec<T>)
	{
		codecBy(clazz.java, codec)
	}
	
	inline fun <reified T : Any, reified A : Any> codecBy(crossinline functionPull: (A) -> T?, crossinline functionPush: (T?) -> A?)
	{
		codecBy(T::class, KormCodec.by(functionPull, functionPush))
	}
	
}