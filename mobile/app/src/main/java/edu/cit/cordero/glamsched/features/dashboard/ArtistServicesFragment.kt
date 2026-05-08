package edu.cit.cordero.glamsched.features.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.adapter.ServicePostAdapter
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistServicesFragment : Fragment() {

    private var artistId: Long = 0L
    private lateinit var adapter: ServicePostAdapter
    private var tvEmptyRef: TextView? = null
    private var tvCountRef: TextView? = null
    private val addServiceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            tvEmptyRef?.let { empty ->
                tvCountRef?.let { count ->
                    loadServices(empty, count)
                }
            }
        }

    companion object {
        fun newInstance(artistId: Long) = ArtistServicesFragment().apply {
            arguments = Bundle().apply { putLong("ARTIST_ID", artistId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_artist_services, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        artistId = arguments?.getLong("ARTIST_ID") ?: 0L

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvArtistServices)
        val tvEmpty = view.findViewById<TextView>(R.id.tvArtistServicesEmpty)
        val tvCount = view.findViewById<TextView>(R.id.tvArtistServiceCount)
        tvEmptyRef = tvEmpty
        tvCountRef = tvCount

        adapter = ServicePostAdapter(
            items = emptyList(),
            artistManageMode = true,
            onEditClick = { service -> openEditService(service) },
            onDeleteClick = { service -> confirmDelete(service) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btnAddArtistService)?.setOnClickListener {
            val intent = Intent(requireContext(), AddServiceActivity::class.java).apply {
                putExtra("ARTIST_ID", artistId)
            }
            addServiceLauncher.launch(intent)
        }

        loadServices(tvEmpty, tvCount)
    }

    private fun loadServices(tvEmpty: TextView, tvCount: TextView) {
        RetrofitClient.glamApi.getServicesByArtist(artistId, artistId)
            .enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
                override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                    val data = response.body()?.data ?: emptyList()
                    adapter.updateItems(data)
                    tvEmpty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                    tvCount.text = "${data.size} ${if (data.size == 1) "service" else "services"}"
                }
                override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                    if (isAdded) Toast.makeText(requireContext(), "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun openEditService(service: ServiceDto) {
        val intent = Intent(requireContext(), AddServiceActivity::class.java).apply {
            putExtra("ARTIST_ID", artistId)
            putExtra("SERVICE_ID", service.id)
            putExtra("SERVICE_NAME", service.name ?: "")
            putExtra("SERVICE_DESCRIPTION", service.description ?: "")
            putExtra("SERVICE_CATEGORY", service.category ?: "Other")
            putExtra("SERVICE_PRICE", service.price ?: 0.0)
            putStringArrayListExtra("SERVICE_PHOTOS", ArrayList(service.photos ?: emptyList()))
        }
        addServiceLauncher.launch(intent)
    }

    private fun confirmDelete(service: ServiceDto) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Service?")
            .setMessage("This will remove '${service.name ?: "this service"}' permanently.")
            .setPositiveButton("Delete") { _, _ ->
                RetrofitClient.glamApi.deleteService(service.id)
                    .enqueue(object : Callback<ApiResponse<String>> {
                        override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                            if (!isAdded) return
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(requireContext(), "Service deleted", Toast.LENGTH_SHORT).show()
                                tvEmptyRef?.let { empty ->
                                    tvCountRef?.let { count ->
                                        loadServices(empty, count)
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), response.body()?.error?.message ?: "Could not delete service", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                            if (!isAdded) return
                            Toast.makeText(requireContext(), "Failed to delete service", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        tvEmptyRef?.let { empty ->
            tvCountRef?.let { count ->
                loadServices(empty, count)
            }
        }
    }
}
