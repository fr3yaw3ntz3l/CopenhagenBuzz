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
            val generatedEvents = generateMockEvents()
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
                    eventPhotoUrl = "android.resource://dk.itu.moapd.copenhagenbuzz.frnw/drawable/brat_tour",
                    eventDescription = faker.lorem().sentence()
                )
            )
        }

        return eventsList
    }
}


