package io.scalac.android.circlegame.highscore

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

typealias Score = Pair<String, Int>

class HighScoreManager(context: Context) {
    private val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    fun saveScore(name: String, score: Int) {
        val key = "$KEY_PREFIX$name"
        prefs.edit().putInt(key, score).apply()
    }

    fun allScores(): List<Score> {
        return prefs.all.map {
            val user = it.key.replace(KEY_PREFIX, "")
            val score = it.value as Int
            Score(user, score)
        }
    }

    companion object {
        private val KEY_PREFIX = "key_prefix."
    }
}