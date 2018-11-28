package com.sxtanna.korm.data

import com.sxtanna.korm.data.KormType.*

/**
 * The three basic key->value types used in Korm
 *
 * - [BaseType]
 * - [ListType]
 * - [HashType]
 */
sealed class KormType {

    abstract val key: Data


    /**
     * Attempts to cast this [KormType] to a [BaseType]
     */
    fun asBase() = this as? BaseType

    /**
     * Attempts to cast this [KormType] to a [ListType]
     */
    fun asList() = this as? ListType

    /**
     * Attempts to cast this [KormType] to a [HashType]
     */
    fun asHash() = this as? HashType


    /**
     * Represents the most basic key->value
     *
     * ```key: "value"```
     */
    data class BaseType(override val key: Data, val data: Any) : KormType() {

        override fun toString(): String {
            return "Base<${data::class.simpleName}>[\n  k=$key\n  v=$data\n]"
        }

    }

    /**
     * Represents a key with a list as it's value
     *
     * ```key: ["v0", "v1", "v3"]```
     */
    data class ListType(override val key: Data, val data: List<Any>) : KormType() {

        override fun toString(): String {
            val kormTypes = data.any { it is KormType }
            return "List<${data::class.simpleName}>[\n  k=$key\n  v=${if (kormTypes) data.joinToString(", ", "\n---", "\n---") else data.toString()}\n]"
        }

    }

    /**
     * Represents a key with a hash as it's value
     *
     * ```
     * key: {
     *   key0: "value0"
     *   key1: "value1"
     * }
     * ```
     */
    data class HashType(override val key: Data, val data: List<KormType>) : KormType() {

        override fun toString(): String {
            return "\nHash:\nk=$key\nv=\n${data.joinToString("\n")}"
        }

    }

}