package dk.itu.moapd.copenhagenbuzz.frnw.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.frnw.R
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    // A set of private constants used in this class.
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Initialize viewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up the toolbar
        setSupportActionBar(binding.topAppBar)

        // Set up drawer layout
        val drawerLayout = binding.drawerLayout
        val navView = binding.navView

        // Set up ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.topAppBar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Find NavHostFragment and NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.fragment_container_view
            ) as NavHostFragment
        val navController = navHostFragment.navController

        binding.contentMain.bottomNavigation.setupWithNavController(navController)

        // Retrieve login status
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)

        // Update navigation drawer header with user information
        updateNavHeader()

        // Set up the navigation view's menu item click listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Handle profile action
                    // You could add specific profile functionality here
                    true
                }
                R.id.nav_settings -> {
                    // Handle settings action
                    // You could add specific settings functionality here
                    true
                }
                R.id.nav_logout -> {
                    auth.signOut()
                    startLoginActivity()
                    true
                }
                else -> false
            }

            // Close the drawer after handling the action
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun updateNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val imageViewPhoto = headerView.findViewById<ImageView>(R.id.imageViewPhoto)
        val textViewName = headerView.findViewById<TextView>(R.id.textViewName)
        val textViewEmail = headerView.findViewById<TextView>(R.id.textViewEmail)

        // Get current user
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Set user name and email
            textViewName.text = user.displayName ?: getString(R.string.user_name)
            textViewEmail.text = user.email ?: getString(R.string.user_email)

            // Load user photo if available
            user.photoUrl?.let { url ->
                imageViewPhoto.imageTintMode = null
                Picasso.get().load(url).into(imageViewPhoto)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Redirect the user to the LoginActivity
        // if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    private fun startLoginActivity() {
        Intent(this, dk.itu.moapd.firebaseauthentication.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onBackPressed() {
        // Close drawer when back is pressed if drawer is open
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}