package com.sxtanna.korm.data.custom

import com.sxtanna.korm.base.KormPuller
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormCustomPull(val puller: KClass<out KormPuller<*>>)