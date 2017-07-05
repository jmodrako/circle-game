package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel

interface EngineView : LevelChangedListener {
    fun onShowCircle(circle: CircleModel)
    fun onRemoveCircle(circleModel: CircleModel)
    fun onLevelCompleted()
    fun onGameEnded(points: Int)
    fun onGameStarted()
    fun onGameStateChanged(newState: GameState)
}