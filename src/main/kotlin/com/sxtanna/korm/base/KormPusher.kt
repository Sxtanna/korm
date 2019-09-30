package com.sxtanna.korm.base

import com.sxtanna.korm.writer.KormWriter

/**
 * A custom method for pushing an object to korm data
 *
 *  - Should properly override [push] to produce valid korm data
 */
interface KormPusher<in T : Any>
{
	
	/**
	 * Create korm data with the given [writer] from the provided [data]
	 */
	fun push(writer: KormWriter.WriterContext, data: T?)
	
}