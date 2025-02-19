package dk.itu.moapd.copenhagenbuzz.frnw.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.ActivityLoginBinding

/**
 *
 */

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
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
    }
}
