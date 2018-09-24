package com.sxtanna.korm.base

import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.reader.KormReader

/**
 * A custom method for pulling an object from korm data
 *
 *  - Should properly override [pull] to produce an instance of [T]
 */
interface KormPuller<out T : Any> {

    /**
     * Create an object [T] from the current [reader] using the provided data in [types]
     */
    fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): T?


    /**
     * Convenience method for throwing an [IllegalStateException] with the provided [reason]
     */
    fun badState(reason: String): Nothing {
        throw IllegalStateException(reason)
    }

    /**
     * Find a [KormType] with the provided [name], with the option to [remove] it from the given list
     */
    fun MutableList<KormType>.typeByName(name: String, remove: Boolean = false): KormType? {
        val korm = find { it.key.data.toString() == name } ?: return null

        if (remove) {
            remove(korm)
        }

        return korm
    }

}