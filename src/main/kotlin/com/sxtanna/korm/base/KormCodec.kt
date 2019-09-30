package com.sxtanna.korm.base

import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.reader.KormReader.ReaderContext
import com.sxtanna.korm.writer.KormWriter.WriterContext

/**
 * A class that combines a [KormPuller] and a [KormPusher] for type [T]
 */
interface KormCodec<T : Any> : KormPuller<T>, KormPusher<T>
{
	
	companion object
	{
		
		inline fun <reified T : Any, reified A : Any> by(crossinline functionPull: (A) -> T?, crossinline functionPush: (T?) -> A?): KormCodec<T>
		{
			return object : KormCodec<T>
			{
				
				override fun pull(reader: ReaderContext, types: MutableList<KormType>): T?
				{
					val single = types.singleOrNull() ?: return null
					val result = reader.mapKormToType(single, A::class.java) as? A
					
					return functionPull.invoke(result ?: return null)
				}
				
				override fun push(writer: WriterContext, data: T?)
				{
					writer.writeData(functionPush.invoke(data) ?: return)
				}
				
			}
		}
		
	}
	
}