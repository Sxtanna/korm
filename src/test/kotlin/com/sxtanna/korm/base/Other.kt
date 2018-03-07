package com.sxtanna.korm.base

interface Other {

        val name : String


        object Only : Other {

            override val name = "Only"

        }

    }