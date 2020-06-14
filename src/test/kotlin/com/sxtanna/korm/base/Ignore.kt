package com.sxtanna.korm.base

data class Ignore(val number: Int)
{
	@Transient
	val data = false
}