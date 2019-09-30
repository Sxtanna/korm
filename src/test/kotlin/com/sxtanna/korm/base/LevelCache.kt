package com.sxtanna.korm.base

data class LevelCache(val level: List<Level> = emptyList(), val cache: List<Cache> = emptyList())
{
	
	data class Cache(val named: String, val modes: List<String>)
	
}