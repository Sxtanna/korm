package com.sxtanna.korm.comp

import com.sxtanna.korm.data.Data

data class Token(val data: Data, var type: Type = data.type)