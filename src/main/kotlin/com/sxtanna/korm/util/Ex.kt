package com.sxtanna.korm.util

import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

/**
 * Don't ask...
 */
internal object Ex {

    fun printException(exception: Exception, message: String, vararg extraData: Any) {
        println("== Korm Exception =S=")
        println("== $message: ${exception.message}")

        val exceptionMessage = StringWriter().apply { exception.printStackTrace(PrintWriter(this)) }.toString().replace("\n", "\n  ==")
        println("  ==$exceptionMessage")

        if (extraData.isNotEmpty()) {
            println("  ==== Extra ==S==")
            extraData.forEach { println(it) }
            println("  ==== Extra ==E==")
        }

        println("== $message: ${exception.message}")
        println("== Korm Exception =E=")
    }

}