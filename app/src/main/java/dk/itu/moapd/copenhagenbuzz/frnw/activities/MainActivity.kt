/*
 * This file is part of CopenhagenBuzz
 *
 * Copyright (c) 2025 Freya Nørlund Wentzel
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the root of this project for more details.
 */

package dk.itu.moapd.copenhagenbuzz.frnw.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.frnw.R
import android.content.Intent
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.frnw.fragments.UserInfoDialogFragment


/**
 * The main activity of the CopenhagenBuzz application.
 *
 * This activity is responsible for handling the main UI components, including user input fields
 * and an event submission button. It initializes the view binding and manages user interactions.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    // A set of private constants used in this class.
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    private var isLoggedIn: Boolean = false

    /**
     * This method is called, when the activity is created.
     *
     * This method sets up the UI, initializes ViewBinding, and handles the "+" (add event) button clicks.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if available.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Initialize viewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Find NavHostFragment and NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.fragment_container_view
            ) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up the toolbar
        setSupportActionBar(binding.topAppBar)

        // Retrieve login status
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)

        binding.contentMain.bottomNavigation.setupWithNavController(navController)


    }

    override fun onStart() {
        super.onStart()

        // Redirect the user to the LoginActivity
        // if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    private fun startLoginActivity() {
        // Make sure the package path is correct
        Intent(this, dk.itu.moapd.firebaseauthentication.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    /**
     *
     */

    override fun onOptionsItemSelected(item: MenuItem):
        Boolean = when (item.itemId) {
        // Handle top app bar menu item clicks.
        R.id.action_user_info -> {
            UserInfoDialogFragment<Any>().apply {
                isCancelable = false
            }.also { dialogFragment ->
                dialogFragment.show(
                    supportFragmentManager,
                    "UserInfoDialogFragment"
                )
            }
            true
        }

        R.id.action_logout -> {
            auth.signOut()
            startLoginActivity()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}


