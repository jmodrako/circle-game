package io.scalac.android.circlegame.engine

import io.scalac.android.circlegame.model.CircleModel

data class CircleModelWrapper(
        val circle: CircleModel,
        val sustainMs: Long,
        val nextDelayMs: Long)