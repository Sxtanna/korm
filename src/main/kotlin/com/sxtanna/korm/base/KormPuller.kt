package com.sxtanna.korm.base

import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.reader.KormReader

interface KormPuller<out T : Any> {

    fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): T?


    fun badState(reason: String): Nothing {
        throw IllegalStateException(reason)
    }

    fun MutableList<KormType>.typeByName(name: String, remove: Boolean = false): KormType? {
        val korm = find { it.key.data.toString() == name } ?: return null
        if (remove) remove(korm)

        return korm
    }

}