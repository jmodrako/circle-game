package io.scalac.android.circlegame.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.scalac.android.circlegame.R
import io.scalac.android.circlegame.engine.Engine
import io.scalac.android.circlegame.engine.EngineView
import io.scalac.android.circlegame.engine.LevelCreator
import io.scalac.android.circlegame.model.CircleModel

class MainActivity : AppCompatActivity(), EngineView {

    private val levelCreator by lazy { LevelCreator() }
    private val engine: Engine by lazy {
        Engine(engineView = this, level = levelCreator.createSimpleLevel(
                onLevelChangedListener = this))
    }

    private val container: FrameLayout by lazy { findViewById(R.id.container) as FrameLayout }
    private val level: TextView by lazy { findViewById(R.id.level) as TextView }

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
        engine.startEngine()
    }

    override fun onPause() {
        engine.stopEngine()
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
            i.setOnClickListener { v ->
                engine.onCircleClicked(circle)
                removeCircleFromView(circle)
            }

            circleToViewMap[circle] = i

            container.addView(i)
        }
    }

    override fun onCircleMissed(circleModel: CircleModel) = runOnUiThread {
        removeCircleFromView(circleModel)
    }

    override fun onLevelCompleted() = runOnUiThread {
        level.text = "Level completed!!!"
    }

    private fun removeCircleFromView(circle: CircleModel) {
        val view = circleToViewMap[circle]
        container.removeView(view)
        circleToViewMap[circle] = null
    }
}