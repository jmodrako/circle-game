package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel

class LevelCreator {
    fun createSimpleLevel(onLevelChangedListener: LevelChangedListener) = Level(listOf(
            CircleModelWrapper(
                    CircleModel(id = System.currentTimeMillis(), radius = 100, x = 100, y = 100),
                    sustainMs = 3000, nextDelayMs = 2000),
            CircleModelWrapper(
                    CircleModel(id = System.currentTimeMillis(), radius = 200, x = 200, y = 50),
                    sustainMs = 2000, nextDelayMs = 1000),
            CircleModelWrapper(
                    CircleModel(id = System.currentTimeMillis(), radius = 300, x = 300, y = 120),
                    sustainMs = 1000, nextDelayMs = 3000)), onLevelChangedListener)
}