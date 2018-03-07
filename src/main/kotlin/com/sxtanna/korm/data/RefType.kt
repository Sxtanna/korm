package com.sxtanna.korm.data

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class RefType<T : Any> {

    fun type(): Type {
        val genericType = this::class.java.genericSuperclass
        return (genericType as ParameterizedType).actualTypeArguments[0]
    }


    companion object {

        inline fun <reified T : Any> of() = object : RefType<T>() {}

    }

}