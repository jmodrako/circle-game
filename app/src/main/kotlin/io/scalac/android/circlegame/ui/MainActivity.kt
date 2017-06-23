package io.scalac.android.circlegame.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import io.scalac.android.circlegame.R
import io.scalac.android.circlegame.engine.Engine
import io.scalac.android.circlegame.engine.EngineView
import io.scalac.android.circlegame.model.CircleModel
import java.util.*

class MainActivity : AppCompatActivity(), EngineView {

    private val engine: Engine by lazy { Engine(this) }

    private val container: FrameLayout by lazy { findViewById(R.id.container) as FrameLayout }
    private val deviceWidth: Int by lazy { resources.displayMetrics.widthPixels }
    private val deviceHeight: Int by lazy { resources.displayMetrics.heightPixels }
    private val maxRadius: Int by lazy { deviceWidth / 2 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val random = Random()
        var x: Long = 0
        while (x < 20) {
            x++
            showCircle(CircleModel(x, MIN_RADIUS + random.nextInt(maxRadius),
                    random.nextInt(deviceWidth), random.nextInt(deviceHeight)))
        }

    }

    override fun showCircle(circle: CircleModel) {
        val i: ImageView = ImageView(this)
        i.setImageResource(R.drawable.circle)
        i.adjustViewBounds = true
        i.layoutParams = FrameLayout.LayoutParams(circle.radius * 2, circle.radius * 2)
        i.x = circle.x.toFloat()
        i.y = circle.y.toFloat()
        i.setOnClickListener { v -> onItemClicked(v) }
        container.addView(i)
    }

    private fun onItemClicked(v: View?) {
        container.removeView(v)
    }

    companion object {
        private val MIN_RADIUS = 10
    }
}