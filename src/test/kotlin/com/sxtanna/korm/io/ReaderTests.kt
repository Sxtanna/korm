package com.sxtanna.korm.io

import com.sxtanna.korm.Korm
import com.sxtanna.korm.base.*
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.writer.base.Options
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReaderTests {

    private val korm = Korm(writer = KormWriter(2, Options.min(Options.COMPLEX_LIST_ENTRY_ON_NEW_LINE)))


    @Test
    internal fun testBase() {

        println(korm.pull("123").to<Byte>())
        println(korm.pull("245").to<Short>())
        println(korm.pull("243215").to<Int>())
        println(korm.pull("256768845").to<Long>())
        println(korm.pull("245.43").to<Float>())
        println(korm.pull("24345746745.43").to<Double>())

        println("\n")

        println(korm.pull("true").to<Boolean>())

        println("\n")

        println(korm.pull("'A'").to<Char>())
        println(korm.pull("\"Hello\"").to<String>())

    }

    @Test
    internal fun testList() {

        val intA = korm.pull("[1, 2, 3, 4]").to<IntArray>()
        println(intA)
        println(intA?.joinToString())

        println("\n")

        val intL = korm.pull("[1, 2, 3, 4]").toList<Int>()
        println(intL)
        println(intL.joinToString())

        println("\n")

        val nameL = korm.pull("""[{ value: "Ranald" }, { value: "Emiliee" }]""").toList<Name>()
        println(nameL)

    }

    @Test
    internal fun testHash() {

        val intNameH: Map<Int, Name> = korm.pull("""{ 0: { value: "Ranald" } 1: { value: "Emiliee" } }""").toHash()
        println(intNameH)

        println("\n\n")
        println(korm.push(intNameH))

    }

    @Test
    internal fun testCustoms() {

        val person = Person("Sxtanna", mapOf(
                Rel.MOM to Person("Mom", mapOf(
                        Rel.SIS to Person("Mom's Sis", emptyMap()))),
                Rel.DAD to Person("Dad", emptyMap()),
                Rel.BRO to Person("Bro", emptyMap()),
                Rel.SIS to Person("Sis", emptyMap())))

        println(person)

        println("\n\n")

        val personText = korm.push(person)
        println(personText)

        println("\n\n")

        val personData = korm.pull(personText)
        println(personData.to(Person::class))

    }

    @Test
    internal fun testDeep() {

        val list = listOf(listOf(listOf(1, 2), listOf(3, 4)), listOf(listOf(5, 6), listOf(7, 8)))

        val listText = korm.push(list)
        println(listText)

        println("\n\n")

        val listData = korm.pull(listText)
        println(listData.viewTypes())

        println("\n\n")

        println(listData.toListRef<List<List<Int>>>())
        println(listData.toRef<List<List<List<Int>>>>())

    }

    @Test
    internal fun testComplex() {

        val complex = mapOf((1 to 1) to "Hello", (2 to 2) to "Goodbye")
        println(complex)

        println("\n")

        val complexText = korm.push(complex)
        println(complexText)

        println("\n")

        val complexData = korm.pull(complexText)
        println(complexData.toHashRef<Pair<Int, Int>, String>())

    }

    @Test
    internal fun testComplexComplex() {

        val complex = mapOf(listOf(Name("Sxtanna"), Name("Ranald")) to 18, listOf(Name("Other"), Name("Person")) to 40)
        println(complex)

        println("\n")

        val complexText = korm.push(complex)
        println(complexText)

        println("\n")

        val complexData = korm.pull(complexText)
        println(complexData.toHashRef<List<Name>, String>())

    }


    @Test
    internal fun testLoose() {

        val text =
                """
                    first: "Hello"
                    second: "World"
                """


        println(korm.pull(text.trimIndent()).toRef<Pair<String, String>>())
    }


    @Test
    internal fun testInterface() {

        val text =
                """
                    other: {
                      name: "Only"
                    }
                """

        println(korm.pull(text.trimIndent()).toRef<Pair<String, String>>())
    }


    @Test
    internal fun testAsList() {

        val text = """["Sxtanna", MONDAY]"""

        println(korm.pull(text.trimIndent()).to<AsListType>())

    }

    @Test
    internal fun testComplexAsList() {

        val text =
                """
                    [
                      ["Sxtanna", MONDAY],
                      ["Sxtanna", MONDAY],
                      ["Sxtanna", MONDAY],
                      ["Sxtanna", MONDAY],
                    ]
                """

        println(korm.pull(text).toList<AsListType>())
    }


    @Test
    internal fun testCodec() {

        val text0 = """names: ["Sxtanna", "Sxtanna"]"""

        println(korm.pull(text0.trimIndent()).to<CustomCodecTest>())


        val text1 =
                """
                    lang: { name: "French" }
                    name: "Sxtanna"
                    id: 21
                """

        println(korm.pull(text1.trimIndent()).to<User>())
    }

    @Test
    internal fun testLang() {
        val text =
                """
                    {
                      GAME_JOIN: "{player}, the new kitten has arrived!"
                      GAME_QUIT: "{player}, went back to his litterbox."

                      GAME_KIT_CHOOSE: "The kitten equipped the majestic kit, \"{kit}\""

                      GAME_CANT_SPEC: "Unfortunately, you can't spy on this game"

                      USER_LANG: "Meowth! I knew you would pick this language eventually :3"
                    }
                """

        val pull = korm.pull(text)

        println(pull.viewTypes())
        println(pull.toHashRef<GameMessage, String>())

    }


    @Test
    internal fun testSymbolValue() {
        val text =
                """
                    store: {
                      type: LOCAL_KORM
                      temp: false
                    }
                """

        val pull = korm.pull(text)

        println(pull.viewTypes())
    }

}