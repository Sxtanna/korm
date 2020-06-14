package com.sxtanna.korm

import com.google.gson.Gson
import com.sxtanna.korm.data.custom.KormList
import com.sxtanna.korm.data.option.Options
import com.sxtanna.korm.writer.KormWriter
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

internal class Examples
{
	
	
	private val gson = Gson()
	private val korm = Korm(writer = KormWriter(2, Options.none()))
	
	
	@KormList(["name"])
	data class Person(val name: Naming)
	
	@KormList(["name"])
	data class Naming(val name: String)
	
	@Test
	fun personExampleKorm()
	{
		val time = mutableListOf<Long>()
		
		repeat(100)
		{
			time += measureTimeMillis()
			{
				val sxtannaObject = Person(Naming("Sxtanna"))
				//println(sxtannaObject)
				
				val sxtannaString = korm.push(sxtannaObject)
				//println(sxtannaString)
				
				korm.pull(sxtannaString).to<Person>()
			}
		}
		
		
		println(time.average())
	}
	
	
	@Test
	fun personExampleGson()
	{
		val time = mutableListOf<Long>()
		
		repeat(100)
		{
			time += measureTimeMillis()
			{
				val sxtannaObject = Person(Naming("Sxtanna"))
				//println(sxtannaObject)
				
				val sxtannaString = gson.toJson(sxtannaObject)
				//println(sxtannaString)
				
				gson.fromJson(sxtannaString, Person::class.java)
			}
		}
		
		
		println(time.average())
	}
	
}