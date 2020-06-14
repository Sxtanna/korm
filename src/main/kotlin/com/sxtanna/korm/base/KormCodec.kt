package com.sxtanna.korm.base

import com.sxtanna.korm.data.KormNull
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.reader.KormReader.ReaderContext
import com.sxtanna.korm.util.RefType
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
			val type = RefType.of<A>().type()
			
			return object : KormCodec<T>
			{
				
				override fun pull(reader: ReaderContext, types: MutableList<KormType>): T?
				{
					val result = reader.mapDataToType(types.singleOrNull(), type)
					return functionPull.invoke(result as? A? ?: return null)
				}
				
				override fun push(writer: WriterContext, data: T?)
				{
					writer.writeData(functionPush.invoke(data) ?: KormNull)
				}
				
			}
		}
		
	}
	
}