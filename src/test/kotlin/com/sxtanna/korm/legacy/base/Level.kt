package com.sxtanna.korm.legacy.base

data class Level(val name: String, val owner: String,
                 val min: Vec, val max: Vec,
                 val spawns: Map<Int, List<Vec>> = emptyMap(), val custom: Map<Int, List<Vec>> = emptyMap())