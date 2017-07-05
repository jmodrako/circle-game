package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber
import java.util.Random

data class CircleModelStorage(val circleCreator: CircleCreator) {

    val circles = ArrayList<CircleModelWrapper>()
    val circleMap = HashMap<CircleModel, CircleModelWrapper>()

    init {
        levelUp(INITIAL_LEVEL)
    }

    fun levelUp(currentLevel: Int) {
        circles.clear()
        generateCircles(currentLevel, getNumberOfCircles(currentLevel))
    }

    private fun generateCircles(currentLevel: Int, numberOfCircles: Int) = (0..numberOfCircles).mapTo(circles) {
        val result = circleCreator.createCircleForLevel(it.toLong(), currentLevel)
        circleMap.put(result.circle, result)
        Timber.d("generateCircles: " + result.toString())
        result
    }

    private fun getNumberOfCircles(currentLevel: Int) = currentLevel + 4

    fun isLevelComplete() = circles.size == 0

    fun currentCircle(): CircleModelWrapper = circles[0]

    fun reset() {
        circles.clear()
        circleMap.clear()
        levelUp(INITIAL_LEVEL)
    }

    fun removeCircle(circle: CircleModel) {
        circles.remove(circleMap[circle])
        circleMap.remove(circle)
    }

    fun hasCircle(circleToShow: CircleModel) = circleMap.contains(circleToShow)

    companion object {
        val INITIAL_LEVEL = 1
    }

    fun reshuffleCurrentCircle(currentLevel: Int) {
        removeCircle(circles[0].circle)
        val newCircle = circleCreator.createCircleForLevel(Random().nextLong(), currentLevel)
        circleMap.put(newCircle.circle, newCircle)
        circles.add(0, newCircle)
    }
}