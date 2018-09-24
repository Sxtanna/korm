package com.sxtanna.korm.base

/**
 * Represents a class that has a block of code that will be "executed"
 */
interface Exec<out T : Any> {

    /**
     * Execute this class' function
     */
    fun exec(): T

}