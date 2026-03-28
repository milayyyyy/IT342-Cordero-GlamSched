package edu.cit.cordero.glamsched.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.databinding.ActivityArtistDashboardBinding
import edu.cit.cordero.glamsched.ui.login.LoginActivity

class ArtistDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtistDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("USER_NAME") ?: "User"

        binding.tvWelcome.text = getString(R.string.welcome_user, userName)

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
