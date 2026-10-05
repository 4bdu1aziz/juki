package com.example.juki

import android.graphics.RectF

enum class BugType(val points: Int, val speedFactor: Float, val sizeFactor: Float) {
    NORMAL(points = 1, speedFactor = 1.0f, sizeFactor = 1.0f),
    FAST(points = 2, speedFactor = 2.0f, sizeFactor = 0.8f),
    RARE(points = 5, speedFactor = 0.7f, sizeFactor = 1.3f)
}

data class Bug(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val type: BugType,
    val drawableRes: Int
) {
    val size: Float get() = 0.12f * type.sizeFactor

    fun bounds(): RectF = RectF(
        x - size / 2f,
        y - size / 2f,
        x + size / 2f,
        y + size / 2f
    )
}