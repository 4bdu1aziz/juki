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
        SELECT p.fullName AS playerName, 
               MAX(s.score) AS score, 
               s.difficulty, 
               s.roundDuration, 
               s.accuracy, 
               s.timestamp
        FROM scores s
        INNER JOIN players p ON p.id = s.playerId
        WHERE s.roundDuration = :duration
        GROUP BY s.playerId
        ORDER BY score DESC
        LIMIT 50
    """)
    fun getTopScoresByDuration(duration: Int): Flow<List<ScoreWithName>>

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 1")
    suspend fun getBestScore(): ScoreEntity?
}

data class ScoreWithName(
    val playerName: String,
    val score: Int,
    val difficulty: Int,
    val roundDuration: Int,
    val accuracy: Float,
    val timestamp: Long
)