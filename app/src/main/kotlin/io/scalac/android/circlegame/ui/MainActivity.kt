package io.scalac.android.circlegame.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.scalac.android.circlegame.R
import io.scalac.android.circlegame.engine.CircleCreator
import io.scalac.android.circlegame.engine.CircleModelStorage
import io.scalac.android.circlegame.engine.Engine
import io.scalac.android.circlegame.engine.EngineView
import io.scalac.android.circlegame.model.CircleModel

class MainActivity : AppCompatActivity(), EngineView {

    private val levelCreator by lazy { CircleCreator(deviceWidth, deviceHeight) }
    private val engine: Engine by lazy {
        Engine(circleModelStorage = CircleModelStorage(levelCreator))
    }

    private val container: FrameLayout by lazy { findViewById(R.id.container) as FrameLayout }
    private val level: TextView by lazy { findViewById(R.id.level) as TextView }
    private val points: TextView by lazy { findViewById(R.id.points) as TextView }
    private val lives: TextView by lazy { findViewById(R.id.lives) as TextView }
    private val retry: View by lazy {
        val retryButton = findViewById(R.id.retry)
        retryButton.setOnClickListener { view -> engine.reset() }
        retryButton
    }

    private val circleToViewMap: HashMap<CircleModel, View?> = HashMap()

    private val deviceWidth: Int by lazy { resources.displayMetrics.widthPixels }
    private val deviceHeight: Int by lazy { resources.displayMetrics.heightPixels }
    private val maxRadius: Int by lazy { deviceWidth / 2 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        engine.engineView = this
        engine.startEngine()
        updateTexts()
    }

    override fun onPause() {
        engine.stopEngine()
        engine.engineView = null
        super.onPause()
    }

    override fun onLevelChanged(currentLevel: Int) = runOnUiThread {
        level.text = currentLevel.toString()
    }

    override fun onShowCircle(circle: CircleModel) {
        runOnUiThread {
            val i: ImageView = ImageView(this)
            i.setImageResource(R.drawable.circle)
            i.adjustViewBounds = true
            i.layoutParams = FrameLayout.LayoutParams(circle.radius * 2, circle.radius * 2)
            i.x = circle.x.toFloat()
            i.y = circle.y.toFloat()
            i.setOnClickListener { _ ->
                engine.onCircleClicked(circle)
                removeCircleFromView(circle)
                updateTexts()
            }

            circleToViewMap[circle] = i

            container.addView(i)
        }
    }

    private fun updateTexts() {
        points.text = engine.points.toString()
        lives.text = engine.lives.toString()
        level.text = engine.currentLevel.toString()
    }

    override fun onCircleMissed(circleModel: CircleModel) = runOnUiThread {
        removeCircleFromView(circleModel)
        updateTexts()
    }

    override fun onGameEnded() = runOnUiThread {
        updateTexts()
        retry.visibility = View.VISIBLE
        Toast.makeText(this, R.string.game_ended, Toast.LENGTH_LONG).show()
    }

    override fun onLevelCompleted() = runOnUiThread {
        updateTexts()
        Toast.makeText(this, R.string.level_up, Toast.LENGTH_LONG).show()
    }

    override fun onGameStarted() = runOnUiThread {
        retry.visibility = View.GONE
        updateTexts()
    }

    private fun removeCircleFromView(circle: CircleModel) {
        val view = circleToViewMap[circle]
        container.removeView(view)
        circleToViewMap[circle] = null
    }
}