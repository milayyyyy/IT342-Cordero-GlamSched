package edu.cit.cordero.glamsched.ui.dashboard

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        userName = intent.getStringExtra("USER_NAME") ?: "User"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        userRole = intent.getStringExtra("USER_ROLE") ?: "CLIENT"

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_bookings -> BookingsFragment()
                R.id.nav_profile -> ProfileFragment.newInstance(userName, userEmail, userRole)
                else -> HomeFragment()
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
