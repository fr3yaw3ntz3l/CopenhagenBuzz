package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.utils.EventDateUtil
import java.util.*

/**
 * A dialog fragment that displays all events for a specific day.
 */
class DayEventsDialogFragment : DialogFragment() {

    private lateinit var events: List<Event>
    private lateinit var date: Calendar

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_MONTH = "month"
        private const val ARG_YEAR = "year"
        private const val ARG_EVENT_IDS = "event_ids"

        /**
         * Creates a new instance of the dialog with specified events and date.
         *
         * @param day Day of month
         * @param month Month (0-11)
         * @param year Year
         * @param eventIds List of event IDs for this day
         * @return A new instance of DayEventsDialogFragment
         */
        fun newInstance(day: Int, month: Int, year: Int, eventIds: List<String>): DayEventsDialogFragment {
            val fragment = DayEventsDialogFragment()
            val args = Bundle()
            args.putInt(ARG_DAY, day)
            args.putInt(ARG_MONTH, month)
            args.putInt(ARG_YEAR, year)
            args.putStringArrayList(ARG_EVENT_IDS, ArrayList(eventIds))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_day_events_dialog, null)

        // Get arguments
        val day = arguments?.getInt(ARG_DAY) ?: 1
        val month = arguments?.getInt(ARG_MONTH) ?: 0
        val year = arguments?.getInt(ARG_YEAR) ?: 2025
        val eventIds = arguments?.getStringArrayList(ARG_EVENT_IDS) ?: arrayListOf()

        // Create date
        date = Calendar.getInstance()
        date.set(year, month, day)

        // Set the dialog title with formatted date
        val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
        dialogTitle.text = getString(R.string.events_for_day, EventDateUtil.formatDate(date))

        // Set up the RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.events_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create and set the adapter
        val adapter = EventListAdapter(eventIds) { eventId ->
            // Show EventDetailsDialog when an event is clicked
            val detailsDialog = EventDetailsDialogFragment.newInstance(eventId)
            detailsDialog.show(parentFragmentManager, "EventDetailsDialog")
        }
        recyclerView.adapter = adapter

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * Adapter for displaying a list of events for a specific day.
     */
    inner class EventListAdapter(
        private val eventIds: List<String>,
        private val onEventClick: (String) -> Unit
    ) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val eventName: TextView = itemView.findViewById(R.id.text_event_name)
            val eventType: TextView = itemView.findViewById(R.id.text_event_type)
            val eventTime: TextView = itemView.findViewById(R.id.text_event_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.day_event_list_item, parent, false)
            return EventViewHolder(view)
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val eventId = eventIds[position]
            val parentFragment = parentFragment as? CalendarFragment
            val event = parentFragment?.getEventById(eventId)

            event?.let {
                holder.eventName.text = it.eventName
                holder.eventType.text = it.eventType
                holder.eventTime.text = it.eventDate

                // Set click listener
                holder.itemView.setOnClickListener {
                    onEventClick(eventId)
                }
            }
        }

        override fun getItemCount(): Int = eventIds.size
    }
}