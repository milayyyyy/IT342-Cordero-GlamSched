package edu.cit.cordero.glamsched.ui.booking

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R

class AdditionalNotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_notes)

        val etNotes = findViewById<EditText>(R.id.etNotes)
        val tvCharCount = findViewById<TextView>(R.id.tvCharCount)

        etNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tvCharCount.text = "${s?.length ?: 0}/500"
            }
        })

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnContinue).setOnClickListener {
            val intent = Intent(this, BookingConfirmationActivity::class.java)
            startActivity(intent)
        }
    }
}
