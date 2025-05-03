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
import dk.itu.moapd.copenhagenbuzz.frnw.activities.MainActivity
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.utils.EventDateUtil
import java.util.*

/**
 * A dialog fragment that displays all events for a specific day.
 */
class DayEventsDialogFragment : DialogFragment() {

    private lateinit var events: List<Event>
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

        // Create and set the adapter (similar to EventAdapter)
        val adapter = EventListAdapter(eventIds, dataViewModel, parentFragmentManager)
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
        private val dataViewModel: DataViewModel,
        private val fragmentManager: FragmentManager
    ) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val eventName: TextView = itemView.findViewById(R.id.event_name)
            val eventType: TextView = itemView.findViewById(R.id.event_type)
            val eventPhoto: ImageView = itemView.findViewById(R.id.event_photo)
            val favoriteIcon: ImageView = itemView.findViewById(R.id.remove_favorite_btn)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.favorite_row_item, parent, false)
            return EventViewHolder(view)
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val eventId = eventIds[position]
            val event = dataViewModel.events.value?.find { it.id == eventId }

            event?.let { currentEvent ->
                // Set event information
                holder.eventName.text = currentEvent.eventName
                holder.eventType.text = currentEvent.eventType

                // Check if this event is in the favorites collection
                val favorites = dataViewModel.favorites.value ?: emptyList()
                val isFavorite = favorites.any { it.id == currentEvent.id }

                // Set the appropriate favorite icon based on status
                holder.favoriteIcon.setImageResource(
                    if (isFavorite) R.drawable.baseline_favorite_24
                    else R.drawable.baseline_favorite_border_24
                )

                // Load event image
                if (currentEvent.eventPhotoUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(currentEvent.eventPhotoUrl)
                        .placeholder(R.drawable.baseline_refresh_24)
                        .error(R.drawable.baseline_image_not_supported_24)
                        .into(holder.eventPhoto)
                } else {
                    holder.eventPhoto.setImageResource(R.drawable.baseline_image_not_supported_24)
                }

                // Handle click on favorite icon
                holder.favoriteIcon.setOnClickListener {
                    if (isFavorite) {
                        // Remove from favorites
                        dataViewModel.removeFromFavorites(currentEvent.id)
                    } else {
                        // Add to favorites
                        dataViewModel.addToFavorites(currentEvent)
                    }
                    notifyItemChanged(position) // Notify the adapter of the change
                }

                // Set item click listener
                holder.itemView.setOnClickListener {
                    val dialog = EventDetailsDialogFragment.newInstance(currentEvent.id)
                    dialog.show(fragmentManager, "EventDetailsDialog")
                }
            }
        }

        override fun getItemCount(): Int = eventIds.size
    }
}