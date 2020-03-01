package com.sxtanna.korm.legacy.base

data class User(val id: Long)
{
	
	var lang: Lang = Lang.English
	val name: String = "Sxtanna"
	
	
	override fun toString(): String
	{
		return "User(id=$id, lang=$lang, name='$name')"
	}
	
}