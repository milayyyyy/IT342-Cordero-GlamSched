package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R

class TermsOfServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_page)

        findViewById<ImageButton>(R.id.btnLegalBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvLegalHeaderTitle).text = "Terms of Service"
        findViewById<TextView>(R.id.tvLegalTitle).text = "Terms of Service"
        findViewById<TextView>(R.id.tvLegalUpdated).text = "Last updated: May 2026"
        findViewById<TextView>(R.id.tvLegalBody).text = """
            Welcome to GlamSched. By using this app, you agree to these terms.

            1) Account responsibilities
            • Provide accurate and updated information
            • Keep your password secure
            • You are responsible for activity under your account

            2) Booking and service terms
            • Artists manage service availability and pricing
            • Clients must provide correct booking details
            • Cancellations and updates are subject to platform behavior

            3) Acceptable use
            • No abusive, fraudulent, or harmful activity
            • No misuse of platform features or data
            • Violation may lead to account restrictions

            4) Platform updates
            • We may improve or modify features at any time
            • Terms can be updated, and continued use implies acceptance

            If you do not agree with these terms, please discontinue use of the app.
        """.trimIndent()
    }
}
