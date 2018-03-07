package com.sxtanna.korm.base

data class TransientTest(val number: Int) {

    @Transient
    val data = false

}