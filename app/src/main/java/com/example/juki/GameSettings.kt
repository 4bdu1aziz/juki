package com.example.juki

data class GameSettings(
    val speed: Int,
    val maxRoaches: Int,
    val bonusIntervalSec: Int,
    val roundDurationSec: Int
)