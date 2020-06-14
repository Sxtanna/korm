package com.sxtanna.korm.legacy.base

import com.sxtanna.korm.base.KormPuller
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.custom.KormCustomPull
import com.sxtanna.korm.reader.KormReader
import java.util.UUID

@KormCustomPull(Message.Puller::class)
sealed class Message
{
	
	val name = this::class.simpleName ?: this.javaClass.simpleName.substringAfterLast('.')
	
	
	class Delete : Message()
	
	
	data class Join(val playerUUID: UUID) : Message()
	
	data class Quit(val playerUUID: UUID) : Message()
	
	
	data class Error(val message: String) : Message()
	
	
	object Puller : KormPuller<Message>
	{
		
		override fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): Message?
		{
			val name = types.byName("name") ?: return null
			
			return when (name.asBase()?.dataAsString() ?: return null)
			{
				"Join"  ->
				{
					reader.mapInstanceK(Join::class, types, this)
				}
				"Quit"  ->
				{
					reader.mapInstanceK(Quit::class, types, this)
				}
				"Error" ->
				{
					reader.mapInstanceK(Error::class, types, this)
				}
				else    ->
				{
					null
				}
			}
		}
		
	}
	
	
	interface Thing0
	
	interface Thing1
	
	interface Thing2 : Thing0, Thing1
	
}