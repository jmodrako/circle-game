package io.scalac.android.circlegame.engine

import kotlin.properties.Delegates

data class Level(
        private val circles: List<CircleModelWrapper>,
        private var onLevelChangedListener: LevelChangedListener?) {

    var currentLevel: Int by Delegates.observable(0, {
        _, _, newValue ->
        onLevelChangedListener?.onLevelChanged(newValue)
    })
        private set
        get

    fun levelUp() {
        currentLevel += 1
    }

    fun levelDown() {
        currentLevel = Math.max(currentLevel - 1, 0)
    }

    fun isLevelComplete() = circles.size == currentLevel

    fun currentCircle() = circles[currentLevel]

    fun clearLevelChangedListener() {
        onLevelChangedListener = null
    }
}