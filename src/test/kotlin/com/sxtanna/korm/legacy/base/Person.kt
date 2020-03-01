package com.sxtanna.korm.legacy.base

data class Person(val name: String, val relations: Map<Rel, Person>)