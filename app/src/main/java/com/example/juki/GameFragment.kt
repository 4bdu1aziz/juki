package com.example.juki

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameView = view.findViewById(R.id.gameView)
        tvScore = view.findViewById(R.id.tvScore)
        tvTimer = view.findViewById(R.id.tvTimer)

        gameView.gameSettings = GameSettings(
            speed = 5,
            maxRoaches = 10,
            bonusIntervalSec = 15,
            roundDurationSec = 60
        )

        gameView.onScoreChanged = { score ->
            tvScore.text = "Очки: $score"
        }

        gameView.onRoundFinished = { stats ->
            showGameOverDialog(stats)
        }

        gameView.startGame()
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
                gameView.startGame()
            }
            .setNegativeButton("Выход") { _, _ ->
                // ничего не делаем — пользователь остаётся на вкладке
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        gameView.stopGame()
    }
}