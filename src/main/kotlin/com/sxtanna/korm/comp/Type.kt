package com.sxtanna.korm.comp

enum class Type(val map: (Any) -> Any = { it }) {

    INT({ (it as? Number)?.toInt() ?: it.toString().toInt() }),
    DEC({ (it as? Number)?.toDouble() ?: it.toString().toDouble() }),

    BOOL({ (it as? Boolean) ?: it.toString().toBoolean() }),
    CHAR({ (it as? Char) ?: it.toString().first() }),
    TEXT,

    COMMA,

    BRACE_L,
    BRACE_R,

    BRACK_L,
    BRACK_R,

    ASSIGN,
    SYMBOL,
    COMPLEX;

}