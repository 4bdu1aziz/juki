package com.example.juki

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

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

    private var currentBonus: Bonus? = null
    private var nextBonusTimeMs = 0L
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var tiltX = 0f
    private var tiltY = 0f
    private var isGravityMode = false
    private var gravityModeEndTime = 0L

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
        .build()
    private var screamSoundId = 0

    init {
        screamSoundId = soundPool.load(context, R.raw.bug_scream, 1)
    }

    fun startGame() {
        bugs.clear()
        currentBonus = null
        score = 0
        hits = 0
        misses = 0
        roundTimeLeftMs = gameSettings.roundDurationSec * 1000L
        running = true
        isGravityMode = false

        val now = System.currentTimeMillis()
        lastFrameTime = now
        nextSpawnTimeMs = now
        nextBonusTimeMs = now + 15000L
        lastReportedSeconds = -1

        onScoreChanged?.invoke(score)
        onTimerChanged?.invoke(gameSettings.roundDurationSec)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        invalidate()
    }

    fun stopGame() {
        running = false
        isGravityMode = false
        bugs.clear()
        currentBonus = null
        sensorManager.unregisterListener(this) // Отключаем датчик
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

        if (isGravityMode && now > gravityModeEndTime) {
            isGravityMode = false
        }

        val speedBase = 0.15f * (gameSettings.speed / 5f)
        for (bug in bugs) {
            if (isGravityMode) {
                // Физика наклона (гравитация)
                bug.x += (tiltX / 5f) * speedBase * dt * 4f
                bug.y += (tiltY / 5f) * speedBase * dt * 4f
            } else {
                bug.x += bug.vx * speedBase * bug.type.speedFactor * dt
                bug.y += bug.vy * speedBase * bug.type.speedFactor * dt
            }

            val half = bug.size / 2f
            if (bug.x < half) { bug.x = half; bug.vx = abs(bug.vx) }
            if (bug.x > 1f - half) { bug.x = 1f - half; bug.vx = -abs(bug.vx) }
            if (bug.y < half) { bug.y = half; bug.vy = abs(bug.vy) }
            if (bug.y > 1f - half) { bug.y = 1f - half; bug.vy = -abs(bug.vy) }
        }

        if (bugs.size < gameSettings.maxRoaches) {
            if (now >= nextSpawnTimeMs) {
                bugs.add(spawnBug())

                val baseDelay = 1500L - (gameSettings.speed * 100L)
                val randomDelay = Random.nextLong(0, 500)
                nextSpawnTimeMs = now + baseDelay + randomDelay
            }
        }

        if (currentBonus == null && now >= nextBonusTimeMs) {
            currentBonus = Bonus(
                x = Random.nextFloat().coerceIn(0.2f, 0.8f),
                y = Random.nextFloat().coerceIn(0.2f, 0.8f)
            )
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

        currentBonus?.let { bonus ->
            val left = (bonus.x - bonus.size / 2f) * width
            val top = (bonus.y - bonus.size / 2f) * height
            val right = (bonus.x + bonus.size / 2f) * width
            val bottom = (bonus.y + bonus.size / 2f) * height

            val drawable = ContextCompat.getDrawable(context, R.drawable.bonus)
            drawable?.bounds = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            drawable?.draw(canvas)
        }

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && running) {
            val tx = event.x / width
            val ty = event.y / height

            val bonus = currentBonus
            if (bonus != null) {
                val bonusRect = Rect(
                    ((bonus.x - bonus.size / 2f) * width).toInt(),
                    ((bonus.y - bonus.size / 2f) * height).toInt(),
                    ((bonus.x + bonus.size / 2f) * width).toInt(),
                    ((bonus.y + bonus.size / 2f) * height).toInt()
                )
                if (bonusRect.contains(event.x.toInt(), event.y.toInt())) {
                    isGravityMode = true
                    gravityModeEndTime = System.currentTimeMillis() + 5000L
                    currentBonus = null
                    nextBonusTimeMs = System.currentTimeMillis() + 15000L

                    soundPool.play(screamSoundId, 1f, 1f, 0, 0, 1f)
                    invalidate()
                    return true
                }
            }

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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            tiltX = event.values[0]
            tiltY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

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
            x = Random.nextFloat().coerceIn(0.15f, 0.85f),
            y = Random.nextFloat().coerceIn(0.15f, 0.85f),
            vx = if (Random.nextBoolean()) 1f else -1f,
            vy = if (Random.nextBoolean()) 1f else -1f,
            type = type,
            drawableRes = drawableRes
        )
    }

    private fun finishRound() {
        running = false
        isGravityMode = false
        sensorManager.unregisterListener(this)
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