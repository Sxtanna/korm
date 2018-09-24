package com.sxtanna.korm.data

import com.sxtanna.korm.comp.Type

data class Data(val inputData: Any, val type: Type) {

    val data: Any
        get() = type.mapValue(inputData)


    fun isNumber() = data is Number

    fun isString() = data is String

    fun isBoolean() = data is Boolean


    fun asNumber() = data as? Number


    override fun toString(): String {
        return "Data(data=$data, type=$type | ${data::class})"
    }


    companion object {

        fun none() = Data("", Type.TEXT)

    }

}