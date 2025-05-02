package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.services.LocationService

class MapsFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "MapsFragment"

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private var hasShownInitialLocation = false
    private var eventMarkers = mutableMapOf<String, Marker>()

    // Location receiver
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                try {
                    val location = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(LocationService.EXTRA_LOCATION, Location::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
                    }

                    location?.let {
                        Log.d(TAG, "Received location update: ${it.latitude}, ${it.longitude}")
                        currentLocation = it
                        updateCurrentLocationOnMap()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing location update", e)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Get map fragment and async load
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)

            // Register location receiver
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                locationReceiver,
                IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Snackbar.make(
                binding.root,
                "Error initializing map: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            Log.d(TAG, "Map is ready")
            googleMap = map

            // Enable location if permission is granted
            enableMyLocation()

            // Set a default location (Copenhagen)
            if (currentLocation == null && !hasShownInitialLocation) {
                val copenhagen = LatLng(55.6761, 12.5683)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(copenhagen, 12f))
                hasShownInitialLocation = true
            }

            // Observe events data
            dataViewModel.events.observe(viewLifecycleOwner) { events ->
                try {
                    displayEventsOnMap(events)
                } catch (e: Exception) {
                    Log.e(TAG, "Error displaying events on map", e)
                }
            }

            // Set info window click listener
            googleMap?.setOnInfoWindowClickListener { marker ->
                try {
                    val eventId = marker.tag as? String
                    if (eventId != null) {
                        showEventDetails(eventId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling marker click", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMapReady", e)
            Snackbar.make(
                binding.root,
                "Error setting up map: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun enableMyLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                Log.d(TAG, "Location permission not granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling location", e)
        }
    }

    private fun updateCurrentLocationOnMap() {
        try {
            currentLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)

                // Only zoom to user location when first received or if specifically requested
                if (!hasShownInitialLocation) {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    hasShownInitialLocation = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating current location on map", e)
        }
    }

    private fun displayEventsOnMap(events: List<Event>) {
        try {
            Log.d(TAG, "Displaying ${events.size} events on map")

            // Track markers to add or update
            val currentEventIds = mutableSetOf<String>()

            // Add or update markers for events
            for (event in events) {
                val eventId = event.id
                currentEventIds.add(eventId)

                val location = event.eventLocation
                // Skip events with invalid location coordinates
                if (location.latitude == 0.0 && location.longitude == 0.0) {
                    Log.d(TAG, "Skipping event with invalid coordinates: ${event.eventName}")
                    continue
                }

                val position = LatLng(location.latitude, location.longitude)

                // Check if marker already exists
                if (eventMarkers.containsKey(eventId)) {
                    // Update existing marker
                    val marker = eventMarkers[eventId]
                    marker?.position = position
                    marker?.title = event.eventName
                    marker?.snippet = event.eventDate
                } else {
                    // Create new marker
                    val marker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(event.eventName)
                            .snippet(event.eventDate)
                    )

                    marker?.tag = eventId
                    if (marker != null) {
                        eventMarkers[eventId] = marker
                    }
                }
            }

            // Remove markers for events that no longer exist
            val markersToRemove = eventMarkers.keys.filter { it !in currentEventIds }
            for (markerId in markersToRemove) {
                eventMarkers[markerId]?.remove()
                eventMarkers.remove(markerId)
            }

            // If no current location and at least one event, center on first event with valid coordinates
            if (currentLocation == null && !hasShownInitialLocation && events.isNotEmpty()) {
                val validEvent = events.find {
                    it.eventLocation.latitude != 0.0 || it.eventLocation.longitude != 0.0
                }

                validEvent?.let {
                    val position = LatLng(it.eventLocation.latitude, it.eventLocation.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
                    hasShownInitialLocation = true
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error displaying events on map", e)
        }
    }

    private fun showEventDetails(eventId: String) {
        try {
            // Find the event by ID
            val event = dataViewModel.events.value?.find { it.id == eventId }
            event?.let {
                // Show event details in a dialog
                val dialog = EventDetailsDialogFragment.newInstance(eventId)
                dialog.show(parentFragmentManager, "EventDetailsDialog")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing event details", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            // Request latest location when fragment becomes visible
            val serviceIntent = Intent(requireContext(), LocationService::class.java)
            requireActivity().startService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        _binding = null
    }
}