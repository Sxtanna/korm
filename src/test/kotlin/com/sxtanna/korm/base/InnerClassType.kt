package com.sxtanna.korm.base

class InnerClassType {

    val inners = mutableListOf<LookItsAnInnerClass>()


    override fun toString(): String {
        return "Outer[inners=$inners]"
    }

    inner class LookItsAnInnerClass(val data: Int) {

        val innerInners = mutableListOf<LookItsAnInnerInnerClass>()

        init {
            inners.add(this)
        }

        fun accessTest() {
            println("OUTER'S INNERS ARE $inners")
        }

        override fun toString(): String {
            return "Inner[data=$data, innerInners=$innerInners]"
        }

        inner class LookItsAnInnerInnerClass(val bool: Boolean) {

            init {
                innerInners.add(this)
            }

            fun accessTest() {
                println("INNERS'S INNERINNERS ARE $innerInners")
            }

            override fun toString(): String {
                return "InnerInner[bool=$bool]"
            }

        }

    }

}