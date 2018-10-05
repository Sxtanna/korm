package com.sxtanna.korm.base

/**
 * A class that combines a [KormPuller] and a [KormPusher] for type [T]
 */
interface KormCodec<T : Any> : KormPuller<T>, KormPusher<T>