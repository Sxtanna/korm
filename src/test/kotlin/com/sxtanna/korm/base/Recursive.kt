package com.sxtanna.korm.base

data class Recursive(val index: Int, val list: List<Recursive>)
{
	
	constructor(index: Int, count: Int) : this(index, (0..count).map { Recursive(it + 1, count - 1) })
}