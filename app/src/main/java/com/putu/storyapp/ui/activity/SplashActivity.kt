package com.putu.storyapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.putu.storyapp.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var _splashBind: ActivitySplashBinding? = null
    private val splashBind get() = _splashBind


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _splashBind = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBind?.root)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed ({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DELAY.toLong())
    }

    companion object {
        const val SPLASH_DELAY = 3000
    }
}