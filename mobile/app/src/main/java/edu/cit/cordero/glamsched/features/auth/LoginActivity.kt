package edu.cit.cordero.glamsched.features.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.shared.RetrofitClient
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.SessionManager
import edu.cit.cordero.glamsched.databinding.ActivityLoginBinding
import edu.cit.cordero.glamsched.features.dashboard.ClientDashboardActivity
import edu.cit.cordero.glamsched.features.dashboard.ArtistDashboardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If already logged in, skip the login screen entirely
        if (SessionManager.isLoggedIn(this)) {
            navigateToDashboard(
                userId    = SessionManager.getUserId(this),
                userName  = SessionManager.getUserName(this),
                userEmail = SessionManager.getUserEmail(this),
                userRole  = SessionManager.getUserRole(this)
            )
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun navigateToDashboard(
        userId: Long,
        userName: String,
        userEmail: String,
        userRole: String
    ) {
        val target = if (userRole.equals("ARTIST", ignoreCase = true)) {
            ArtistDashboardActivity::class.java
        } else {
            ClientDashboardActivity::class.java
        }
        startActivity(Intent(this, target).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_EMAIL", userEmail)
            putExtra("USER_ROLE", userRole)
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_invalid_email)
            binding.etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_required)
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        setLoading(true)

        val request = LoginRequest(email = email, password = password)

        RetrofitClient.authApi.login(request).enqueue(object : Callback<ApiResponse<AuthResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<AuthResponse>>,
                response: Response<ApiResponse<AuthResponse>>
            ) {
                setLoading(false)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    val userName  = body.data?.user?.fullName ?: "User"
                    val userEmail = body.data?.user?.email ?: ""
                    val userRole  = body.data?.user?.role ?: "CLIENT"
                    val userId    = body.data?.user?.id ?: 0L

                    // Persist session so the user stays logged in across app restarts
                    SessionManager.saveSession(
                        context   = this@LoginActivity,
                        userId    = userId,
                        userName  = userName,
                        userEmail = userEmail,
                        userRole  = userRole
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.welcome_user, userName),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to Login Success
                    val intent = Intent(this@LoginActivity, LoginSuccessActivity::class.java).apply {
                        putExtra("USER_NAME", userName)
                        putExtra("USER_EMAIL", userEmail)
                        putExtra("USER_ROLE", userRole)
                        putExtra("USER_ID", userId)
                        putExtra("ACCESS_TOKEN", body.data?.accessToken)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = body?.error?.message ?: getString(R.string.login_failed)
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<AuthResponse>>, t: Throwable) {
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.network_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}
