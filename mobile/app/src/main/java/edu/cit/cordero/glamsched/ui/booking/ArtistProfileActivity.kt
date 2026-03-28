package edu.cit.cordero.glamsched.ui.booking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R

class ArtistProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_profile)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnBookAppointment).setOnClickListener {
            val intent = Intent(this, SelectDateActivity::class.java)
            startActivity(intent)
        }
    }
}
