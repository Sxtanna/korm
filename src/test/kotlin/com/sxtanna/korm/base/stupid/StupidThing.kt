package com.sxtanna.korm.base.stupid

import com.sxtanna.korm.base.KormCodec

sealed class StupidThing
{
	
	abstract val name: String
	abstract val kind: Boolean
	
	
	override fun toString(): String
	{
		return "StupidThing(name='$name')"
	}
	
	
	object Thing1 : StupidThing()
	{
		
		override val name = "Thing1"
		override val kind = false
		
	}
	
	object Thing2 : StupidThing()
	{
		
		override val name = "Thing2"
		override val kind = true
		
	}
	
	
	class Enclosing(val thing: StupidThing)
	
	
	companion object
	{
		
		val CODEC = KormCodec.by<StupidThing, String>(
			functionPull = {
				when (it)
				{
					"Thing1" -> Thing1
					"Thing2" -> Thing2
					else     -> null
				}
			},
			functionPush = {
				it?.name
			}
		                                             )
		
	}
	
	
}