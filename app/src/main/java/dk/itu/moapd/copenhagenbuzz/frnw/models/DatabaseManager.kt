package dk.itu.moapd.copenhagenbuzz.frnw.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseManager {
    private val TAG = "DatabaseManager"
    private val database = FirebaseDatabase.getInstance().reference
    private val eventsRef = database.child("events")
    private val auth = FirebaseAuth.getInstance()

    // Get current user ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Save an event to the database
    suspend fun saveEvent(event: Event): String = withContext(Dispatchers.IO) {
        try {
            // Set the current user ID
            event.userId = currentUserId

            // Create a new unique key if not existing
            val eventId = event.id.ifEmpty { eventsRef.push().key ?: "" }
            event.id = eventId

            // Save the event to Firebase
            eventsRef.child(eventId).setValue(event).await()

            return@withContext eventId
        } catch (e: Exception) {
            Log.e(TAG, "Error saving event", e)
            throw e
        }
    }

    // Add initial events (only call this once during setup)
    suspend fun addInitialEvents() = withContext(Dispatchers.IO) {
        try {
            // Event 1: Copenhagen Jazz Festival
            val event1 = Event(
                id = "jazz-festival",
                userId = currentUserId,
                eventName = "Copenhagen Jazz Festival",
                eventLocation = EventLocation(
                    latitude = 55.6761,
                    longitude = 12.5683,
                    address = "Various locations, Copenhagen"
                ),
                eventDate = "July 5-14, 2025",
                eventType = "Music Festival",
                eventDescription = "Copenhagen Jazz Festival is an annual jazz event that takes place across Copenhagen, featuring performances by international jazz artists and local talent.",
                eventPhotoUrl = "android.resource://dk.itu.moapd.copenhagenbuzz.frnw/drawable/brat_tour",
                isFavorite = false
            )

            // Event 2: Copenhagen Light Festival
            val event2 = Event(
                id = "light-festival",
                userId = currentUserId,
                eventName = "Copenhagen Light Festival",
                eventLocation = EventLocation(
                    latitude = 55.6800,
                    longitude = 12.5717,
                    address = "City Center, Copenhagen"
                ),
                eventDate = "February 3-26, 2025",
                eventType = "Art Festival",
                eventDescription = "The Copenhagen Light Festival is an annual celebration of light art that transforms the dark winter months with illuminated installations throughout the city.",
                eventPhotoUrl = "android.resource://dk.itu.moapd.copenhagenbuzz.frnw/drawable/brat_tour",
                isFavorite = false
            )

            // Event 3: IT University Tech Conference
            val event3 = Event(
                id = "itu-tech-conf",
                userId = currentUserId,
                eventName = "IT University Tech Conference",
                eventLocation = EventLocation(
                    latitude = 55.6596,
                    longitude = 12.5910,
                    address = "IT University of Copenhagen, Rued Langgaards Vej 7, 2300 København"
                ),
                eventDate = "May 15-16, 2025",
                eventType = "Academic Conference",
                eventDescription = "The IT University Tech Conference showcases cutting-edge research and development in computer science, focusing on artificial intelligence, human-computer interaction, and digital design.",
                eventPhotoUrl = "android.resource://dk.itu.moapd.copenhagenbuzz.frnw/drawable/brat_tour",
                isFavorite = false
            )

            // Save the events
            saveEvent(event1)
            saveEvent(event2)
            saveEvent(event3)

            Log.d(TAG, "Initial events added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding initial events", e)
        }
    }

    // Get all events
    suspend fun getAllEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting all events from Firebase")
            val snapshot = eventsRef.get().await()
            Log.d(TAG, "Snapshot received, has children: ${snapshot.hasChildren()}")

            val events = mutableListOf<Event>()

            for (childSnapshot in snapshot.children) {
                try {
                    // Simply convert the snapshot to Event class
                    val event = childSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        // Ensure the ID is set
                        event.id = childSnapshot.key ?: ""
                        events.add(event)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing event ${childSnapshot.key}", e)
                    // Continue with next event
                }
            }

            Log.d(TAG, "Returning ${events.size} events")
            return@withContext events
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events", e)
            throw e
        }
    }

    // Get events created by the current user
    suspend fun getCurrentUserEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            val snapshot = eventsRef.orderByChild("userId").equalTo(currentUserId).get().await()
            val events = mutableListOf<Event>()

            for (childSnapshot in snapshot.children) {
                try {
                    val event = childSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        // Ensure the ID is set
                        event.id = childSnapshot.key ?: ""
                        events.add(event)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user event ${childSnapshot.key}", e)
                }
            }

            return@withContext events
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user events", e)
            throw e
        }
    }

    // Update an existing event
    suspend fun updateEvent(event: Event): Boolean = withContext(Dispatchers.IO) {
        try {
            // Only allow updating if the user is the creator
            val eventSnapshot = eventsRef.child(event.id).get().await()
            val userId = eventSnapshot.child("userId").getValue(String::class.java)

            if (userId == currentUserId) {
                eventsRef.child(event.id).setValue(event).await()
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event", e)
            throw e
        }
    }

    // Delete an event
    suspend fun deleteEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Only allow deletion if the user is the creator
            val eventSnapshot = eventsRef.child(eventId).get().await()
            val userId = eventSnapshot.child("userId").getValue(String::class.java)

            if (userId == currentUserId) {
                eventsRef.child(eventId).removeValue().await()
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event", e)
            throw e
        }
    }

    // Delete all events (use with caution!)
    suspend fun deleteAllEvents() = withContext(Dispatchers.IO) {
        try {
            eventsRef.removeValue().await()
            Log.d(TAG, "All events deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all events", e)
            throw e
        }
    }
}