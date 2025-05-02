package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.adapter.CalendarAdapter
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentCalendarBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.utils.EventDateUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment(), CalendarAdapter.OnDayClickListener {
    private val TAG = "CalendarFragment"

    private var _binding: FragmentCalendarBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private var currentMonthEvents = mutableMapOf<Int, List<Event>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCalendarBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarHeader()
        setupCalenderGrid()
        setupNavigationButtons()

        // Observe events data
        dataViewModel.events.observe(viewLifecycleOwner) { events: List<Event> ->
            updateEventsForCurrentMonth(events)
        }
    }

    private fun setupCalendarHeader() {
        updateMonthYearText()
    }

    private fun setupCalenderGrid() {
        val daysPerWeek = 7
        binding.calendarGrid.layoutManager = GridLayoutManager(requireContext(), daysPerWeek)

        // Create adapter with current month data
        calendarAdapter = CalendarAdapter(getDaysInMonth(), currentMonthEvents, this)
        binding.calendarGrid.adapter = calendarAdapter
    }

    private fun setupNavigationButtons() {
        binding.prevMonthButton.setOnClickListener { _: View ->
            calendar.add(Calendar.MONTH, -1)
            updateCalenderView()
        }

        binding.nextMonthButton.setOnClickListener { _: View ->
            calendar.add(Calendar.MONTH, 1)
            updateCalenderView()
        }
    }

    private fun updateCalenderView() {
        updateMonthYearText()
        updateEventsForCurrentMonth(dataViewModel.events.value ?: emptyList())
        calendarAdapter.updateDays(getDaysInMonth(), currentMonthEvents)
    }

    private fun updateMonthYearText() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYearText = dateFormat.format(calendar.time)
        binding.calendarHeader.findViewById<TextView>(R.id.text_month_year).text = monthYearText
    }

    private fun getDaysInMonth(): List<Int> {
        val daysInMonth = mutableListOf<Int>()

        // Set calender to the first day of the month
        val tempCalender = calendar.clone() as Calendar
        tempCalender.set(Calendar.DAY_OF_MONTH, 1)

        // Get the day of the week for the first day (Sunday is 0)
        val firstDayOfWeek = tempCalender.get(Calendar.DAY_OF_WEEK) - 1

        // Add empty spaces for days before the first day of the month
        for (i in 0 until firstDayOfWeek) {
            daysInMonth.add(0) // 0 represents an empty space
        }

        // Add the actual days of the month
        val daysCount = tempCalender.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysCount) {
            daysInMonth.add(i)
        }

        return daysInMonth
    }

    private fun updateEventsForCurrentMonth(events: List<Event>) {
        try {
            currentMonthEvents.clear()

            // Get current month and year from the calendar
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            // Filter events for the current month being displayed
            val eventsInMonth = events.filter { event: Event ->
                val eventDate = event.getStartDateCalendar()
                eventDate?.let { date: Calendar ->
                    EventDateUtil.isInMonth(date, currentMonth, currentYear)
                } ?: false
            }

            // Group events by day of month
            eventsInMonth.forEach { event: Event ->
                val eventDate = event.getStartDateCalendar()
                eventDate?.let { date: Calendar ->
                    val dayOfMonth = EventDateUtil.getDayOfMonth(date)
                    val eventsForDay = currentMonthEvents[dayOfMonth] ?: emptyList()
                    currentMonthEvents[dayOfMonth] = eventsForDay + event
                }
            }

            // Update the adapter with the new events
            calendarAdapter.updateDays(getDaysInMonth(), currentMonthEvents)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating events for current month", e)
        }
    }

    private fun parseEventDate(dateString: String): Calendar? {
        return EventDateUtil.parseDate(dateString)
    }

    override fun onDayClicked(day: Int, events: List<Event>?) {
        if (day <= 0 || events.isNullOrEmpty()) return

        // Get current month and year
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        if (events.size == 1) {
            // If there's only one event, show its details directly
            val dialog = EventDetailsDialogFragment.newInstance(events.first().id)
            dialog.show(parentFragmentManager, "EventDetailsDialog")
        } else {
            // If there are multiple events, show the events list dialog
            val eventIds = events.map { event: Event -> event.id }
            val dialog = DayEventsDialogFragment.newInstance(day, month, year, eventIds)
            dialog.show(parentFragmentManager, "DayEventsDialog")
        }
    }

    /**
     * Gets an event by its ID.
     * Used by the DayEventsDialogFragment to retrieve event details.
     *
     * @param eventId The ID of the event to retrieve
     * @return The Event object, or null if not found
     */
    fun getEventById(eventId: String): Event? {
        return dataViewModel.events.value?.find { event: Event -> event.id == eventId }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}