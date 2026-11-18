package com.example.juki

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel : ViewModel() {
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun setScore(newScore: Int) {
        _score.value = newScore
    }

    fun updateTime(seconds: Int) {
        _timeLeft.value = seconds
    }

    fun startGame(roundDuration: Int) {
        _score.value = 0
        _timeLeft.value = roundDuration
        _isRunning.value = true
    }

    fun finishGame() {
        _isRunning.value = false
    }
}