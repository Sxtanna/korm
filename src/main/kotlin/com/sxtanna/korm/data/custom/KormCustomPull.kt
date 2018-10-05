package com.sxtanna.korm.data.custom

import com.sxtanna.korm.base.KormPuller
import kotlin.reflect.KClass

/**
 * Classes annotated with this will use the provided [puller] when pulling
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormCustomPull(val puller: KClass<out KormPuller<*>>)