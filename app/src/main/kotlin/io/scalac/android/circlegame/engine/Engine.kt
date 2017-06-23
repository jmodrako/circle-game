package io.scalac.android.circlegame.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.SystemClock
import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber
import kotlin.properties.Delegates

interface LevelChangedListener {
    fun onLevelChanged(currentLevel: Int)
}

interface EngineView : LevelChangedListener {
    fun onShowCircle(circle: CircleModel)
    fun onCircleMissed(circleModel: CircleModel)
    fun onLevelCompleted()
}

data class CircleModelWrapper(
        val circle: CircleModel,
        val sustainMs: Long,
        val nextDelayMs: Long)

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

    companion object {
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

    fun clearLevelChangedListener() {
        onLevelChangedListener = null
    }
}

class Engine(val engineView: EngineView) : Handler.Callback {

    init {
        Timber.plant(Timber.DebugTree())
    }

    private val level = Level.createSimpleLevel(engineView)
    private val visibleCircles = mutableSetOf<CircleModel>()
    private lateinit var workerHandler: Handler
    private lateinit var workerThread: HandlerThread

    fun startEngine() {
        workerThread = HandlerThread("engine-worker")
        workerThread.start()

        workerHandler = Handler(workerThread.looper, this)
        workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE, 2000L)
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
            workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE, currentCircle.nextDelayMs)
        }
    }

    private fun onLevelCompleted() {
        stopEngine()
        engineView.onLevelCompleted()
    }

    override fun handleMessage(msg: Message?): Boolean {
        when (msg?.what) {
            SHOW_CIRCLE -> {
                // Post show now action.
                workerHandler.post({
                    val circleWrapper = level.currentCircle()
                    val currentCircleHideTime = SystemClock.uptimeMillis() + circleWrapper.sustainMs

                    val circleToShow = circleWrapper.circle
                    sendCircleToView(circleToShow)

                    // Schedule hide current circle.
                    workerHandler.postAtTime({
                        // User missed circle ;(
                        if (didUserMissCircle(circleToShow)) {
                            onCircleMissed(circleToShow)

                            workerHandler.sendEmptyMessageDelayed(SHOW_CIRCLE, circleWrapper.nextDelayMs)
                        }
                    }, currentCircleHideTime)
                })
            }
        }
        return true
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
        private val SHOW_CIRCLE = 23
    }
}