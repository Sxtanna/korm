package com.sxtanna.korm.comp

enum class Type {

    INT {

        override fun mapValue(input: Any): Any {
            return (input as? Number)?.toInt() ?: input.toString().toInt()
        }

    },
    DEC {

        override fun mapValue(input: Any): Any {
            return (input as? Number)?.toDouble() ?: input.toString().toDouble()
        }

    },

    BOOL {

        override fun mapValue(input: Any): Any {
            return (input as? Boolean) ?: input.toString().toBoolean()
        }

    },

    CHAR {

        override fun mapValue(input: Any): Any {
            return (input as? Char) ?: input.toString().first()
        }

    },
    TEXT,

    COMMA,

    BRACE_L,
    BRACE_R,

    BRACK_L,
    BRACK_R,

    ASSIGN,
    SYMBOL,
    COMPLEX;


    internal open fun mapValue(input: Any): Any {
        return input
    }

}