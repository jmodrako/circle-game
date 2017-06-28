package io.scalac.android.circlegame.common

import timber.log.Timber

inline fun logInvocation(message: String, block: () -> Unit) {
    Timber.d("Invocation of: $message")
    block()
}