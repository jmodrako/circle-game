package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel
import java.lang.Math.max
import java.util.*

class CircleCreator(val deviceWidth: Int, val deviceHeight: Int) {
    val random = Random()

    fun createCircleForLevel(id: Long, currentLevel: Int): CircleModelWrapper {
        val radius = radius(currentLevel)
        val circle = CircleModel(id, radius, positionX(radius), positionY(radius))
        return CircleModelWrapper(circle, sustainMs(currentLevel), nextDelayMs(currentLevel))
    }

    private fun positionX(radius: Int): Int = random.nextInt(deviceWidth - radius * 3 / 4)

    private fun positionY(radius: Int): Int = random.nextInt(deviceHeight - radius * 3 / 4)

    private fun nextDelayMs(currentLevel: Int) = sustainMs(currentLevel)

    private fun sustainMs(currentLevel: Int): Long {
        val staticPart = getStaticPart(currentLevel, MIN_DELAY, MAX_DELAY_LEVEL_BONUS)
        val randomPart = getRandomPart(currentLevel, 0, MAX_DELAY_RANDOM_PART)
        return (staticPart + randomPart).toLong()
    }

    private fun radius(currentLevel: Int): Int {
        val staticPart = getStaticPart(currentLevel, MIN_RADIUS, MAX_RADIUS_LEVEL_BONUS)
        val randomPart = getRandomPart(currentLevel, 0, getMaxRadiusRandomPart(currentLevel))
        return staticPart + randomPart
    }

    private fun getMaxRadiusRandomPart(currentLevel: Int) = deviceWidth / 2 - getLevelMultipler(currentLevel)

    private fun getRandomPart(currentLevel: Int, min: Int, max: Int): Int {
        return max(min, random.nextInt(max - getLevelMultipler(currentLevel)))
    }

    private fun getStaticPart(currentLevel: Int, min: Int, max: Int): Int {
        return max(min, max - getLevelMultipler(currentLevel))
    }

    private fun getLevelMultipler(currentLevel: Int) = currentLevel * LEVEL_MULTIPLIER

    companion object {
        val MIN_DELAY = 100
        val MAX_DELAY_LEVEL_BONUS = 900
        val MAX_DELAY_RANDOM_PART = 1000

        val MIN_RADIUS = 25
        val MAX_RADIUS_LEVEL_BONUS = 50

        val LEVEL_MULTIPLIER = 10
    }
}