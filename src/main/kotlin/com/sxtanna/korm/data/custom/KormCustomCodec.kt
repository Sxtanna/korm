package com.sxtanna.korm.data.custom

import com.sxtanna.korm.base.KormCodec
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class KormCustomCodec(val codec: KClass<out KormCodec<out Any>>)