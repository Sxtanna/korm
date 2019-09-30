package com.sxtanna.korm.io

import com.sxtanna.korm.Korm
import com.sxtanna.korm.base.*
import com.sxtanna.korm.base.stupid.StupidThing
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.writer.base.Options
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WriterTests
{
	
	private val korm = Korm(writer = KormWriter(2, Options.min(Options.COMPLEX_LIST_ENTRY_ON_NEW_LINE)))
	
	
	@Test
	internal fun testNoSymbol()
	{
		
		println(korm.push("Hello"))
		println(korm.push(1234567))
		
		println(korm.push(listOf(1, 2, 3, 4, 5)))
		println(korm.push(mapOf(1 to "1", 2 to "2")))
		
	}
	
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
	
}