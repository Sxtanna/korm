package com.sxtanna.korm.legacy.base

interface Other
{
	
	val name: String
	
	
	object Only : Other
	{
		
		override val name = "Only"
		
	}
	
}