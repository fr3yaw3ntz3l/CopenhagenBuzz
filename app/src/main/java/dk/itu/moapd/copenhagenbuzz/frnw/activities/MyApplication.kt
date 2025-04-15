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
        lateinit var database: FirebaseDatabase
    }

    override fun onCreate() {
        super.onCreate()

       // val apiKey = dotenv["API_KEY"] ?: throw RuntimeException("API_KEY is missing!")

        // Set the global variables
        DATABASE_URL = "https://copenhagenbuzz-frnw-default-rtdb.europe-west1.firebasedatabase.app/"

        database = FirebaseDatabase.getInstance(DATABASE_URL)
        database.setPersistenceEnabled(true)

        val databaseReference = database.reference
        databaseReference.keepSynced(true)

    }
}