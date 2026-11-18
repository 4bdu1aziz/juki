package com.example.juki

import android.content.Context
import com.example.juki.db.PlayerEntity

object CurrentPlayer {
    var player: PlayerEntity? = null

    fun saveToPrefs(context: Context, playerId: Long) {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("current_player_id", playerId).apply()
    }

    fun getSavedId(context: Context): Long {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("current_player_id", -1L)
    }
}