package edu.cit.cordero.glamsched.ui.loginsuccess

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.databinding.ActivityLoginSuccessBinding
import edu.cit.cordero.glamsched.ui.dashboard.ClientDashboardActivity
import edu.cit.cordero.glamsched.ui.dashboard.ArtistDashboardActivity

class LoginSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: "CLIENT"
        val accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""

        binding.tvSuccessMessage.text = "Welcome back, $userName!\nYou're logged in as ${userRole.lowercase().replaceFirstChar { it.uppercase() }}."
        binding.tvRoleBadge.text = userRole

        binding.btnContinue.setOnClickListener {
            val targetActivity = if (userRole.equals("ARTIST", ignoreCase = true)) {
                ArtistDashboardActivity::class.java
            } else {
                ClientDashboardActivity::class.java
            }

            val intent = Intent(this, targetActivity).apply {
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", userEmail)
                putExtra("USER_ROLE", userRole)
                putExtra("ACCESS_TOKEN", accessToken)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
