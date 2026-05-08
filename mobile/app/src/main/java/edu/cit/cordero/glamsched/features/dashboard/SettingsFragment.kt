package edu.cit.cordero.glamsched.features.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.auth.LoginActivity
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import edu.cit.cordero.glamsched.shared.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class SettingsFragment : Fragment() {

    companion object {
        private const val ARG_NAME = "USER_NAME"
        private const val ARG_EMAIL = "USER_EMAIL"
        private const val ARG_ROLE = "USER_ROLE"

        fun newInstance(name: String, email: String, role: String): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_EMAIL, email)
                    putString(ARG_ROLE, role)
                }
            }
        }
    }

    private lateinit var tvInitial: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString(ARG_NAME) ?: "User"
        val email = arguments?.getString(ARG_EMAIL) ?: ""
        val role = arguments?.getString(ARG_ROLE) ?: "CLIENT"

        tvInitial = view.findViewById(R.id.tvSettingsInitial)
        val tvName = view.findViewById<TextView>(R.id.tvSettingsName)
        val tvEmail = view.findViewById<TextView>(R.id.tvSettingsEmail)
        val tvRole = view.findViewById<TextView>(R.id.tvSettingsRole)

        tvInitial.text = name.first().uppercase()
        tvName.text = name
        tvEmail.text = email
        tvRole.text = role.replaceFirstChar { it.uppercase() }
        loadAvatar(view, name)

        view.findViewById<View>(R.id.rowChangePassword)?.setOnClickListener {
            showChangePasswordDialog()
        }

        view.findViewById<View>(R.id.rowPaymentHistory)?.setOnClickListener {
            startActivity(Intent(requireContext(), PaymentHistoryActivity::class.java))
        }

        view.findViewById<View>(R.id.rowAbout)?.setOnClickListener {
            startActivity(Intent(requireContext(), AboutGlamSchedActivity::class.java))
        }

        view.findViewById<View>(R.id.rowPrivacyPolicy)?.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyPolicyActivity::class.java))
        }

        view.findViewById<View>(R.id.rowTermsOfService)?.setOnClickListener {
            startActivity(Intent(requireContext(), TermsOfServiceActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnSettingsLogout).setOnClickListener {
            SessionManager.clearSession(requireContext())
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun showChangePasswordDialog() {
        val container = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val tilCurrent = container.findViewById<TextInputLayout>(R.id.tilCurrentPassword)
        val tilNew = container.findViewById<TextInputLayout>(R.id.tilNewPassword)
        val tilConfirm = container.findViewById<TextInputLayout>(R.id.tilConfirmPassword)
        val etCurrent = container.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = container.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = container.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(container, 48, 12, 48, 0)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val current = etCurrent.text.toString()
                val newPass = etNew.text.toString()
                val confirm = etConfirm.text.toString()
                tilCurrent.error = null
                tilNew.error = null
                tilConfirm.error = null

                when {
                    current.isBlank() -> tilCurrent.error = "Current password is required"
                    newPass.isBlank() -> tilNew.error = "New password is required"
                    !isStrongPassword(newPass) ->
                        tilNew.error = "Use 8+ chars with uppercase, lowercase, number, and symbol"
                    confirm.isBlank() -> tilConfirm.error = "Please confirm new password"
                    newPass != confirm -> tilConfirm.error = "Passwords do not match"
                    current == newPass -> tilNew.error = "New password must be different"
                    else -> changePassword(current, newPass, dialog)
                }
            }
        }
        dialog.show()
    }

    private fun changePassword(currentPassword: String, newPassword: String, dialog: AlertDialog) {
        val userId = SessionManager.getUserId(requireContext())
        if (userId == 0L) return
        val body = mapOf("currentPassword" to currentPassword, "newPassword" to newPassword)
        RetrofitClient.glamApi.changePassword(userId, body)
            .enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        val message = response.body()?.error?.message ?: "Could not update password"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadAvatar(view: View, fallbackName: String) {
        val ivAvatar = view.findViewById<ImageView>(R.id.ivSettingsAvatar)
        val userId = SessionManager.getUserId(requireContext())
        if (userId == 0L) return

        RetrofitClient.glamApi.getUserById(userId).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                if (!isAdded) return
                val user = response.body()?.data ?: return
                val img = user.profileImage
                if (!img.isNullOrBlank()) {
                    ivAvatar.visibility = View.VISIBLE
                    tvInitial.visibility = View.GONE
                    Glide.with(this@SettingsFragment).load(img).circleCrop().into(ivAvatar)
                } else {
                    ivAvatar.visibility = View.GONE
                    tvInitial.visibility = View.VISIBLE
                    tvInitial.text = fallbackName.first().uppercase()
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                if (!isAdded) return
                ivAvatar.visibility = View.GONE
                tvInitial.visibility = View.VISIBLE
                tvInitial.text = fallbackName.first().uppercase()
            }
        })
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrBlank()) return "Date TBD"
        return runCatching {
            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            outFmt.format(inFmt.parse(date)!!)
        }.getOrDefault(date)
    }

    private fun formatTime12hr(time: String?): String {
        if (time.isNullOrBlank()) return "Time TBD"
        return runCatching {
            val normalized = time.take(8)
            val inFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outFmt.format(inFmt.parse(normalized)!!)
        }.getOrElse { time }
    }

    private fun isStrongPassword(password: String): Boolean {
        val strongRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")
        return strongRegex.matches(password)
    }
}
