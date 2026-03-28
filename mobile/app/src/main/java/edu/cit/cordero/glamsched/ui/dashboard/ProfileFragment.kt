package edu.cit.cordero.glamsched.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    companion object {
        private const val ARG_NAME = "USER_NAME"
        private const val ARG_EMAIL = "USER_EMAIL"
        private const val ARG_ROLE = "USER_ROLE"

        fun newInstance(name: String, email: String, role: String): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_EMAIL, email)
                    putString(ARG_ROLE, role)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString(ARG_NAME) ?: "User"
        val email = arguments?.getString(ARG_EMAIL) ?: ""
        val role = arguments?.getString(ARG_ROLE) ?: "CLIENT"

        view.findViewById<TextView>(R.id.tvInitial).text = name.first().uppercase()
        view.findViewById<TextView>(R.id.tvProfileName).text = name
        view.findViewById<TextView>(R.id.tvProfileEmail).text = email
        view.findViewById<TextView>(R.id.tvProfileRole).text = role.replaceFirstChar { it.uppercase() }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
