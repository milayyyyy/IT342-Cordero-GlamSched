package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cit.cordero.glamsched.R

class ClientDashboardActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private var userName = "User"
    private var userEmail = ""
    private var userRole = "CLIENT"
    private var userId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        userName = intent.getStringExtra("USER_NAME") ?: "User"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        userRole = intent.getStringExtra("USER_ROLE") ?: "CLIENT"
        userId = intent.getLongExtra("USER_ID", 0L)

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(FeedFragment.newInstance(userId))
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_feed -> FeedFragment.newInstance(userId)
                R.id.nav_favorites -> FavoritesFragment.newInstance(userId)
                R.id.nav_appointments -> AppointmentsFragment.newInstance(userId)
                R.id.nav_settings -> SettingsFragment.newInstance(userName, userEmail, userRole)
                else -> FeedFragment.newInstance(userId)
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
