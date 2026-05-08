package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cit.cordero.glamsched.R

class AboutGlamSchedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_page)

        findViewById<ImageButton>(R.id.btnLegalBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvLegalHeaderTitle).text = "About GlamSched"
        findViewById<TextView>(R.id.tvLegalTitle).text = "About GlamSched"
        findViewById<TextView>(R.id.tvLegalUpdated).text = "Version 1.0.0"
        findViewById<TextView>(R.id.tvLegalBody).text = """
            GlamSched helps clients discover beauty services and book appointments with trusted artists.

            What you can do:
            • Browse services by category
            • Save favorites and follow artists
            • Book and track appointments
            • Manage profile and settings
            • Connect with skilled beauty professionals

            Our goal is to make beauty booking simple, clear, and convenient for both clients and artists.

            Thank you for using GlamSched.
        """.trimIndent()
    }
}
