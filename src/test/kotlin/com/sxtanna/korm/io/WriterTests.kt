package com.sxtanna.korm.io

import com.sxtanna.korm.Korm
import com.sxtanna.korm.base.*
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.writer.base.Options
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WriterTests {

    private val korm = Korm(writer = KormWriter(2, Options.of(Options.SPACE_AFTER_ASSIGN, Options.HASH_ENTRY_ON_NEW_LINE)))


    @Test
    internal fun testNoSymbol() {

        println(korm.push("Hello"))
        println(korm.push(1234567))

        println(korm.push(listOf(1, 2, 3, 4, 5)))
        println(korm.push(mapOf(1 to "1", 2 to "2")))

    }

    @Test
    internal fun testWithSymbol() {
        println(korm.push(Word("Hello")))
        println(korm.push(Numb(1234567)))
    }

    @Test
    internal fun testComplex() {

        //println(korm.push(Complex(listOf(Numb(21), Numb(12), Numb(46)))))

        println(korm.push(mapOf((1 to 2) to 21)))

    }

    @Test
    internal fun testLists() {

        println(korm.push(listOf(1, 2, 3, 4)))

        println(korm.push(listOf(Numb(1), Numb(2), Numb(3), Numb(4))))

    }

    @Test
    internal fun testHashs() {

        println(korm.push(mapOf(1 to 2, 3 to 4, 5 to 6)))

    }

    @Test
    internal fun testLevelCache() {

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
    internal fun testEnum() {

        println(korm.push(Rel.MOM))
        println(korm.push(Rel.MOM to listOf("Name1", "Name2")))

    }

    @Test
    internal fun testConfig() {

        println(korm.push(ArcadeConfig()))

    }

    @Test
    internal fun testTransient() {

        println(korm.push(TransientTest(234)))

    }

    @Test
    internal fun testInterface() {

        println(korm.push(Thing()))

    }

    @Test
    internal fun testAsList() {

        println(korm.push(AsListType("Sxtanna", AsListType.Date.MONDAY)))

    }

    @Test
    internal fun testCodec() {

        println(korm.push(CustomCodecTest("Sxtanna")))

    }


    @Test
    internal fun testMessage() {
        println(korm.push(Message.Error("This is an error!")))
    }

}