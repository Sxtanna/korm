package com.sxtanna.korm.legacy.base

data class LevelCache(val level: List<Level> = emptyList(), val cache: List<Cache> = emptyList())
{
	
	data class Cache(val named: String, val modes: List<String>)
	
}