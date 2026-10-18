package com.example.juki.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("""
        SELECT p.fullName AS playerName, s.score, s.difficulty, s.accuracy, s.timestamp
        FROM scores s
        INNER JOIN players p ON p.id = s.playerId
        ORDER BY s.score DESC
        LIMIT 50
    """)
    fun getTopScores(): Flow<List<ScoreWithName>>

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 1")
    suspend fun getBestScore(): ScoreEntity?
}

data class ScoreWithName(
    val playerName: String,
    val score: Int,
    val difficulty: Int,
    val accuracy: Float,
    val timestamp: Long
)