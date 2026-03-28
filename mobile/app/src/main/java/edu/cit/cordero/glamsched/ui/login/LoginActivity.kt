package edu.cit.cordero.glamsched.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.api.RetrofitClient
import edu.cit.cordero.glamsched.databinding.ActivityLoginBinding
import edu.cit.cordero.glamsched.model.request.LoginRequest
import edu.cit.cordero.glamsched.model.response.ApiResponse
import edu.cit.cordero.glamsched.model.response.AuthResponse
import edu.cit.cordero.glamsched.ui.loginsuccess.LoginSuccessActivity
import edu.cit.cordero.glamsched.ui.register.RegisterActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
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
                    val userName = body.data?.user?.fullName ?: "User"
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.welcome_user, userName),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to Login Success
                    val intent = Intent(this@LoginActivity, LoginSuccessActivity::class.java).apply {
                        putExtra("USER_NAME", body.data?.user?.fullName)
                        putExtra("USER_EMAIL", body.data?.user?.email)
                        putExtra("USER_ROLE", body.data?.user?.role)
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
