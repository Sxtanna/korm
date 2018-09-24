package com.sxtanna.korm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildTests {

    val korm = Korm()


    @Test
    internal fun testNamed() {
        val text0 = korm.writer.build {
            dsl {
                "name0" {
                    "data"(21)

                    "list"(0, 1, 2, 3)

                    "hash"(0 _ 1, 2 _ 3)
                }
            }
        }

        println(text0)

        val text1 = korm.writer.build {
            data("name1") {
                data("data", 21)

                list<Int>("list") {
                    addAll(0, 1, 2, 3)
                }

                hash<Int, Int>("hash") {
                    put(0, 1)
                    put(2, 3)
                }
            }
        }

        println(text1)
    }
}