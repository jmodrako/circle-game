package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel

interface EngineView : LevelChangedListener {
    fun onShowCircle(circle: CircleModel)
    fun onCircleMissed(circleModel: CircleModel)
    fun onLevelCompleted()
    fun onGameEnded()
    fun onGameStarted()
}