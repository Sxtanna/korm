package com.sxtanna.korm.data.custom

import com.sxtanna.korm.base.KormPusher
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormCustomPush(val pusher: KClass<out KormPusher<Any>>)