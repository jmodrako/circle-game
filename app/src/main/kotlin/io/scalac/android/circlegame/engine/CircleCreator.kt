package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel
import java.lang.Math.max
import java.util.*

// TODO: Serek plis dodaj magic nubersy do stałych :)
class CircleCreator(val deviceWidth: Int, val deviceHeight: Int) {
    val random = Random()

    fun createCircleForLevel(id: Long, currentLevel: Int): CircleModelWrapper {
        val radius = radius(currentLevel)
        val circle = CircleModel(id, radius, positionX(radius), positionY(radius))
        val sustainMs = sustainMs(currentLevel)
        return CircleModelWrapper(circle, sustainMs, nextDelayMs(currentLevel))
    }

    private fun nextDelayMs(currentLevel: Int) = sustainMs(currentLevel)

    private fun sustainMs(currentLevel: Int) = getMinValue(currentLevel) + getRandomValue(currentLevel)

    private fun getRandomValue(currentLevel: Int): Long = random.nextInt(1000 - currentLevel * 10).toLong()

    private fun getMinValue(currentLevel: Int): Long = max(0, 900 - currentLevel * 10).toLong()

    private fun positionX(radius: Int): Int = random.nextInt(deviceWidth - radius)
    private fun positionY(radius: Int): Int = random.nextInt(deviceHeight - radius)

    private fun radius(currentLevel: Int) = max(10, 50 - currentLevel * 10) +
            random.nextInt(max(deviceWidth / 2, 50 - currentLevel * 10))
}