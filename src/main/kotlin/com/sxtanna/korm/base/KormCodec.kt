package com.sxtanna.korm.base

interface KormCodec<T : Any> : KormPuller<T>, KormPusher<T>