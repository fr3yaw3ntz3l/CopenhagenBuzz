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

/**
 * MainActivity serves as the entry point of the application after user authentication.
 * It initializes Firebase authentication, sets up navigation components,
 * and manages the navigation drawer UI.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    // Toggle for managing the navigation drawer state
    private lateinit var toggle: ActionBarDrawerToggle

    // A set of private constants used in this class.
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    private var isLoggedIn: Boolean = false


    /**
     * Called when the activity is first created.
     * Initializes UI components, authentication, and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Inflate layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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

        // Update navigation drawer header with user information
        updateNavHeader()

        // Set up click listeners for navigation drawer menu items
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


    /**
     * Updates the navigation drawer header with the logged-in user's information.
     */
    private fun updateNavHeader() {

        // Retrieve header view from the navigation drawer
        val headerView = binding.navView.getHeaderView(0)

        // Find header UI elements
        val imageViewPhoto = headerView.findViewById<ImageView>(R.id.imageViewPhoto)
        val textViewName = headerView.findViewById<TextView>(R.id.textViewName)
        val textViewEmail = headerView.findViewById<TextView>(R.id.textViewEmail)

        // Get current user from Firebase Authenticatio
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
    }


    /**
     * Called when the activity becomes visible to the user.
     * If the user is not authenticated, they are redirected to the login screen.
     */
    override fun onStart() {
        super.onStart()

        // Redirect the user to the LoginActivity
        // if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }


    /**
     * Starts the login activity and clears the activity stack.
     */
    private fun startLoginActivity() {
        Intent(this, dk.itu.moapd.firebaseauthentication.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }


    /**
     * Inflates the options menu in the top app bar.
     *
     * @param menu The options menu to be inflated.
     * @return True if the menu was created successfully.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }


    /**
     * Handles back press action. If the navigation drawer is open, it closes it;
     * otherwise, it performs the default back navigation.
     */
    override fun onBackPressed() {
        // Close drawer when back is pressed if drawer is open
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}