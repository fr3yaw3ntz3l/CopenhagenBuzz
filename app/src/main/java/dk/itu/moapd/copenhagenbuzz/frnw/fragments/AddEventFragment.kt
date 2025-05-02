package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentAddEventBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.models.EventLocation
import java.util.Locale

class AddEventFragment : Fragment() {
    private var _binding: FragmentAddEventBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val eventsRef = database.reference.child("events")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAddEventBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            fabAddEvent.setOnClickListener {
                // Validate all required fields are filled
                if (validateForm()) {
                    // Generate a unique key for the new event
                    val newEventKey = eventsRef.push().key

                    if (newEventKey != null) {
                        // Create event object with the generated key
                        val addressString = editTextEventLocation.text.toString()
                        val event = Event(
                            id = newEventKey,
                            userId = auth.currentUser?.uid ?: "",
                            eventName = editTextEventName.text.toString(),
                            eventLocation = EventLocation(
                                latitude = 0.0, // This will be updated with geocoding
                                longitude = 0.0, // This will be updated with geocoding
                                address = addressString
                            ),
                            eventDate = editTextEventDate.text.toString(),
                            eventType = editTextEventType.text.toString(),
                            eventDescription = editTextEventDescription.text.toString(),
                            eventPhotoUrl = "android.resource://dk.itu.moapd.copenhagenbuzz.frnw/drawable/brat_tour",
                            isFavorite = false
                        )

                        // Use DataViewModel to save event
                        dataViewModel.addEvent(event)

                        showMessage("Event added successfully!")
                        clearForm()
                    } else {
                        showMessage("Error generating event ID. Please try again.")
                    }
                } else {
                    showMessage("Please fill in all fields")
                }
            }
        }
    }

    /**
     * Validates that all required fields are filled.
     *
     * @return true if all fields are valid, false otherwise
     */
    private fun validateForm(): Boolean {
        return with(binding) {
            editTextEventName.text.toString().isNotEmpty() &&
                    editTextEventLocation.text.toString().isNotEmpty() &&
                    editTextEventDate.text.toString().isNotEmpty() &&
                    editTextEventType.text.toString().isNotEmpty() &&
                    editTextEventDescription.text.toString().isNotEmpty()
        }
    }

    /**
     * Displays a Snackbar message.
     */
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Clears the text fields in the UI.
     */
    private fun clearForm() {
        with(binding) {
            editTextEventName.text?.clear()
            editTextEventLocation.text?.clear()
            editTextEventDate.text?.clear()
            editTextEventType.text?.clear()
            editTextEventDescription.text?.clear()
        }
    }

    private fun geocodeAddress(address: String, onComplete: (EventLocation?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+
                geocoder.getFromLocationName(address, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val location = addresses[0]
                        onComplete(EventLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = address
                        ))
                    } else {
                        onComplete(null)
                    }
                }
            } else {
                // For Android 12 and below
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(address, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val location = addresses[0]
                    onComplete(EventLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = address
                    ))
                } else {
                    onComplete(null)
                }
            }
        } catch (e: Exception) {
            Log.e("AddEventFragment", "Geocoding error", e)
            onComplete(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}