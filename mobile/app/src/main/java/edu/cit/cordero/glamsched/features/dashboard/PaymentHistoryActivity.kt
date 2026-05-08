package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.adapter.PaymentHistoryAdapter
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import edu.cit.cordero.glamsched.shared.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: PaymentHistoryAdapter
    private lateinit var rv: RecyclerView
    private lateinit var emptyView: View
    private lateinit var progress: ProgressBar
    private val priceMap = mutableMapOf<Long, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        findViewById<ImageButton>(R.id.btnPaymentHistoryBack).setOnClickListener { finish() }
        rv = findViewById(R.id.rvPaymentHistory)
        emptyView = findViewById(R.id.layoutPaymentEmpty)
        progress = findViewById(R.id.pbPaymentHistory)

        adapter = PaymentHistoryAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val userId = SessionManager.getUserId(this)
        if (userId == 0L) {
            finish()
            return
        }
        progress.visibility = View.VISIBLE
        RetrofitClient.glamApi.getServices(userId).enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                response.body()?.data?.forEach { svc ->
                    priceMap[svc.id] = svc.price ?: 0.0
                }
                loadAppointments(userId)
            }

            override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                loadAppointments(userId)
            }
        })
    }

    private fun loadAppointments(userId: Long) {
        RetrofitClient.glamApi.getAppointments(userId).enqueue(object : Callback<ApiResponse<List<AppointmentDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<AppointmentDto>>>,
                response: Response<ApiResponse<List<AppointmentDto>>>
            ) {
                progress.visibility = View.GONE
                val completed = (response.body()?.data ?: emptyList())
                    .filter { it.status.equals("COMPLETED", true) }
                    .map { appt -> appt.copy(price = appt.price ?: (appt.serviceId?.let { priceMap[it] })) }
                    .sortedByDescending { "${it.date} ${it.time}" }
                adapter.updateItems(completed)
                emptyView.visibility = if (completed.isEmpty()) View.VISIBLE else View.GONE
                rv.visibility = if (completed.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun onFailure(call: Call<ApiResponse<List<AppointmentDto>>>, t: Throwable) {
                progress.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                rv.visibility = View.GONE
                Toast.makeText(this@PaymentHistoryActivity, "Could not load payment history", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
