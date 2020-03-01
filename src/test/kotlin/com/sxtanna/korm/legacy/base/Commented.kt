package com.sxtanna.korm.legacy.base

import com.sxtanna.korm.data.custom.KormComment

data class Commented(@KormComment("This is commented!!")
                     val name: String,
                     @KormComment("This is the comment's age!", "It means nothing")
                     val age: Int,
                     @KormComment("This is optional")
                     var thing: String? = null)