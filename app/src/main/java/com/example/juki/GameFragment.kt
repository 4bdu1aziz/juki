package com.example.juki

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameView = view.findViewById(R.id.gameView)
        tvScore = view.findViewById(R.id.tvScore)
        tvTimer = view.findViewById(R.id.tvTimer)
        btnStart = view.findViewById(R.id.btnStart)

        gameView.gameSettings = GameSettings(
            speed = 5,
            maxRoaches = 10,
            bonusIntervalSec = 15,
            roundDurationSec = 60
        )

        gameView.onScoreChanged = { score ->
            tvScore.text = "Очки: $score"
        }

        gameView.onTimerChanged = { seconds ->
            tvTimer.text = "Время: $seconds"
        }

        gameView.onRoundFinished = { stats ->
            showGameOverDialog(stats)
        }

        btnStart.setOnClickListener {
            btnStart.visibility = View.GONE        // скрыть кнопку
            tvTimer.text = "Время: ${gameView.gameSettings.roundDurationSec}"
            gameView.startGame()
        }

        tvTimer.text = "Время: --"
        tvScore.text = "Очки: 0"
    }

    private fun showGameOverDialog(stats: GameStats) {
        AlertDialog.Builder(requireContext())
            .setTitle("Раунд окончен")
            .setMessage("""
                Очки: ${stats.score}
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