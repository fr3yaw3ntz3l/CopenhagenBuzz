package dk.itu.moapd.copenhagenbuzz.frnw.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.javafaker.Faker

class DataViewModel : ViewModel() {
    private var isEventsGenerated = false

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    val _favorites = MutableLiveData<List<Event>>()
    val favorites: LiveData<List<Event>> = _favorites

    // Method to fetch the event list asynchronously using coroutines
    fun fetchEvents() {
        if (!isEventsGenerated) {
            CoroutineScope(Dispatchers.IO).launch {
                val generatedEvents = generateMockEvents()
                _events.postValue(generatedEvents) // Post events to LiveData

                val favoriteSample = generatedEvents.shuffled().take(3) // Generate favorites
                _favorites.postValue(favoriteSample)

                isEventsGenerated = true // Flag to prevent regeneration
            }
        }
    }


    // Generates 3 random favorite-events
    private fun generateFavoriteEvents(eventList: List<Event>) {
        val favoriteSample = eventList.shuffled().take(3)
        _favorites.postValue(favoriteSample)
    }

    fun updateFavorites(updatedFavorites: List<Event>) {
        _favorites.value = updatedFavorites
    }

    private fun generateMockEvents(): List<Event> {
        val faker = Faker()
        val eventsList = mutableListOf<Event>()

        // Creating 10 mock events
        for (i in 1..10) {
            eventsList.add(
                Event(
                    eventName = faker.book().title(),
                    eventLocation = faker.address().city(),
                    eventDate = faker.date().future(30, java.util.concurrent.TimeUnit.DAYS).toString(),
                    eventType = faker.music().genre(),
                    eventPhotoUrl = "android.resource://dk.itu.moapd.copenhagenbuzz.frnw/drawable/brat_tour",
                    eventDescription = faker.lorem().sentence()
                )
            )
        }

        return eventsList
    }
}


