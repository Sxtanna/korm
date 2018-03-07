package com.sxtanna.korm.data

sealed class KormType {

    abstract val key: Data


    fun asBase() = this as? BaseType

    fun asList() = this as? ListType

    fun asHash() = this as? HashType


    data class BaseType(override val key: Data, val data: Any) : KormType() {

        override fun toString(): String {
            return "Base(key=$key, data=$data | ${data::class})"
        }

    }

    data class ListType(override val key: Data, val data: List<Any>) : KormType() {

        override fun toString(): String {
            return "List(key=$key, data=${data.joinToString("\n  ", "[\n  ", "\n]")} | ${data::class})"
        }

    }

    data class HashType(override val key: Data, val data: List<KormType>) : KormType() {

        override fun toString(): String {
            return "Hash(key$key, data=${data.joinToString("\n  ", "[\n  ", "\n]")})"
        }

    }

}