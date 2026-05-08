package edu.cit.cordero.glamsched.features.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.shared.SessionManager
import edu.cit.cordero.glamsched.features.dashboard.ClientDashboardActivity
import edu.cit.cordero.glamsched.features.dashboard.ArtistDashboardActivity

class SplashActivity : AppCompatActivity() {

    private val splashDelayMs = 2200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_splash)

        val content = findViewById<View>(R.id.llSplashContent)
        val title = findViewById<TextView>(R.id.tvSplashTitle)
        val accent = findViewById<View>(R.id.vSplashAccent)
        val progress = findViewById<ProgressBar>(R.id.pbSplash)

        title.post {
            accent.layoutParams = accent.layoutParams.apply { width = title.width - 64 }
            accent.requestLayout()
        }

        val fadeIn = ObjectAnimator.ofFloat(content, "alpha", 0f, 1f).apply { duration = 700 }
        val scaleX = ObjectAnimator.ofFloat(content, "scaleX", 0.92f, 1f).apply { duration = 800 }
        val scaleY = ObjectAnimator.ofFloat(content, "scaleY", 0.92f, 1f).apply { duration = 800 }
        val progressFade = ObjectAnimator.ofFloat(progress, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 600
        }

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(fadeIn, scaleX, scaleY, progressFade)
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({ navigateNext() }, splashDelayMs)
    }

    private fun navigateNext() {
        if (SessionManager.isLoggedIn(this)) {
            val role = SessionManager.getUserRole(this)
            val target = if (role.equals("ARTIST", ignoreCase = true))
                ArtistDashboardActivity::class.java else ClientDashboardActivity::class.java
            startActivity(Intent(this, target).apply {
                putExtra("USER_NAME",  SessionManager.getUserName(this@SplashActivity))
                putExtra("USER_EMAIL", SessionManager.getUserEmail(this@SplashActivity))
                putExtra("USER_ROLE",  SessionManager.getUserRole(this@SplashActivity))
                putExtra("USER_ID",    SessionManager.getUserId(this@SplashActivity))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
