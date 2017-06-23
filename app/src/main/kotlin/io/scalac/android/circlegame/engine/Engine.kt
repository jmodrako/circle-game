package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel
import timber.log.Timber

interface EngineView {
    fun showCircle(circle: CircleModel)
}

class Engine(val engineView: EngineView) {
    fun onCircleClicked(circle: CircleModel) {
        Timber.d("onCircleClicked: $circle")
    }
}