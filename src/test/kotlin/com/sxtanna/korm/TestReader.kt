package com.sxtanna.korm

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
object TestReader : WithAssertions
{
	
	private val kormBase = Korm()
	
}