package com.sxtanna.korm.data

import com.sxtanna.korm.comp.Type

data class Data(@Transient val inputData: Any, val type: Type) {

    val data: Any
        get() = type.mapValue(inputData)


    fun isNumber() = data is Number

    fun isString() = data is String

    fun isBoolean() = data is Boolean


    fun asNumber() = data as? Number

    fun asString() = data as? String

    fun asBoolean() = data as? Boolean


    override fun toString(): String {
        return "Data<${data::class.simpleName}, $type>[$data]"
    }


    companion object {

        fun none() = Data("", Type.TEXT)

    }

}