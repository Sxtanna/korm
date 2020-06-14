package com.sxtanna.korm.legacy.io

import com.sxtanna.korm.Korm
import com.sxtanna.korm.legacy.base.ArcadeConfig
import com.sxtanna.korm.legacy.base.AsListType
import com.sxtanna.korm.legacy.base.Commented
import com.sxtanna.korm.legacy.base.CustomCodecTest
import com.sxtanna.korm.legacy.base.InnerClassType
import com.sxtanna.korm.legacy.base.Level
import com.sxtanna.korm.legacy.base.LevelCache
import com.sxtanna.korm.legacy.base.Message
import com.sxtanna.korm.legacy.base.Numb
import com.sxtanna.korm.legacy.base.Rel
import com.sxtanna.korm.legacy.base.Thing
import com.sxtanna.korm.legacy.base.TransientTest
import com.sxtanna.korm.legacy.base.Vec
import com.sxtanna.korm.legacy.base.Word
import com.sxtanna.korm.legacy.base.stupid.StupidThing
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.data.option.Options
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WriterTests
{
	
	private val korm = Korm(writer = KormWriter(2, Options.min(Options.COMPLEX_LIST_ENTRY_ON_NEW_LINE)))
	
	
	@Test
	internal fun testWithSymbol()
	{
		println(korm.push(Word("Hello")))
		println(korm.push(Numb(1234567)))
	}
	
	@Test
	internal fun testComplex()
	{
		
		//println(korm.push(Complex(listOf(Numb(21), Numb(12), Numb(46)))))
		
		println(korm.push(mapOf((1 to 2) to 21)))
		
	}
	
	@Test
	internal fun testLists()
	{
		
		println(korm.push(listOf(1, 2, 3, 4)))
		
		println(korm.push(listOf(Numb(1), Numb(2), Numb(3), Numb(4))))
		
	}
	
	@Test
	internal fun testHashs()
	{
		
		println(korm.push(mapOf(1 to 2, 3 to 4, 5 to 6)))
		
	}
	
	@Test
	internal fun testLevelCache()
	{
		
		val level0 = Level(name = "Lobby", owner = "Sxtanna",
		                   min = Vec(-50.0, -50.0, -50.0),
		                   max = Vec(50.0, 50.0, 50.0),
		                   spawns = mapOf(1 to listOf(Vec(1.0, 1.0, 1.0), Vec(2.0, 1.0, 2.0))),
		                   custom = mapOf(1 to listOf(Vec(1.0, 1.0, 1.0), Vec(2.0, 1.0, 2.0))))
		
		val level1 = Level(name = "Arcade", owner = "Sxtanna",
		                   min = Vec(-50.0, -50.0, -50.0),
		                   max = Vec(50.0, 50.0, 50.0),
		                   spawns = mapOf(1 to listOf(Vec(1.0, 1.0, 1.0), Vec(2.0, 1.0, 2.0))),
		                   custom = mapOf(1 to listOf(Vec(1.0, 1.0, 1.0), Vec(2.0, 1.0, 2.0))))
		
		
		println(korm.push(level0))
		println("\n\n\n")
		
		println(korm.push(LevelCache(listOf(level0))))
		println("\n\n\n")
		
		println(korm.push(LevelCache(listOf(level0, level1))))
		
	}
	
	@Test
	internal fun testEnum()
	{
		
		println(korm.push(Rel.MOM))
		println(korm.push(Rel.MOM to listOf("Name1", "Name2")))
		
	}
	
	@Test
	internal fun testConfig()
	{
		
		println(korm.push(ArcadeConfig()))
		
	}
	
	@Test
	internal fun testTransient()
	{
		
		println(korm.push(TransientTest(234)))
		
	}
	
	@Test
	internal fun testInterface()
	{
		
		println(korm.push(Thing()))
		
	}
	
	@Test
	internal fun testAsList()
	{
		
		println(korm.push(AsListType("Sxtanna", AsListType.Date.MONDAY)))
		
	}
	
	@Test
	internal fun testCodec()
	{
		
		println(korm.push(CustomCodecTest("Sxtanna")))
		
	}
	
	
	@Test
	internal fun testMessage()
	{
		println(korm.push(Message.Error("This is an error!")))
	}
	
	@Test
	internal fun testFilePush()
	{
		korm.push(ArcadeConfig(), File("arcade_config.korm"))
	}
	
	@Test
	internal fun testComplexStringKey()
	{
		val text = korm.push(mapOf("Hello World" to 21, "Goodbye World" to 32))
		println(text)
		
		val hash = korm.pull(text).toHash<String, Int>()
		println(hash)
	}
	
	@Test
	internal fun testInnerClass()
	{
		val inner = InnerClassType()
		
		repeat(10) {
			inner.LookItsAnInnerClass(it)
		}
		
		val text = korm.push(inner)
		println(text)
	}
	
	@Test
	internal fun testEscapedQuotes()
	{
		val thing = object : Any()
		{
			
			val text0 = "Hello World"
			val text1 = "Hello \"World\""
			
		}
		
		val text = korm.push(thing)
		println(text)
	}
	
	@Test
	internal fun testAtomics()
	{
		val text0 = korm.push(AtomicInteger(10))
		println(text0)
	}
	
	@Test
	internal fun testStupidThings()
	{
		korm.codecBy(StupidThing::class, StupidThing.CODEC)
		
		val text0 = korm.push(StupidThing.Thing1)
		println(text0)
		
		val text1 = korm.push(StupidThing.Thing2)
		println(text1)
		
		val text2 = korm.push(StupidThing.Enclosing(StupidThing.Thing1))
		println(text2)
	}
	
	@Test
	internal fun testExceptions()
	{
		val text0 = korm.push(IllegalArgumentException("Oops, bad argument"))
		println(text0)
		
		val text1 = korm.push(IllegalArgumentException())
		println(text1)
		
		
		korm.pushWith<UnsupportedOperationException> { writer, data ->
			writer.writeBase("Oh wow, this is unsupported dude: ${data?.message}")
		}
		
		val text2 = korm.push(UnsupportedOperationException("Hi!"))
		println(text2)
	}
	
	@Test
	internal fun testCommented()
	{
		println(korm.push(Commented("Hello!", 20)))
		
		val text =
			"""
				/**
				 * This is the comment's age!
				 * It means nothing
				 */
				age: 20
				// This is commented!!
				name: "Hello!"
			""".trimIndent()
		
		val reader = korm.pull(text)
		val commented = reader.to<Commented>() ?: return
		
		if (commented.thing == null)
		{
			commented.thing = "Optional argument"
		}
		
		println(commented)
		println(korm.push(commented))
	}
	
	@Test
	internal fun testSerializeNulls()
	{
		println("korm list with null")
		val text0 = korm.push(listOf(1, 2, null))
		println(text0)
		println()
		
		println("list nonnull")
		val data0 = korm.pull(text0).toListRef<Int>()
		println(data0)
		println()
		
		println("list nullable")
		val data0N = korm.pull(text0).toListRef<Int?>()
		println(data0N)
		println()
		
		println("korm hash with null")
		val text1 = korm.push(mapOf(1 to "Hi", 2 to null))
		println(text1)
		println()
		
		println("hash nonnull")
		val data1 = korm.pull(text1).toHash<Int, String>()
		println(data1)
		println()
		
		println("hash nullable")
		val data1N = korm.pull(text1).toHash<Int, String?>()
		println(data1N)
		println()
		
		val text2 = korm.push(Thing("Sxtanna", null))
		println(text2)
		
		val data2 = korm.pull(text2).to<Thing>()
		println(data2)
	}
	
	data class Thing(val name: String, val age: Int?)
	
}