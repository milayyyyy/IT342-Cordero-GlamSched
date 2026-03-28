package edu.cit.cordero.glamsched.ui.register

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.api.RetrofitClient
import edu.cit.cordero.glamsched.databinding.ActivityRegisterBinding
import edu.cit.cordero.glamsched.model.request.RegisterRequest
import edu.cit.cordero.glamsched.model.response.ApiResponse
import edu.cit.cordero.glamsched.model.response.AuthResponse
import edu.cit.cordero.glamsched.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedRole: String = "CLIENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleToggle()
        setupClickListeners()
    }

    private fun setupRoleToggle() {
        // Default to CLIENT
        binding.toggleRole.check(R.id.btnClient)
        updateToggleAppearance(R.id.btnClient)

        binding.toggleRole.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedRole = when (checkedId) {
                    R.id.btnArtist -> "ARTIST"
                    else -> "CLIENT"
                }
                updateToggleAppearance(checkedId)
            }
        }
    }

    private fun updateToggleAppearance(checkedId: Int) {
        val buttons = listOf(binding.btnClient, binding.btnArtist)
        for (btn in buttons) {
            if (btn.id == checkedId) {
                btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.garnet))
                btn.setTextColor(ContextCompat.getColor(this, R.color.white))
                btn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.garnet))
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                btn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.role_border))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (fullName.isEmpty()) {
            binding.etFullName.error = getString(R.string.error_name_required)
            binding.etFullName.requestFocus()
            return false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_invalid_email)
            binding.etEmail.requestFocus()
            return false
        }

        if (password.length < 8) {
            binding.etPassword.error = getString(R.string.error_password_min)
            binding.etPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = getString(R.string.error_password_mismatch)
            binding.etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun performRegistration() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        setLoading(true)

        val request = RegisterRequest(
            fullName = fullName,
            email = email,
            password = password,
            role = selectedRole
        )

        RetrofitClient.authApi.register(request).enqueue(object : Callback<ApiResponse<AuthResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<AuthResponse>>,
                response: Response<ApiResponse<AuthResponse>>
            ) {
                setLoading(false)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.registration_success),
                        Toast.LENGTH_LONG
                    ).show()
                    // Navigate to login
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorMsg = body?.error?.message ?: getString(R.string.registration_failed)
                    Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<AuthResponse>>, t: Throwable) {
                setLoading(false)
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.network_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }
}
