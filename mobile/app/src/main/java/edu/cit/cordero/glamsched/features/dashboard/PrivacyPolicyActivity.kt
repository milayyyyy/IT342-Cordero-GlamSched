package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_page)

        findViewById<ImageButton>(R.id.btnLegalBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvLegalHeaderTitle).text = "Privacy Policy"
        findViewById<TextView>(R.id.tvLegalTitle).text = "Privacy Policy"
        findViewById<TextView>(R.id.tvLegalUpdated).text = "Last updated: May 2026"
        findViewById<TextView>(R.id.tvLegalBody).text = """
            We value your privacy and protect your data in GlamSched.

            1) Information we collect
            • Account data such as name, email, and role
            • Profile details like photo, bio, phone, and address (if provided)
            • Booking records, favorites, and follows to enable app features

            2) How we use your data
            • To provide booking, payment history, and account services
            • To improve app performance and user experience
            • To maintain platform security and prevent abuse

            3) Data sharing
            • We do not sell your personal data
            • Data is only used for platform functionality and required operations

            4) Your control
            • You can update your profile information anytime
            • You can request account deletion from settings

            By continuing to use GlamSched, you acknowledge this policy.
        """.trimIndent()
    }
}
