package dk.itu.moapd.copenhagenbuzz.frnw.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseManager {
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
            throw e
        }
    }

    // Get all events
    suspend fun getAllEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d("DatabaseManager", "Getting all events from Firebase")
            val snapshot = eventsRef.get().await()
            Log.d("DatabaseManager", "Snapshot received, has children: ${snapshot.hasChildren()}")

            val events = mutableListOf<Event>()

            for (childSnapshot in snapshot.children) {
                val event = childSnapshot.getValue(Event::class.java)
                Log.d("DatabaseManager", "Event parsed: ${event?.eventName}")
                event?.let { events.add(it) }
            }

            Log.d("DatabaseManager", "Returning ${events.size} events")
            return@withContext events
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error getting events", e)
            throw e
        }
    }

    // Get events created by the current user
    suspend fun getCurrentUserEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            val snapshot = eventsRef.orderByChild("userId").equalTo(currentUserId).get().await()
            val events = mutableListOf<Event>()

            for (childSnapshot in snapshot.children) {
                val event = childSnapshot.getValue(Event::class.java)
                event?.let { events.add(it) }
            }

            return@withContext events
        } catch (e: Exception) {
            throw e
        }
    }

    // Update an existing event
    suspend fun updateEvent(event: Event): Boolean = withContext(Dispatchers.IO) {
        try {
            // Only allow updating if the user is the creator
            val existingEvent = eventsRef.child(event.id).get().await()
                .getValue(Event::class.java)

            if (existingEvent?.userId == currentUserId) {
                eventsRef.child(event.id).setValue(event).await()
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // Delete an event
    suspend fun deleteEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Only allow deletion if the user is the creator
            val existingEvent = eventsRef.child(eventId).get().await()
                .getValue(Event::class.java)

            if (existingEvent?.userId == currentUserId) {
                eventsRef.child(eventId).removeValue().await()
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            throw e
        }
    }
}