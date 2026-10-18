package com.example.juki

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onScoreChanged: ((Int) -> Unit)? = null
    var onTimerChanged: ((Int) -> Unit)? = null
    var onRoundFinished: ((GameStats) -> Unit)? = null

    var gameSettings = GameSettings(5, 10, 15, 60)

    private val bugs = mutableListOf<Bug>()
    private var score = 0
    private var hits = 0
    private var misses = 0
    private var roundTimeLeftMs = 0L
    private var lastFrameTime = 0L
    private var running = false
    private var nextId = 0L
    private var nextSpawnTimeMs = 0L
    private var lastReportedSeconds = -1

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun startGame() {
        bugs.clear()
        score = 0
        hits = 0
        misses = 0
        roundTimeLeftMs = gameSettings.roundDurationSec * 1000L
        running = true
        nextSpawnTimeMs = 0L
        lastReportedSeconds = -1
        lastFrameTime = System.currentTimeMillis()

        onScoreChanged?.invoke(score)
        onTimerChanged?.invoke(gameSettings.roundDurationSec)
        invalidate()
    }

    fun stopGame() {
        running = false
        bugs.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!running) return

        val now = System.currentTimeMillis()
        val dt = (now - lastFrameTime) / 1000f
        lastFrameTime = now

        roundTimeLeftMs -= (dt * 1000).toLong()
        if (roundTimeLeftMs <= 0) {
            finishRound()
            return
        }

        val sec = (roundTimeLeftMs / 1000).toInt()
        if (sec != lastReportedSeconds) {
            lastReportedSeconds = sec
            onTimerChanged?.invoke(sec)
        }

        val speedBase = 0.15f * (gameSettings.speed / 5f)
        for (bug in bugs) {
            bug.x += bug.vx * speedBase * bug.type.speedFactor * dt
            bug.y += bug.vy * speedBase * bug.type.speedFactor * dt

            val half = bug.size / 2f
            if (bug.x < half) { bug.x = half; bug.vx = -bug.vx }
            if (bug.x > 1f - half) { bug.x = 1f - half; bug.vx = -bug.vx }
            if (bug.y < half) { bug.y = half; bug.vy = -bug.vy }
            if (bug.y > 1f - half) { bug.y = 1f - half; bug.vy = -bug.vy }
        }

        // 3. Спавн новых жуков по таймеру
        if (bugs.size < gameSettings.maxRoaches) {
            if (now >= nextSpawnTimeMs) {
                bugs.add(spawnBug())

                val baseDelay = 1500L - (gameSettings.speed * 100L)
                val randomDelay = Random.nextLong(0, 500) // + случайная задержка до 0.5 сек
                nextSpawnTimeMs = now + baseDelay + randomDelay
            }
        }

        for (bug in bugs) {
            val left = (bug.x - bug.size / 2f) * width
            val top = (bug.y - bug.size / 2f) * height
            val right = (bug.x + bug.size / 2f) * width
            val bottom = (bug.y + bug.size / 2f) * height

            val drawable = ContextCompat.getDrawable(context, bug.drawableRes)
            drawable?.bounds = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            drawable?.draw(canvas)
        }

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && running) {
            val tx = event.x / width
            val ty = event.y / height

            val hit = bugs.lastOrNull { it.bounds().contains(tx, ty) }

            if (hit != null) {
                bugs.remove(hit)
                score += hit.type.points
                hits++
                onScoreChanged?.invoke(score)
            } else {
                misses++
                score -= 1
                onScoreChanged?.invoke(score)
            }
            invalidate()
        }
        return true
    }

    private fun spawnBug(): Bug {
        val type = when {
            Random.nextFloat() < 0.1f -> BugType.RARE
            Random.nextFloat() < 0.4f -> BugType.FAST
            else -> BugType.NORMAL
        }

        val drawableRes = when (type) {
            BugType.NORMAL -> R.drawable.bug_normal
            BugType.FAST -> R.drawable.bug_fast
            BugType.RARE -> R.drawable.bug_rare
        }

        return Bug(
            id = nextId++,
            x = Random.nextFloat().coerceIn(0.15f, 0.85f), // Чтобы не спавнился прямо в углу
            y = Random.nextFloat().coerceIn(0.15f, 0.85f),
            vx = if (Random.nextBoolean()) 1f else -1f, // Случайное направление по X
            vy = if (Random.nextBoolean()) 1f else -1f, // Случайное направление по Y
            type = type,
            drawableRes = drawableRes
        )
    }

    private fun finishRound() {
        running = false
        // Защита от деления на ноль, если игрок вообще не тапал
        val accuracy = if (hits + misses > 0) hits * 100f / (hits + misses) else 0f
        onRoundFinished?.invoke(
            GameStats(
                score = score,
                hits = hits,
                misses = misses,
                accuracy = accuracy
            )
        )
    }
}

data class GameStats(
    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Float
)