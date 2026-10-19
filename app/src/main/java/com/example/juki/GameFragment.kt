package com.example.juki

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.juki.db.AppDatabase
import com.example.juki.db.ScoreEntity
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var db: AppDatabase

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

        gameView.onScoreChanged = { score ->
            tvScore.text = "Очки: $score"
        }

        gameView.onTimerChanged = { seconds ->
            tvTimer.text = "Время: $seconds"
        }

        gameView.onRoundFinished = { stats ->
            val current = CurrentPlayer.player
            if (current != null) {
                // ИЗМЕНЕНО: Считаем итоговый рейтинг (очки * сложность)
                val finalRating = stats.score * gameView.gameSettings.speed

                lifecycleScope.launch {
                    db.scoreDao().insert(
                        ScoreEntity(
                            playerId = current.id,
                            score = finalRating, // Сохраняем рейтинг
                            hits = stats.hits,
                            misses = stats.misses,
                            accuracy = stats.accuracy,
                            difficulty = gameView.gameSettings.speed,
                            roundDuration = gameView.gameSettings.roundDurationSec // Сохраняем время раунда
                        )
                    )
                }
            }
            showGameOverDialog(stats)
        }

        btnStart.setOnClickListener {
            // Передаем актуальные настройки перед стартом
            val currentSettings = GameSettings(
                speed = GlobalSettings.speed,
                maxRoaches = GlobalSettings.maxRoaches,
                bonusIntervalSec = GlobalSettings.bonusIntervalSec,
                roundDurationSec = GlobalSettings.roundDurationSec
            )
            gameView.gameSettings = currentSettings

            btnStart.visibility = View.GONE
            tvTimer.text = "Время: ${currentSettings.roundDurationSec}"
            gameView.startGame()
        }

        tvTimer.text = "Время: --"
        tvScore.text = "Очки: 0"
    }

    private fun showGameOverDialog(stats: GameStats) {
        // В диалоге показываем "чистые" очки и рейтинг
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
        btnStart.visibility = View.VISIBLE
        tvTimer.text = "Время: --"
    }
}