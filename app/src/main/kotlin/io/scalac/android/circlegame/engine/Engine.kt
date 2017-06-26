package io.scalac.android.circlegame.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.SystemClock
import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber
import kotlin.properties.Delegates

class Engine(val circleModelStorage: CircleModelStorage) : Handler.Callback {

    init {
        Timber.plant(Timber.DebugTree())
    }

    private lateinit var workerHandler: Handler
    private lateinit var workerThread: HandlerThread

    var points = 0
    var engineView: EngineView? = null
    var lives = LIVES_COUNT

    var currentLevel: Int by Delegates.observable(1, {
        _, _, newValue ->
        engineView?.onLevelChanged(newValue)
    })
        private set
        get

    fun startEngine() {
        Timber.d("startEngine ")
        workerThread = HandlerThread("engine-worker")
        workerThread.start()

        workerHandler = Handler(workerThread.looper, this)
        workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE_MSG, 2000L)
    }

    fun stopEngine() {
        Timber.d("stopEngine")
        workerThread.quitSafely()
    }

    fun onCircleClicked(circle: CircleModel) {
        val currentCircle = circleModelStorage.currentCircle()
        points += currentLevel
        circleModelStorage.removeCircle(circle)
        continueGame(currentCircle)
    }

    private fun continueGame(currentCircle: CircleModelWrapper) {
        if (circleModelStorage.isLevelComplete()) {
            onLevelCompleted()
        } else {
            workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE_MSG, currentCircle.nextDelayMs)
        }
    }

    private fun onLevelCompleted() {
        stopEngine()
        currentLevel++
        engineView?.onLevelCompleted()
        circleModelStorage.levelUp(currentLevel)
        startEngine()
    }

    override fun handleMessage(msg: Message?): Boolean {
        if (msg?.what == SHOW_CIRCLE_MSG) {
            val showNewCircleRunnable = Runnable {
                val circleWrapper = circleModelStorage.currentCircle()
                val currentCircleHideTime = SystemClock.uptimeMillis() + circleWrapper.sustainMs

                val currentCircle = circleWrapper.circle
                sendCircleToView(currentCircle)

                scheduleHideCurrentCircle(currentCircle, circleWrapper, currentCircleHideTime)
            }

            workerHandler.post(showNewCircleRunnable)
        }
        return true
    }

    private fun scheduleHideCurrentCircle(
            currentCircle: CircleModel, circleWrapper: CircleModelWrapper, currentCircleHideTime: Long) {

        val onCircleSustainEndRunnable = Runnable {
            if (didUserMissCircle(currentCircle)) { // User missed circle ;(
                onCircleMissed(circleWrapper)
            }
        }

        workerHandler.postAtTime(onCircleSustainEndRunnable, currentCircleHideTime)
    }

    private fun didUserMissCircle(circleToShow: CircleModel) = circleModelStorage.hasCircle(circleToShow)

    private fun sendCircleToView(circleToShow: CircleModel) {
        Timber.d("view.onShowCircle: $circleToShow")
        engineView?.onShowCircle(circleToShow)
    }

    private fun onCircleMissed(circleWrapper: CircleModelWrapper) {
        Timber.d("onCircleMissed " + lives)

        points--
        lives--
        engineView?.onCircleMissed(circleWrapper.circle)
        circleModelStorage.removeCircle(circleWrapper.circle)

        if (lives == 0) {
            stopEngine()
            engineView?.onGameEnded()
        } else {
            workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE_MSG, circleWrapper.nextDelayMs)
            if (circleModelStorage.isLevelComplete()) {
                onLevelCompleted()
            }
        }
    }

    companion object {
        private val SHOW_CIRCLE_MSG = 23
        private val LIVES_COUNT = 5
    }

    fun reset() {
        lives = LIVES_COUNT
        points = 0
        currentLevel = 1
        circleModelStorage.reset()
        startEngine()
        engineView?.onGameStarted()
    }
}