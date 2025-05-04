package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.utils.EventDateUtil
import java.util.*

/**
 * DialogFragment that shows all events for a selected calendar day.
 *
 * Displays the date in the title and a scrollable list of events
 * (with name, type, photo, and favorite toggle) for that day.
 */
class DayEventsDialogFragment : DialogFragment() {

    private lateinit var date: Calendar

    private val dataViewModel: DataViewModel by activityViewModels()

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
         * @return A new instance of [DayEventsDialogFragment]
         */
        fun newInstance(
            day: Int,
            month: Int,
            year: Int,
            eventIds: List<String>
        ): DayEventsDialogFragment {
            val fragment = DayEventsDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_DAY, day)
                putInt(ARG_MONTH, month)
                putInt(ARG_YEAR, year)
                putStringArrayList(ARG_EVENT_IDS, ArrayList(eventIds))
            }
            return fragment
        }
    }

    /**
     * Builds and returns the dialog UI.
     *
     * - Reads the date and event IDs from arguments
     * - Formats and sets the dialog title to "Events for {MMMM d, yyyy}"
     * - Configures a RecyclerView with [EventListAdapter]
     */
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

        // Setup RecyclerView to list events
        val recyclerView = view.findViewById<RecyclerView>(R.id.events_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = EventListAdapter(eventIds, dataViewModel, parentFragmentManager)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * Adapter for rendering a vertical list of events in the dialog.
     *
     * @param eventIds        IDs of the events to display
     * @param dataViewModel   ViewModel providing event and favorite data
     * @param fragmentManager FragmentManager for launching detail dialogs
     */
    inner class EventListAdapter(
        private val eventIds: List<String>,
        private val dataViewModel: DataViewModel,
        private val fragmentManager: FragmentManager
    ) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

        /**
         * ViewHolder representing one event row: name, type, photo, favorite toggle.
         */
        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val eventName: TextView = itemView.findViewById(R.id.event_name)
            val eventType: TextView = itemView.findViewById(R.id.event_type)
            val eventPhoto: ImageView = itemView.findViewById(R.id.event_photo)
            val favoriteIcon: ImageView = itemView.findViewById(R.id.remove_favorite_btn)
        }

        /**
         * Inflates the layout for a single event row.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.favorite_row_item, parent, false)
            return EventViewHolder(view)
        }

        /**
         * Binds an event's data into the ViewHolder:
         * - Sets name, type, and photo
         * - Displays filled or outlined heart based on favorite status
         * - Handles favorite toggle clicks
         * - Launches [EventDetailsDialogFragment] on row click
         */
        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val eventId = eventIds[position]
            val event = dataViewModel.events.value?.find { it.id == eventId } ?: return

            // Populate textual fields
            holder.eventName.text = event.eventName
            holder.eventType.text = event.eventType

            // Determine favorite state
            val isFavorite = dataViewModel.favorites.value?.any { it.id == event.id } == true
            holder.favoriteIcon.setImageResource(
                if (isFavorite) R.drawable.baseline_favorite_24
                else R.drawable.baseline_favorite_border_24
            )

            // Load or placeholder image
            if (event.eventPhotoUrl.isNotEmpty()) {
                Picasso.get()
                    .load(event.eventPhotoUrl)
                    .placeholder(R.drawable.baseline_refresh_24)
                    .error(R.drawable.baseline_image_not_supported_24)
                    .into(holder.eventPhoto)
            } else {
                holder.eventPhoto.setImageResource(R.drawable.baseline_image_not_supported_24)
            }

            // Toggle favorite on icon click and refresh this item
            holder.favoriteIcon.setOnClickListener {
                if (isFavorite) dataViewModel.removeFromFavorites(event.id)
                else dataViewModel.addToFavorites(event)
                notifyItemChanged(position)
            }

            // Open details on row click
            holder.itemView.setOnClickListener {
                EventDetailsDialogFragment
                    .newInstance(event.id)
                    .show(fragmentManager, "EventDetailsDialog")
            }
        }

        /**
         * @return Number of events (rows) in this list.
         */
        override fun getItemCount(): Int = eventIds.size
    }
}