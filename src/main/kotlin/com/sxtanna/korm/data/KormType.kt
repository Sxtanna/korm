package com.sxtanna.korm.data

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
            return "Base(key=$key, data=$data | ${data::class})"
        }

    }

    /**
     * Represents a key with a list as it's value
     *
     * ```key: ["v0", "v1", "v3"]```
     */
    data class ListType(override val key: Data, val data: List<Any>) : KormType() {

        override fun toString(): String {
            return "List(key=$key, data=${data.joinToString("\n  ", "[\n  ", "\n]")} | ${data::class})"
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
            return "Hash(key$key, data=${data.joinToString("\n  ", "[\n  ", "\n]")})"
        }

    }

}