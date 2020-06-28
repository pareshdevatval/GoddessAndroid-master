package com.krystal.goddesslifestyle.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.WaveView
import kotlinx.android.synthetic.main.no_favorites_recipes.*

class TestingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.no_opinions)
        setWaveAnimation(R.color.violet)
    }

    /* Wave Animation */
    @SuppressLint("NewApi")
    private fun setWaveAnimation(color: Int) {
        /* Left to Right */
        wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (AppUtils.getAnimationSize(this) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                this.getColor(color),
                this.getColor(color),
                0.5f,
                (1000 + Math.random() * 600).toLong(),
                true
            )
        )
        /* Left to Right */
        wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (AppUtils.getAnimationSize(this) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                this.getColor(color),
                this.getColor(color),
                0.5f,
                (2000 + Math.random() * 1000).toLong(),
                true
            )
        )

        /* Right to Left */
        wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (AppUtils.getAnimationSize(this) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                this.getColor(color),
                this.getColor(color),
                0.5f,
                (2000 + Math.random() * 1000).toLong(),
                false
            )
        )
        wave.startAnimation()
    }
}
