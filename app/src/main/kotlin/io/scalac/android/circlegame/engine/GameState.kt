package io.scalac.android.circlegame.engine

enum class GameState {
    PAUSED {
        override fun process(newState: GameState, engine: Engine) = when (newState) {
            RUNNING -> engine.resumeEngine()
            STOPPED -> engine.stopEngine()
            PAUSED -> nop()
        }
    },
    RUNNING {
        override fun process(newState: GameState, engine: Engine) = when (newState) {
            RUNNING -> nop()
            STOPPED -> engine.stopEngine()
            PAUSED -> engine.pauseEngine()
        }
    },
    STOPPED {
        override fun process(newState: GameState, engine: Engine) = when (newState) {
            RUNNING -> engine.startEngine()
            STOPPED -> nop()
            PAUSED -> engine.pauseEngine()
        }
    };

    abstract fun process(newState: GameState, engine: Engine)
}