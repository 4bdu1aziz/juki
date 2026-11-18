package com.example.juki

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.juki.db.AppDatabase
import com.example.juki.db.ScoreEntity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var db: AppDatabase

    private val viewModel: GameViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getInstance(requireContext())
        gameView = view.findViewById(R.id.gameView)
        tvScore = view.findViewById(R.id.tvScore)
        tvTimer = view.findViewById(R.id.tvTimer)
        btnStart = view.findViewById(R.id.btnStart)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.score.collect { score ->
                tvScore.text = "Очки: $score"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timeLeft.collect { time ->
                tvTimer.text = "Время: $time"
            }
        }

        gameView.currentScore = viewModel.score.value

        gameView.onScoreChanged = { delta ->
            val currentScore = viewModel.score.value
            viewModel.setScore(currentScore + delta)
        }

        gameView.onTimerChanged = { seconds ->
            viewModel.updateTime(seconds)
        }

        gameView.onRoundFinished = { stats ->
            viewModel.finishGame()
            val current = CurrentPlayer.player
            if (current != null) {
                val finalRating = stats.score * gameView.gameSettings.speed
                lifecycleScope.launch {
                    db.scoreDao().insert(
                        ScoreEntity(
                            playerId = current.id,
                            score = finalRating,
                            hits = stats.hits,
                            misses = stats.misses,
                            accuracy = stats.accuracy,
                            difficulty = gameView.gameSettings.speed,
                            roundDuration = gameView.gameSettings.roundDurationSec
                        )
                    )
                }
            }
            showGameOverDialog(stats)
        }

        // Проверка: если был поворот и игра шла, восстанавливаем её
        if (viewModel.isRunning.value) {
            btnStart.visibility = View.GONE
            gameView.resumeGame(viewModel.score.value, viewModel.timeLeft.value)
        } else {
            btnStart.visibility = View.VISIBLE
            tvTimer.text = "Время: --"
        }

        btnStart.setOnClickListener {
            val currentSettings = GameSettings(
                speed = GlobalSettings.speed,
                maxRoaches = GlobalSettings.maxRoaches,
                bonusIntervalSec = GlobalSettings.bonusIntervalSec,
                roundDurationSec = GlobalSettings.roundDurationSec
            )
            gameView.gameSettings = currentSettings
            btnStart.visibility = View.GONE
            viewModel.startGame(currentSettings.roundDurationSec)
            gameView.startGame()
        }
    }

    private fun showGameOverDialog(stats: GameStats) {
        val finalRating = stats.score * gameView.gameSettings.speed

        AlertDialog.Builder(requireContext())
            .setTitle("Раунд окончен")
            .setMessage("""
                Очки: ${stats.score}
                Рейтинг (Очки × Сложность): $finalRating
                Попаданий: ${stats.hits}
                Промахов: ${stats.misses}
                Точность: ${"%.1f".format(stats.accuracy)}%
            """.trimIndent())
            .setCancelable(false)
            .setPositiveButton("Играть снова") { _, _ ->
                btnStart.visibility = View.GONE
                viewModel.startGame(gameView.gameSettings.roundDurationSec)
                gameView.startGame()
            }
            .setNegativeButton("Выход") { _, _ ->
                btnStart.visibility = View.VISIBLE
                tvTimer.text = "Время: --"
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        gameView.stopGame()
    }
}