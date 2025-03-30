package dk.itu.moapd.copenhagenbuzz.frnw.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.ActivityLoginBinding

/**
 *
 */

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Register for FirebaseUI sign-in result
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("FirebaseAuth", "User signed in: ${user?.displayName} (${user?.email})")

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isLoggedIn", true)
                putExtra("userName", user?.displayName)
                putExtra("userEmail", user?.email)
            }
            startActivity(intent)
            finish()
        } else {
            // Sign-in failed
            Log.e("FirebaseAuth", "Sign-in failed: ${response?.error?.errorCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isLoggedIn", true)
            }
            startActivity(intent)
            finish()
        }

        binding.guestButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isLoggedIn", false)
            }
            startActivity(intent)
            finish()
        }

        binding.loginGoogleButton.setOnClickListener {
            startSignIn()
        }
    }

    private fun startSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(dk.itu.moapd.copenhagenbuzz.frnw.R.drawable.ic_launcher_foreground) // Optional
          //  .setTheme(dk.itu.moapd.copenhagenbuzz.frnw.R.style.Theme_MyApp) // Optional
            .build()

        signInLauncher.launch(signInIntent)
    }
}

