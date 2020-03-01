package com.sxtanna.korm.legacy.base

data class TransientTest(val number: Int)
{
	
	@Transient
	val data = false
	
}