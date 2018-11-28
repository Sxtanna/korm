package com.sxtanna.korm.comp

import com.sxtanna.korm.data.Data

internal data class Token(val line: Int, val char: Int, val data: Data, var type: Type = data.type) {
    constructor(data: Data, type: Type = data.type): this(0, 0, data, type)
}