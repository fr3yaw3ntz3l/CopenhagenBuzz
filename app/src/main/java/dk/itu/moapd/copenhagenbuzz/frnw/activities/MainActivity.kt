package dk.itu.moapd.copenhagenbuzz.frnw.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.frnw.services.LocationService
import dk.itu.moapd.firebaseauthentication.LoginActivity

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    // Toggle for managing the navigation drawer state
    private lateinit var toggle: ActionBarDrawerToggle

    private var isLoggedIn: Boolean = false
    private lateinit var database: FirebaseDatabase

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Auth state listener to detect authentication changes
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        // Update isLoggedIn flag based on current auth state
        isLoggedIn = firebaseAuth.currentUser != null

        // Update UI elements
        updateNavHeader()
        updateBottomNavigationVisibility()
    }

    // Location permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Log.d(TAG, "Location permission granted")
            startLocationService()
        } else {
            Log.d(TAG, "Location permission denied")
            Snackbar.make(
                binding.root,
                "Location permission denied. Maps functionality will be limited.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // Location broadcast receiver
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                try {
                    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(LocationService.EXTRA_LOCATION, Location::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
                    }

                    location?.let {
                        Log.d(TAG, "Location update received: ${it.latitude}, ${it.longitude}")
                        // You could update a ViewModel here if needed
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing location update", e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        // Inflate layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Initialize Firebase Database using the global variable
            database = FirebaseDatabase.getInstance(MyApplication.DATABASE_URL)

            // Set up the top app bar
            setSupportActionBar(binding.topAppBar)

            // Set up the navigation drawer layout
            val drawerLayout = binding.drawerLayout
            val navView = binding.navView

            // Set up ActionBarDrawerToggle for drawer navigation
            toggle = ActionBarDrawerToggle(
                this, drawerLayout, binding.topAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            // Find NavHostFragment and NavController for fragment navigation
            val navHostFragment =
                supportFragmentManager.findFragmentById(
                    R.id.fragment_container_view
                ) as NavHostFragment
            val navController = navHostFragment.navController

            // Connect bottom navigation to the NavController
            binding.contentMain.bottomNavigation.setupWithNavController(navController)

            // Retrieve login status
            isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)

            // Request location permissions
            requestLocationPermission()

            // Update navigation drawer header with user information
            updateNavHeader()

            // Set up click listeners for navigation drawer menu items
            setupNavigationDrawer(navView)

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    private fun setupNavigationDrawer(navView: com.google.android.material.navigation.NavigationView) {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Handle profile action
                    true
                }
                R.id.nav_settings -> {
                    // Handle settings action
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
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun updateNavHeader() {
        try {
            // Retrieve header view from the navigation drawer
            val headerView = binding.navView.getHeaderView(0)

            // Find header UI elements
            val imageViewPhoto = headerView.findViewById<ImageView>(R.id.imageViewPhoto)
            val textViewName = headerView.findViewById<TextView>(R.id.textViewName)
            val textViewEmail = headerView.findViewById<TextView>(R.id.textViewEmail)

            // Get current user from Firebase Authentication
            val currentUser = auth.currentUser

            // Populate header with user details if available
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
        } catch (e: Exception) {
            Log.e(TAG, "Error updating nav header", e)
        }
    }

    override fun onStart() {
        super.onStart()

        try {
            // Check if user is logged in
            val currentUser = auth.currentUser
            if (currentUser == null) {
                // Not logged in, redirect to login activity
                startLoginActivity()
            } else {
                // User is logged in, update the flag
                isLoggedIn = true

                // Update UI elements
                updateNavHeader()
                updateBottomNavigationVisibility()
            }

            // Register location receiver
            LocalBroadcastManager.getInstance(this).registerReceiver(
                locationReceiver,
                IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
            )

            // Start location service if we have permissions
            if (hasLocationPermission()) {
                startLocationService()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onStart", e)
        }
    }

    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
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

    private fun updateBottomNavigationVisibility() {
        try {
            val bottomNav = binding.contentMain.bottomNavigation
            val menu = bottomNav.menu

            // Get the add event menu item
            val addEventItem = menu.findItem(R.id.fragment_add_event)

            // Check if user is fully authenticated based on our isLoggedIn flag
            // and that they're not an anonymous user
            val user = auth.currentUser
            val isFullyAuthenticated = isLoggedIn && user != null && !user.isAnonymous

            // Set visibility based on authentication status
            addEventItem?.isVisible = isFullyAuthenticated
        } catch (e: Exception) {
            Log.e(TAG, "Error updating bottom nav visibility", e)
        }
    }

    private fun requestLocationPermission() {
        try {
            if (!hasLocationPermission()) {
                Log.d(TAG, "Requesting location permissions")
                // Request both FINE and COARSE location permissions
                requestPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                Log.d(TAG, "Location permission already granted")
                startLocationService()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting location permission", e)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationService() {
        try {
            Log.d(TAG, "Starting location service")
            val serviceIntent = Intent(this, LocationService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            Log.d(TAG, "Location service started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location service", e)
        }
    }

    override fun onStop() {
        super.onStop()

        try {
            // Unregister location receiver
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStop", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            // Stop location service if the app is being completely destroyed
            stopService(Intent(this, LocationService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
}