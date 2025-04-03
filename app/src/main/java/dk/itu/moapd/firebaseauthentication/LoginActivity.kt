package dk.itu.moapd.firebaseauthentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.activities.MainActivity

class LoginActivity : AppCompatActivity() {

    // Register for FirebaseUI sign-in result
    private val signInLauncher =
        registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result -> onSignInResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if there's already a user logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, go directly to MainActivity
            startMainActivity()
            return
        }

        // Begin authentication flow
        createSignInIntent()
    }

    private fun createSignInIntent() {
        // Authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            // Add anonymous provider
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.baseline_pest_control_rodent_24)
            .setTheme(R.style.Theme_CopenhagenBuzz)
            .apply {
                setTosAndPrivacyPolicyUrls(
                    "https://firebase.google.com/terms/",
                    "https://firebase.google.com/policies/…"
                )
            }
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                if (user?.isAnonymous == true) {
                    showSnackbar("Signed in as guest.")
                } else {
                    showSnackbar("User logged in the app.")
                }
                startMainActivity()
            }
            else -> {
                // Sign in failed
                showSnackbar("Sign in failed.")
                // Try again
                createSignInIntent()
            }
        }
    }

    private fun startMainActivity() {
        // Create a new intent to start MainActivity
        val intent = Intent(this, MainActivity::class.java)
        // Add flag to indicate user is logged in
        intent.putExtra("isLoggedIn", true)
        // Start the MainActivity with this intent
        startActivity(intent)
        // Finish the LoginActivity so user can't go back to it
        finish()
    }

    private fun showSnackbar(message: String) {
        window.decorView.rootView?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}