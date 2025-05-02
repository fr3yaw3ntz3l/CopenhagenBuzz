package dk.itu.moapd.copenhagenbuzz.frnw.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.activities.MainActivity

class LocationService : Service() {
    private val TAG = "LocationService"

    companion object {
        const val ACTION_LOCATION_UPDATE = "dk.itu.moapd.copenhagenbuzz.frnw.ACTION_LOCATION_UPDATE"
        const val EXTRA_LOCATION = "dk.itu.moapd.copenhagenbuzz.frnw.EXTRA_LOCATION"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "location_service_channel"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        try {
            createNotificationChannel()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        Log.d(TAG, "New location: ${location.latitude}, ${location.longitude}")
                        broadcastLocation(location)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.description = "Used for location updates"
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel", e)
        }
    }

    private fun startLocationUpdates() {
        try {
            Log.d(TAG, "Starting location updates")

            val locationRequest = LocationRequest.Builder(10000) // 10 seconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000) // 5 seconds
                .build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Location permission not granted")
                return
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            Log.d(TAG, "Location updates requested")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
        }
    }

    private fun broadcastLocation(location: Location) {
        try {
            val intent = Intent(ACTION_LOCATION_UPDATE)
            intent.putExtra(EXTRA_LOCATION, location)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            Log.d(TAG, "Location broadcast sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting location", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d(TAG, "onStartCommand")

            // Start as foreground service with notification
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "Started as foreground service")

            // Start location updates
            startLocationUpdates()

            return START_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun createNotification(): Notification {
        try {
            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CopenhagenBuzz Location Service")
                .setContentText("Tracking your location for nearby events")
                .setSmallIcon(R.drawable.baseline_add_location_24)
                .setContentIntent(pendingIntent)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
            throw e
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            Log.d(TAG, "onDestroy")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
        super.onDestroy()
    }
}