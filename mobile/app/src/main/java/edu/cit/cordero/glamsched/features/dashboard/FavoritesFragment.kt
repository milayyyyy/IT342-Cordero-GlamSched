package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

class FavoritesFragment : Fragment() {

    private var userId: Long = 0L
    private lateinit var adapter: ServicePostAdapter

    companion object {
        fun newInstance(userId: Long) = FavoritesFragment().apply {
            arguments = Bundle().apply { putLong("USER_ID", userId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_favorites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getLong("USER_ID") ?: 0L

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFavorites)
        val emptyView = view.findViewById<View>(R.id.tvFavoritesEmpty)
        val tvCount = view.findViewById<TextView>(R.id.tvFavCount)

        adapter = ServicePostAdapter(items = emptyList(), showLovedBanner = true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadFavorites(emptyView, tvCount)
    }

    private fun loadFavorites(emptyView: View, tvCount: TextView) {
        RetrofitClient.glamApi.getServices(userId).enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                val liked = response.body()?.data?.filter { it.likedByMe } ?: emptyList()
                adapter.updateItems(liked)
                emptyView.visibility = if (liked.isEmpty()) View.VISIBLE else View.GONE
                tvCount.text = "❤ ${liked.size}"
            }
            override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
