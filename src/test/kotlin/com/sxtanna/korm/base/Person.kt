package com.sxtanna.korm.base

data class Person(val name: String, val relations: Map<Rel, Person>)