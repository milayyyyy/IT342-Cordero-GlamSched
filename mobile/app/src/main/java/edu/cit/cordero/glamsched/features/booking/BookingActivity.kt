package edu.cit.cordero.glamsched.features.booking

import android.app.DatePickerDialog
import androidx.appcompat.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.AppointmentDto
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private var selectedDate = ""
    private var selectedTime = ""
    private var paymentMethod = "CARD"
    private var ewalletProvider = "GCash"
    private val baseTimeSlots = listOf(
        "09:00", "10:00", "11:00", "12:00",
        "13:00", "14:00", "15:00", "16:00",
        "17:00", "18:00"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val serviceId = intent.getLongExtra("SERVICE_ID", 0L)
        val artistId = intent.getLongExtra("ARTIST_ID", 0L)
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""
        val artistName = intent.getStringExtra("ARTIST_NAME") ?: ""
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val userId = intent.getLongExtra("USER_ID", 0L)

        val tvTitle = findViewById<TextView>(R.id.tvBookingServiceName)
        val tvArtist = findViewById<TextView>(R.id.tvBookingArtistName)
        val tvPrice = findViewById<TextView>(R.id.tvBookingPrice)
        val btnPickDate = findViewById<MaterialButton>(R.id.btnPickDate)
        val tvDate = findViewById<TextView>(R.id.tvSelectedDate)
        val btnPickTime = findViewById<MaterialButton>(R.id.btnPickTime)
        val tvTime = findViewById<TextView>(R.id.tvSelectedTime)
        val etNotes = findViewById<EditText>(R.id.etBookingNotes)
        val btnConfirm = findViewById<MaterialButton>(R.id.btnConfirmBooking)
        val btnBack = findViewById<ImageButton>(R.id.btnBookingBack)
        val progressBar = findViewById<ProgressBar>(R.id.pbBooking)

        tvTitle.text = serviceName
        tvArtist.text = "by $artistName"
        tvPrice.text = "₱${String.format("%.2f", servicePrice)}"

        btnBack.setOnClickListener { finish() }

        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
                tvDate.visibility = View.VISIBLE
                tvDate.text = "✓ ${formatDateDisplay(selectedDate)}"
                btnPickDate.text = "📅  ${formatDateDisplay(selectedDate)}"
                btnPickDate.setTextColor(resources.getColor(R.color.ink_black, null))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .also { it.datePicker.minDate = System.currentTimeMillis() }
                .show()
        }

        btnPickTime.setOnClickListener {
            if (selectedDate.isBlank()) {
                toast("Please select a date first")
                return@setOnClickListener
            }
            showAvailableTimeSlots(
                artistId = artistId,
                date = selectedDate,
                onPick = { picked ->
                    selectedTime = picked
                    tvTime.visibility = View.VISIBLE
                    tvTime.text = "✓ ${formatTime12hr(selectedTime)}"
                    btnPickTime.text = "🕐  ${formatTime12hr(selectedTime)}"
                    btnPickTime.setTextColor(resources.getColor(R.color.ink_black, null))
                }
            )
        }

        setupPaymentMethodTabs()
        setupEwalletProviders()
        setupCardFormatters()

        btnConfirm.setOnClickListener {
            if (selectedDate.isBlank()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedTime.isBlank()) {
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!validatePaymentInput()) return@setOnClickListener

            val appointment = AppointmentDto(
                clientId = userId,
                artistId = artistId,
                serviceId = serviceId,
                serviceName = serviceName,
                artistName = artistName,
                date = selectedDate,
                time = selectedTime,
                notes = etNotes.text.toString().trim(),
                paymentMethod = paymentMethodLabel()
            )
            btnConfirm.isEnabled = false
            progressBar.visibility = View.VISIBLE

            RetrofitClient.glamApi.createAppointment(appointment)
                .enqueue(object : Callback<ApiResponse<AppointmentDto>> {
                    override fun onResponse(call: Call<ApiResponse<AppointmentDto>>, response: Response<ApiResponse<AppointmentDto>>) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful && (response.body()?.success == true)) {
                            Toast.makeText(this@BookingActivity, "Booking confirmed! ✨", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            btnConfirm.isEnabled = true
                            val apiMsg = response.body()?.error?.message
                            val rawMsg = runCatching { response.errorBody()?.string() }.getOrNull()
                            val msg = when {
                                !apiMsg.isNullOrBlank() -> apiMsg
                                rawMsg?.contains("not found", true) == true || response.code() == 404 ->
                                    "Booking endpoint not found. Please restart/update backend."
                                response.code() >= 500 ->
                                    "Server error while booking. Please try again."
                                else -> "Booking failed. Please check details and try again."
                            }
                            Toast.makeText(this@BookingActivity, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse<AppointmentDto>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnConfirm.isEnabled = true
                        val networkHint = if (t.message?.contains("failed to connect", true) == true ||
                            t.message?.contains("timeout", true) == true
                        ) {
                            "Cannot connect to server. Ensure backend is running."
                        } else {
                            "Booking failed. ${t.message ?: "Try again."}"
                        }
                        Toast.makeText(this@BookingActivity, networkHint, Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    private fun setupPaymentMethodTabs() {
        val tabs = listOf(
            Triple("CARD", R.id.pmCard, R.id.pmCardContent),
            Triple("EWALLET", R.id.pmEwallet, R.id.pmEwalletContent),
            Triple("WALKIN", R.id.pmWalkin, R.id.pmWalkinContent)
        )

        tabs.forEach { (key, tabId, _) ->
            findViewById<TextView>(tabId).setOnClickListener {
                paymentMethod = key
                tabs.forEach { (k, tId, contentId) ->
                    val tab = findViewById<TextView>(tId)
                    val content = findViewById<View>(contentId)
                    if (k == paymentMethod) {
                        tab.setBackgroundResource(R.drawable.bg_chip_selected)
                        tab.setTextColor(resources.getColor(R.color.white, null))
                        content.visibility = View.VISIBLE
                    } else {
                        tab.setBackgroundResource(R.drawable.bg_chip_unselected)
                        tab.setTextColor(resources.getColor(R.color.ink_black, null))
                        content.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupEwalletProviders() {
        val providers = listOf(
            "GCash" to R.id.ewGcash,
            "Maya" to R.id.ewMaya,
            "GrabPay" to R.id.ewGrab
        )
        fun refresh() {
            providers.forEach { (name, id) ->
                val view = findViewById<TextView>(id)
                if (name == ewalletProvider) {
                    view.setBackgroundResource(R.drawable.bg_chip_selected)
                    view.setTextColor(resources.getColor(R.color.white, null))
                    view.setTypeface(null, Typeface.BOLD)
                } else {
                    view.setBackgroundResource(R.drawable.bg_chip_unselected)
                    view.setTextColor(resources.getColor(R.color.ink_black, null))
                    view.setTypeface(null, Typeface.BOLD)
                }
            }
        }
        providers.forEach { (name, id) ->
            findViewById<TextView>(id).setOnClickListener {
                ewalletProvider = name
                refresh()
            }
        }
        refresh()
    }

    private fun setupCardFormatters() {
        val etNumber = findViewById<EditText>(R.id.etCardNumber)
        etNumber.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val digits = s.toString().replace(" ", "")
                val grouped = digits.chunked(4).joinToString(" ")
                if (grouped != s.toString()) {
                    etNumber.setText(grouped)
                    etNumber.setSelection(grouped.length.coerceAtMost(grouped.length))
                }
                isEditing = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val etExpiry = findViewById<EditText>(R.id.etCardExpiry)
        etExpiry.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val digits = s.toString().replace("/", "").take(4)
                val formatted = if (digits.length >= 3) "${digits.substring(0, 2)}/${digits.substring(2)}" else digits
                if (formatted != s.toString()) {
                    etExpiry.setText(formatted)
                    etExpiry.setSelection(formatted.length)
                }
                isEditing = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showAvailableTimeSlots(artistId: Long, date: String, onPick: (String) -> Unit) {
        RetrofitClient.glamApi.getArtistAppointments(artistId).enqueue(
            object : Callback<ApiResponse<List<AppointmentDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<AppointmentDto>>>,
                    response: Response<ApiResponse<List<AppointmentDto>>>
                ) {
                    val appointments = response.body()?.data.orEmpty()
                    val blocked = appointments
                        .filter { it.date == date && !it.status.equals("CANCELLED", true) }
                        .mapNotNull { normalizeTime(it.time) }
                        .toSet()

                    val availableSlots = baseTimeSlots.filterNot { blocked.contains(it) }
                    if (availableSlots.isEmpty()) {
                        toast("No available slots for this date")
                        return
                    }

                    val dialogView = LayoutInflater.from(this@BookingActivity)
                        .inflate(R.layout.dialog_time_slots, null)
                    dialogView.findViewById<TextView>(R.id.tvSlotDialogDate).text =
                        formatDateDisplay(date)
                    val llSlots = dialogView.findViewById<LinearLayout>(R.id.llTimeSlots)

                    val dialog = AlertDialog.Builder(this@BookingActivity)
                        .setView(dialogView, 32, 18, 32, 8)
                        .create()

                    val gap = (8 * resources.displayMetrics.density).toInt()
                    availableSlots.chunked(3).forEach { rowSlots ->
                        val row = LinearLayout(this@BookingActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = gap }
                        }
                        rowSlots.forEach { slot ->
                            val slotBtn = com.google.android.material.button.MaterialButton(this@BookingActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                ).apply {
                                    marginStart = gap / 2
                                    marginEnd = gap / 2
                                }
                                text = formatTime12hr(slot)
                                setTextColor(resources.getColor(R.color.white, null))
                                textSize = 13f
                                typeface = Typeface.DEFAULT_BOLD
                                backgroundTintList = android.content.res.ColorStateList.valueOf(
                                    resources.getColor(R.color.garnet, null)
                                )
                                strokeWidth = 0
                                insetTop = 0
                                insetBottom = 0
                                cornerRadius = (14 * resources.displayMetrics.density).toInt()
                                setPadding(
                                    (8 * resources.displayMetrics.density).toInt(),
                                    (12 * resources.displayMetrics.density).toInt(),
                                    (8 * resources.displayMetrics.density).toInt(),
                                    (12 * resources.displayMetrics.density).toInt()
                                )
                                setOnClickListener {
                                    onPick(slot)
                                    dialog.dismiss()
                                }
                            }
                            row.addView(slotBtn)
                        }
                        repeat(3 - rowSlots.size) {
                            row.addView(View(this@BookingActivity), LinearLayout.LayoutParams(0, 0, 1f))
                        }
                        llSlots.addView(row)
                    }

                    dialog.show()
                }

                override fun onFailure(call: Call<ApiResponse<List<AppointmentDto>>>, t: Throwable) {
                    toast("Could not load available slots")
                }
            }
        )
    }

    private fun validatePaymentInput(): Boolean {
        when (paymentMethod) {
            "CARD" -> {
                val number = findViewById<EditText>(R.id.etCardNumber).text.toString().replace(" ", "")
                val expiry = findViewById<EditText>(R.id.etCardExpiry).text.toString()
                val cvv = findViewById<EditText>(R.id.etCardCvv).text.toString()
                if (number.length < 13) { toast("Enter a valid card number"); return false }
                if (!expiry.matches(Regex("\\d{2}/\\d{2}"))) { toast("Enter expiry as MM/YY"); return false }
                if (cvv.length < 3) { toast("Enter a valid CVV"); return false }
            }
            "EWALLET" -> {
                val phone = findViewById<EditText>(R.id.etEwalletPhone).text.toString()
                if (phone.length < 10) { toast("Enter a valid mobile number"); return false }
            }
            "WALKIN" -> { /* no input required */ }
        }
        return true
    }

    private fun paymentMethodLabel(): String = when (paymentMethod) {
        "CARD" -> "Card"
        "EWALLET" -> "E-wallet · $ewalletProvider"
        "WALKIN" -> "Walk-in (Cash)"
        else -> "Card"
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun normalizeTime(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        // Handles "18:00:00" and "18:00"
        return raw.trim().take(5).takeIf { it.matches(Regex("\\d{2}:\\d{2}")) }
    }

    private fun formatDateDisplay(raw: String): String = try {
        val src = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dst = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        dst.format(src.parse(raw)!!)
    } catch (_: Exception) { raw }

    private fun formatTime12hr(raw: String): String = try {
        val src = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dst = SimpleDateFormat("h:mm a", Locale.getDefault())
        dst.format(src.parse(raw)!!)
    } catch (_: Exception) { raw }
}
