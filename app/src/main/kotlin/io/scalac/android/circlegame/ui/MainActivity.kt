package io.scalac.android.circlegame.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.scalac.android.circlegame.R
import io.scalac.android.circlegame.engine.Engine
import io.scalac.android.circlegame.engine.EngineView
import io.scalac.android.circlegame.model.CircleModel

class MainActivity : AppCompatActivity(), EngineView {

    private val engine: Engine by lazy { Engine(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun showCircle(circle: CircleModel) {

    }
}
