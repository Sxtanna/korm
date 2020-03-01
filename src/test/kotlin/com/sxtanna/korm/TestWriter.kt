package com.sxtanna.korm

import com.sxtanna.korm.base.Person
import com.sxtanna.korm.data.option.Options
import com.sxtanna.korm.writer.KormWriter
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

class TestWriter : WithAssertions
{
	
	@Test
	internal fun `test push contains null key`()
	{
		val korm = Korm(KormWriter(2, Options.min(Options.SERIALIZE_NULLS)))
		
		assertThat(korm.push(Person("Sxtanna", null))).contains("age")
	}
	
	@Test
	internal fun `test push does not contain null kill`()
	{
		val korm = Korm(KormWriter(2, Options.min()))
		
		assertThat(korm.push(Person("Sxtanna", null))).doesNotContain("age")
	}
	
}