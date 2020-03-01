package com.sxtanna.korm.legacy.base

import com.sxtanna.korm.data.custom.KormList

@KormList(["name", "date"])
data class AsListType(val name: String, val date: Date)
{
	
	
	enum class Date
	{
		
		MONDAY,
		TUESDAY,
		WEDNESDAY;
		
	}
	
}