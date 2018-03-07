package com.sxtanna.korm.base

interface Exec<out T : Any> {

    fun eval(): T

}