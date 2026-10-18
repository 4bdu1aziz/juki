package com.example.juki.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val gender: String,
    val course: String,
    val birthDate: String,
    val zodiacSign: String
)