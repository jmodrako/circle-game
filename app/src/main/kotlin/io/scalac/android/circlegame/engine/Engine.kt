package io.scalac.android.circlegame.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.SystemClock
import io.scalac.android.circlegame.common.logInvocation
import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber
import kotlin.properties.Delegates

fun nop() {/*Do nothing.*/
}

class Engine(val circleModelStorage: CircleModelStorage) : Handler.Callback {

    init {
        Timber.plant(Timber.DebugTree())
    }

    private lateinit var workerHandler: Handler
    private lateinit var workerThread: HandlerThread

    var points = 0
        get() {
            return Math.max(field, 0)
        }

    var engineView: EngineView? = null
        set(value) {
            field = value
            gameState = GameState.STOPPED
        }

    var lives = LIVES_COUNT
    var gameState: GameState by Delegates.observable(GameState.STOPPED, { _, oldState, newState ->
        Timber.d("old: $oldState, new: $newState")

        oldState.process(newState, this)
        engineView?.onGameStateChanged(newState)
    })

    var currentLevel: Int by Delegates.observable(1, { _, _, newValue ->
        Timber.d("currentLevel: $currentLevel")

        engineView?.onLevelChanged(newValue)
    })
        private set
        get

    fun onCircleClicked(circle: CircleModel) = logInvocation("onCircleClicked") {
        if (gameState == GameState.RUNNING) {
            logInvocation("onCircleClicked.insideIf") {

                val currentCircle = circleModelStorage.currentCircle()
                points += currentLevel
                circleModelStorage.removeCircle(circle)
                continueGame(currentCircle)
            }
        }
    }

    fun retry() = logInvocation("retry") {
        if (gameState != GameState.RUNNING) {
            lives = LIVES_COUNT
            points = 0
            currentLevel = 1
            circleModelStorage.reset()

            gameState = GameState.RUNNING
            engineView?.onGameStarted()
        }
    }

    fun handleStartPauseClick() = logInvocation("handleStartPauseClick") {
        gameState = when (gameState) {
            GameState.PAUSED -> GameState.RUNNING
            GameState.RUNNING -> GameState.PAUSED
            GameState.STOPPED -> GameState.RUNNING
        }
    }

    internal fun startEngine() = logInvocation("startEngine") {
        workerThread = HandlerThread("engine-worker")
        workerThread.start()

        workerHandler = Handler(workerThread.looper, this)
        workerHandler.sendEmptyMessageDelayed(NEXT_GAME_FRAME_MSG, 2000L)
    }

    internal fun pauseEngine() = logInvocation("pauseEngine") {
        workerHandler.removeMessages(NEXT_GAME_FRAME_MSG)
        workerHandler.removeMessages(HIDE_CURRENT_CIRCLE_MSG)
    }

    internal fun resumeEngine() = logInvocation("resumeEngine") {
        workerHandler.sendEmptyMessage(NEXT_GAME_FRAME_MSG)
    }

    internal fun stopEngine() = logInvocation("stopEngine") {
        gameState = GameState.STOPPED
        workerThread.quitSafely()
    }

    private fun continueGame(currentCircle: CircleModelWrapper) {
        if (circleModelStorage.isLevelComplete()) {
            onLevelCompleted()
        }

        workerHandler.sendEmptyMessageDelayed(NEXT_GAME_FRAME_MSG, currentCircle.nextDelayMs)
    }

    private fun onLevelCompleted() {
        levelUp()
        circleModelStorage.levelUp(currentLevel)
        engineView?.onLevelCompleted()
    }

    private fun levelUp() = logInvocation("levelUp") {
        currentLevel++
    }

    override fun handleMessage(msg: Message?): Boolean {
        when (msg?.what) {
            NEXT_GAME_FRAME_MSG -> {
                val circleWrapper = circleModelStorage.currentCircle()
                val currentCircleHideTime = SystemClock.uptimeMillis() + circleWrapper.sustainMs

                sendCircleToView(circleWrapper.circle)
                scheduleHideCurrentCircle(ScheduleHideCurrentCircleData(circleWrapper, currentCircleHideTime))
            }
            HIDE_CURRENT_CIRCLE_MSG -> {
                val data = msg.obj as ScheduleHideCurrentCircleData
                if (didUserMissCircle(data.circleWrapper.circle)) {
                    onCircleMissed(data.circleWrapper)
                }
            }
        }
        return true
    }

    data class ScheduleHideCurrentCircleData(
            val circleWrapper: CircleModelWrapper,
            val currentCircleHideTime: Long)

    private fun scheduleHideCurrentCircle(data: ScheduleHideCurrentCircleData) {
        val message = workerHandler.obtainMessage(HIDE_CURRENT_CIRCLE_MSG, data)
        workerHandler.sendMessageAtTime(message, data.currentCircleHideTime)
    }

    private fun didUserMissCircle(circleToShow: CircleModel) = circleModelStorage.hasCircle(circleToShow)

    private fun sendCircleToView(circleToShow: CircleModel) = logInvocation("view.onShowCircle: $circleToShow") {
        engineView?.onShowCircle(circleToShow)
    }

    private fun onCircleMissed(circleWrapper: CircleModelWrapper) = logInvocation("onCircleMissed " + lives) {
        points--
        lives--
        engineView?.onCircleMissed(circleWrapper.circle)
        circleModelStorage.removeCircle(circleWrapper.circle)

        if (lives == 0 || circleModelStorage.isLevelComplete()) {
            stopEngine()
            engineView?.onGameEnded(points)

            lives = 0
            points = 0
        } else {
            workerHandler.sendEmptyMessageDelayed(NEXT_GAME_FRAME_MSG, circleWrapper.nextDelayMs)
        }
    }

    companion object {
        private val NEXT_GAME_FRAME_MSG = 1
        private val HIDE_CURRENT_CIRCLE_MSG = 2

        private val LIVES_COUNT = 5
    }
}