package edu.cit.cordero.glamsched.features.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cit.cordero.glamsched.R

class ArtistDashboardActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private var userName = "Artist"
    private var userEmail = ""
    private var userRole = "ARTIST"
    private var userId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_dashboard)

        userName  = intent.getStringExtra("USER_NAME") ?: "Artist"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        userRole  = intent.getStringExtra("USER_ROLE") ?: "ARTIST"
        userId    = intent.getLongExtra("USER_ID", 0L)

        bottomNav = findViewById(R.id.bottomNavArtist)

        if (savedInstanceState == null) {
            loadFragment(ArtistHomeFragment.newInstance(userId, userName, userEmail))
            bottomNav.selectedItemId = R.id.nav_artist_home
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_artist_home -> ArtistHomeFragment.newInstance(userId, userName, userEmail)
                R.id.nav_artist_bookings -> ArtistBookingsFragment.newInstance(userId)
                R.id.nav_artist_services -> ArtistServicesFragment.newInstance(userId)
                R.id.nav_artist_settings -> SettingsFragment.newInstance(userName, userEmail, userRole)
                else -> ArtistHomeFragment.newInstance(userId, userName, userEmail)
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerArtist, fragment)
            .commit()
    }
}
