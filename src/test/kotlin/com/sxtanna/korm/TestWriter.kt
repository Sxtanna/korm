package com.sxtanna.korm

import com.sxtanna.korm.base.Ignore
import com.sxtanna.korm.base.Person
import com.sxtanna.korm.data.option.Options
import com.sxtanna.korm.writer.KormWriter
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
object TestWriter : WithAssertions
{
	
	private val kormBase = Korm()
	
	private val kormSansLines = Korm(KormWriter(Options.none()))
	private val kormWithNulls = Korm(KormWriter(Options.min(Options.SERIALIZE_NULLS)))
	
	
	@Test
	internal fun `test push contains null key`()
	{
		assertThat(kormWithNulls.push(Person("Sxtanna", null))).contains("age")
	}
	
	@Test
	internal fun `test push does not contain null kill`()
	{
		assertThat(kormBase.push(Person("Sxtanna", null))).doesNotContain("age")
	}
	
	
	@Test
	internal fun `test no symbol string is just a string`()
	{
		assertThat(kormBase.push("Hello")).isEqualTo(""""Hello"""")
	}
	
	@Test
	internal fun `test no symbol int is just an int`()
	{
		assertThat(kormBase.push(1234567)).isEqualTo("1234567")
	}
	
	@Test
	internal fun `test no symbol list is just a list`()
	{
		assertThat(kormSansLines.push(listOf(1, 2, 3, 4, 5))).isEqualTo("[1, 2, 3, 4, 5]")
	}
	
	@Test
	internal fun `test no symbol hash is just a hash`()
	{
		assertThat(kormSansLines.push(mapOf(1 to "1", 2 to "2"))).isEqualTo("""{ 1:"1" 2:"2" }""")
	}
	
	@Test
	internal fun `test no symbol complex key is just a hash`()
	{
		assertThat(kormSansLines.push(mapOf((1 to 2) to 21))).isEqualTo("""{ `{ first:1 second:2 }`:21 }""")
	}
	
	@Test
	internal fun `test type with transient value was ignored`()
	{
		assertThat(kormSansLines.push(Ignore(20))).doesNotContain("data")
	}
	
}