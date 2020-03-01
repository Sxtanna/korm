package com.sxtanna.korm

import com.sxtanna.korm.data.option.Options
import com.sxtanna.korm.writer.KormWriter

class TestReader
{
	
	private val korm = Korm(writer = KormWriter(2, Options.none()))
	
}