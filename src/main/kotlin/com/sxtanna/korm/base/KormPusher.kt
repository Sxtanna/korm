package com.sxtanna.korm.base

import com.sxtanna.korm.writer.KormWriter

interface KormPusher<in T : Any> {

    fun push(data: T?, writer: KormWriter.WriterContext)

}