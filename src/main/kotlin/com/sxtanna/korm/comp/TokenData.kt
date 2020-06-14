package com.sxtanna.korm.comp

import com.sxtanna.korm.data.Data

internal data class TokenData(val line: Int, val char: Int, val data: Data, var type: TokenType = data.type)
{
	constructor(data: Data, type: TokenType = data.type) : this(0, 0, data, type)
}