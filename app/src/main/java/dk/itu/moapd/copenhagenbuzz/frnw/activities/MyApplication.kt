package dk.itu.moapd.copenhagenbuzz.frnw.activities

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

class MyApplication : Application() {
    companion object {
        lateinit var DATABASE_URL: String
        // Add other Firebase config variables as needed
    }

    override fun onCreate() {
        super.onCreate()

        // Load environment variables
        val dotenv = dotenv {
            directory = "/assets"
            filename = ".env"
        }

        // Set the global variables
        DATABASE_URL = dotenv["FIREBASE_DATABASE_URL"]

        // Initialize Firebase Database with the URL
        FirebaseDatabase.getInstance(DATABASE_URL)
    }
}