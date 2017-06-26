package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber

data class CircleModelStorage(val circleCreator: CircleCreator) {
    val circles = ArrayList<CircleModelWrapper>()
    val circleMap = HashMap<CircleModel, CircleModelWrapper>()

    init {
        levelUp(1)
    }

    fun levelUp(currentLevel: Int) {
        circles.clear()
        generateCircles(currentLevel, getNumberOfCircles(currentLevel))
    }

    private fun generateCircles(currentLevel: Int, numberOfCircles: Int) {
        (0..numberOfCircles).mapTo(circles) {
            val circle = circleCreator.createCircleForLevel(it.toLong(), currentLevel)
            circleMap.put(circle.circle, circle)
            Timber.d("added circle: " + circle.toString())
            circle
        }
    }

    private fun getNumberOfCircles(currentLevel: Int): Int {
        return currentLevel + 2
    }

    fun isLevelComplete(): Boolean = circles.size == 0

    fun currentCircle(): CircleModelWrapper = circles[0]

    fun reset() {
        circles.clear()
        circleMap.clear()
        levelUp(1)
    }

    fun removeCircle(circle: CircleModel) {
        circles.remove(circleMap[circle])
        circleMap.remove(circle)
    }

    fun hasCircle(circleToShow: CircleModel) = circleMap.contains(circleToShow)
}