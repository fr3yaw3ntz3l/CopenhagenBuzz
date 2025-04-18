package dk.itu.moapd.copenhagenbuzz.frnw.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DataViewModel : ViewModel() {
    private val TAG = "DataViewModel"

    // Firebase references
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val eventsRef = database.reference.child("events")
    private val favoritesRef = database.reference.child("favorites")

    // Database manager for events
    private val databaseManager = DatabaseManager()

    // LiveData for events
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    // LiveData for favorite events
    private val _favorites = MutableLiveData<List<Event>>()
    val favorites: LiveData<List<Event>> = _favorites

    // LiveData for operation status
    private val _operationStatus = MutableLiveData<OperationStatus>()
    val operationStatus: LiveData<OperationStatus> = _operationStatus

    // Current user ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Initialize data
    init {
        fetchEvents()
        setupFavoritesListener()
    }

    /**
     * Fetches all events from Firebase Realtime Database.
     */
    fun fetchEvents() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching events from Firebase...")
                val eventList = databaseManager.getAllEvents()
                _events.postValue(eventList)

                // Also update favorites after fetching events
                fetchFavorites()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to load events: ${e.message}"))
            }
        }
    }

    /**
     * Adds a new event to the Firebase Realtime Database.
     * Uses Firebase push() method to generate a unique key.
     *
     * @param event The event to add
     */
    fun addEvent(event: Event) {
        viewModelScope.launch {
            try {
                // Make sure the event has the current user ID
                event.userId = currentUserId

                // Generate a key if not provided
                if (event.id.isEmpty()) {
                    val newKey = eventsRef.push().key ?: ""
                    if (newKey.isEmpty()) {
                        _operationStatus.postValue(OperationStatus.Error("Failed to generate event ID"))
                        return@launch
                    }
                    event.id = newKey
                }

                // Add event to the database
                val eventId = databaseManager.saveEvent(event)

                // Refresh the event list
                fetchEvents()

                _operationStatus.postValue(OperationStatus.Success("Event added successfully"))
            } catch (e: Exception) {
                Log.e(TAG, "Error adding event", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to add event: ${e.message}"))
            }
        }
    }

    /**
     * Updates an existing event in the Firebase Realtime Database.
     * Only allows updates by the event creator.
     *
     * @param event The event to update
     */
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            try {
                // Check if user owns the event
                if (event.userId != currentUserId) {
                    _operationStatus.postValue(OperationStatus.Error("You can only edit your own events"))
                    return@launch
                }

                // Update the event
                val success = databaseManager.updateEvent(event)

                if (success) {
                    // Refresh events and favorites
                    fetchEvents()
                    _operationStatus.postValue(OperationStatus.Success("Event updated successfully"))
                } else {
                    _operationStatus.postValue(OperationStatus.Error("Failed to update event"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to update event: ${e.message}"))
            }
        }
    }

    /**
     * Deletes an event from the Firebase Realtime Database.
     * Only allows deletion by the event creator.
     * Also removes it from all users' favorites.
     *
     * @param eventId ID of the event to delete
     */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                // Check if user owns the event
                val event = _events.value?.find { it.id == eventId }

                if (event == null) {
                    _operationStatus.postValue(OperationStatus.Error("Event not found"))
                    return@launch
                }

                if (event.userId != currentUserId) {
                    _operationStatus.postValue(OperationStatus.Error("You can only delete your own events"))
                    return@launch
                }

                // Delete the event
                val success = databaseManager.deleteEvent(eventId)

                if (success) {
                    // Remove from all users' favorites
                    removeFavoriteForAllUsers(eventId)

                    // Update local favorites list
                    val updatedFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
                    updatedFavorites.removeAll { it.id == eventId }
                    _favorites.postValue(updatedFavorites)

                    // Refresh events
                    fetchEvents()

                    _operationStatus.postValue(OperationStatus.Success("Event deleted successfully"))
                } else {
                    _operationStatus.postValue(OperationStatus.Error("Failed to delete event"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to delete event: ${e.message}"))
            }
        }
    }

    /**
     * Fetches the current user's favorite events.
     */
    private fun fetchFavorites() {
        if (currentUserId.isEmpty()) {
            _favorites.postValue(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                val userFavoritesRef = favoritesRef.child(currentUserId)
                val snapshot = userFavoritesRef.get().await()

                val favoriteIds = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { favoriteIds.add(it) }
                }

                // Match favorite IDs with actual events
                val allEvents = _events.value ?: emptyList()
                val favoriteEvents = allEvents.filter { event ->
                    favoriteIds.contains(event.id)
                }

                _favorites.postValue(favoriteEvents)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching favorites", e)
            }
        }
    }

    /**
     * Sets up a listener for changes to the user's favorites.
     */
    private fun setupFavoritesListener() {
        if (currentUserId.isEmpty()) return

        val userFavoritesRef = favoritesRef.child(currentUserId)

        userFavoritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fetchFavorites()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error listening for favorites", error.toException())
            }
        })
    }

    /**
     * Adds an event to the user's favorites.
     *
     * @param event The event to add to favorites
     */
    fun addToFavorites(event: Event) {
        if (currentUserId.isEmpty()) {
            _operationStatus.postValue(OperationStatus.Error("You must be logged in to add favorites"))
            return
        }

        viewModelScope.launch {
            try {
                // Add to Firebase
                favoritesRef.child(currentUserId).child(event.id).setValue(true).await()

                // Update local favorites list
                val updatedFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
                if (!updatedFavorites.any { it.id == event.id }) {
                    updatedFavorites.add(event)
                }
                _favorites.postValue(updatedFavorites)

                _operationStatus.postValue(OperationStatus.Success("Added to favorites"))
            } catch (e: Exception) {
                Log.e(TAG, "Error adding to favorites", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to add to favorites: ${e.message}"))
            }
        }
    }

    /**
     * Removes an event from the user's favorites.
     *
     * @param eventId ID of the event to remove from favorites
     */
    fun removeFromFavorites(eventId: String) {
        if (currentUserId.isEmpty()) {
            _operationStatus.postValue(OperationStatus.Error("You must be logged in to remove favorites"))
            return
        }

        viewModelScope.launch {
            try {
                // Remove from Firebase
                favoritesRef.child(currentUserId).child(eventId).removeValue().await()

                // Update local favorites list
                val updatedFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
                updatedFavorites.removeAll { it.id == eventId }
                _favorites.postValue(updatedFavorites)

                _operationStatus.postValue(OperationStatus.Success("Removed from favorites"))
            } catch (e: Exception) {
                Log.e(TAG, "Error removing from favorites", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to remove from favorites: ${e.message}"))
            }
        }
    }

    /**
     * Updates the favorites list.
     *
     * @param updatedFavorites The new list of favorite events
     */
    fun updateFavorites(updatedFavorites: List<Event>) {
        viewModelScope.launch {
            try {
                // Get current favorites for comparison
                val currentFavorites = _favorites.value ?: emptyList()

                // Add new favorites
                for (event in updatedFavorites) {
                    if (currentFavorites.none { it.id == event.id }) {
                        addToFavorites(event)
                    }
                }

                // Remove old favorites
                for (event in currentFavorites) {
                    if (updatedFavorites.none { it.id == event.id }) {
                        removeFromFavorites(event.id)
                    }
                }

                _favorites.postValue(updatedFavorites)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorites", e)
                _operationStatus.postValue(OperationStatus.Error("Failed to update favorites: ${e.message}"))
            }
        }
    }

    /**
     * Removes an event from all users' favorites when the event is deleted.
     * This maintains data consistency between events and favorites.
     *
     * @param eventId ID of the event to remove from all favorites
     */
    private suspend fun removeFavoriteForAllUsers(eventId: String) {
        try {
            // Get all users with this event in favorites
            val snapshot = favoritesRef.get().await()

            // For each user, remove this event from favorites
            for (userSnapshot in snapshot.children) {
                val userId = userSnapshot.key ?: continue

                if (userSnapshot.hasChild(eventId)) {
                    favoritesRef.child(userId).child(eventId).removeValue().await()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing event from all favorites", e)
        }
    }
}