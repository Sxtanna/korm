package com.sxtanna.korm.base;

enum class GameMessage(val string: String, vararg params: String) {

    GAME_JOIN("{player} is joining", "player"),
    GAME_QUIT("{player} is leaving", "player"),

    GAME_CANT_SPEC("You can't spectate this game");


}