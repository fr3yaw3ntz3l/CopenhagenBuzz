package dk.itu.moapd.copenhagenbuzz.frnw.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.javafaker.Faker

class DataViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    // Method to fetch the event list asynchronously using coroutines
    fun fetchEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            // Simulating the asynchronous fetching of event data
            val generatedEvents = generateMockEvents()
            // Post the generated events to the LiveData object
            _events.postValue(generatedEvents)
        }
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
                    eventPhotoUrl = "https://via.placeholder.com/150",
                    eventDescription = faker.lorem().sentence()
                )
            )
        }

        return eventsList
    }
}



