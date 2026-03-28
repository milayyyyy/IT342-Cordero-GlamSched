package edu.cit.cordero.glamsched.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.databinding.ActivityDashboardBinding
import edu.cit.cordero.glamsched.ui.login.LoginActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val userRole = intent.getStringExtra("USER_ROLE") ?: ""

        binding.tvWelcome.text = getString(R.string.welcome_user, userName)
        binding.tvUserEmail.text = userEmail
        binding.tvUserRole.text = userRole

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
