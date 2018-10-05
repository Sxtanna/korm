package com.sxtanna.korm.data.custom

import com.sxtanna.korm.base.KormPusher
import kotlin.reflect.KClass

/**
 * Classes annotated with this will use the provided [pusher] when pushing
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormCustomPush(val pusher: KClass<out KormPusher<*>>)