package io.scalac.android.circlegame.ui

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import io.scalac.android.circlegame.R
import io.scalac.android.circlegame.engine.*
import io.scalac.android.circlegame.highscore.HighScoreManager
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
        retryButton.setOnClickListener { engine.retry() }
        retryButton
    }

    private val startPauseButton: Button by lazy { findViewById(R.id.start_pause_btn) as Button }
    private val highScoreButton: Button by lazy { findViewById(R.id.high_score) as Button }
    private val circleToViewMap: HashMap<CircleModel, View?> = HashMap()
    private val deviceWidth: Int by lazy { resources.displayMetrics.widthPixels }
    private val deviceHeight: Int by lazy { resources.displayMetrics.heightPixels }

    private val highScoreManager: HighScoreManager by lazy { HighScoreManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startPauseButton.setOnClickListener { engine.handleStartPauseClick() }
        highScoreButton.setOnClickListener {
            Toast.makeText(this, "${highScoreManager.allScores()}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        engine.engineView = this
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

    override fun onLevelCompleted() = runOnUiThread {
        updateTexts()
        Toast.makeText(this, R.string.level_up, Toast.LENGTH_LONG).show()
    }

    override fun onGameStarted() = runOnUiThread {
        retry.visibility = View.GONE
        updateTexts()
    }

    override fun onGameEnded(points: Int) = runOnUiThread {
        updateTexts()
        retry.visibility = View.VISIBLE
        Toast.makeText(this, R.string.game_ended, Toast.LENGTH_LONG).show()

        showSaveScoreDialog(points)
    }

    private fun showSaveScoreDialog(score: Int) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_save_score, null)
        val userNameInput = view.findViewById(R.id.dialog_user_input) as EditText

        AlertDialog.Builder(this).setView(view).setTitle(R.string.save_score)
                .setPositiveButton("Save", { _, _ ->
                    highScoreManager.saveScore(userNameInput.text.toString(), score)
                })
                .show()
    }

    override fun onGameStateChanged(newState: GameState) = runOnUiThread {
        when (newState) {
            GameState.PAUSED -> {
                startPauseButton.text = "RESUME"
            }
            GameState.RUNNING -> {
                startPauseButton.text = "PAUSE"
            }
            GameState.STOPPED -> {
                startPauseButton.text = "START"
            }
        }
    }

    private fun removeCircleFromView(circle: CircleModel) {
        val view = circleToViewMap[circle]
        container.removeView(view)
        circleToViewMap[circle] = null
    }
}