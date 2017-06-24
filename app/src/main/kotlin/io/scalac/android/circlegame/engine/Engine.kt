package io.scalac.android.circlegame.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.SystemClock
import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber

class Engine(
        val engineView: EngineView,
        val level: Level) : Handler.Callback {

    init {
        Timber.plant(Timber.DebugTree())
    }

    private val visibleCircles = mutableSetOf<CircleModel>()
    private lateinit var workerHandler: Handler
    private lateinit var workerThread: HandlerThread

    fun startEngine() {
        workerThread = HandlerThread("engine-worker")
        workerThread.start()

        workerHandler = Handler(workerThread.looper, this)
        workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE_MSG, 2000L)
    }

    fun stopEngine() {
        level.clearLevelChangedListener()
        workerThread.quitSafely()
    }


    fun onCircleClicked(circle: CircleModel) {
        val currentCircle = level.currentCircle()

        visibleCircles.remove(circle)
        level.levelUp()

        if (level.isLevelComplete()) {
            onLevelCompleted()
        } else {
            workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE_MSG, currentCircle.nextDelayMs)
        }
    }

    private fun onLevelCompleted() {
        stopEngine()
        engineView.onLevelCompleted()
    }

    override fun handleMessage(msg: Message?): Boolean {
        if (msg?.what == SHOW_CIRCLE_MSG) {
            val showNewCircleRunnable = Runnable {
                val circleWrapper = level.currentCircle()
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
            currentCircle: CircleModel,
            circleWrapper: CircleModelWrapper,
            currentCircleHideTime: Long) {

        val onCircleSustainEndRunnable = Runnable {
            if (didUserMissCircle(currentCircle)) { // User missed circle ;(
                onCircleMissed(currentCircle)

                workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE_MSG, circleWrapper.nextDelayMs)
            }
        }

        workerHandler.postAtTime(onCircleSustainEndRunnable, currentCircleHideTime)
    }

    private fun didUserMissCircle(circleToShow: CircleModel) = visibleCircles.contains(circleToShow)

    private fun sendCircleToView(circleToShow: CircleModel) {
        Timber.d("view.onShowCircle: ${level.currentLevel}")

        visibleCircles.add(circleToShow)
        engineView.onShowCircle(circleToShow)
    }

    private fun onCircleMissed(circleToShow: CircleModel) {
        engineView.onCircleMissed(circleToShow)
        visibleCircles.remove(circleToShow)

        level.levelDown()
    }

    companion object {
        private val SHOW_CIRCLE_MSG = 23
    }
}