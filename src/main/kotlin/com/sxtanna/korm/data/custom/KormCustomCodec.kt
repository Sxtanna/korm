package com.sxtanna.korm.data.custom

import com.sxtanna.korm.base.KormCodec
import kotlin.reflect.KClass

/**
 * Classes annotated with this will use the provided [codec] when pulling/pushing
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormCustomCodec(val codec: KClass<out KormCodec<out Any>>)