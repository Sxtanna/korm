package com.sxtanna.korm.data

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A cute little JVM Type Erasure workaround
 */
abstract class RefType<T : Any> {

    /**
     * The full type [T]
     */
    fun type(): Type {
        val genericType = this::class.java.genericSuperclass
        return (genericType as ParameterizedType).actualTypeArguments[0]
    }


    companion object {

        /**
         * Create a new RefType using an anonymous class
         */
        inline fun <reified T : Any> of() = object : RefType<T>() {}

    }

}