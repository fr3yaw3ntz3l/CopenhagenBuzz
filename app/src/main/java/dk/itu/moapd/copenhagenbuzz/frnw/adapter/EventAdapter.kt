package dk.itu.moapd.copenhagenbuzz.frnw.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.fragments.EditEventDialogFragment
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event

/**
 * Adapter for displaying events from Firebase in a ListView.
 * Supports favoriting and editing events.
 */
class EventAdapter(
    options: FirebaseListOptions<Event>,
    private val context: Context,
    private val dataViewModel: DataViewModel,
    private val fragmentManager: FragmentManager
) : FirebaseListAdapter<Event>(options) {

    private val auth = FirebaseAuth.getInstance()

    override fun populateView(view: View, event: Event, position: Int) {
        // Get references to the views in the list item layout
        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventType: TextView = view.findViewById(R.id.event_type)
        val eventLocation: TextView = view.findViewById(R.id.event_location)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        val eventPhoto: ImageView = view.findViewById(R.id.event_photo)
        val favoriteIcon: ImageView = view.findViewById(R.id.favorite_icon)

        // Get reference to buttons
        val buttonEdit: MaterialButton = view.findViewById(R.id.button_edit)
        val buttonInfo: MaterialButton = view.findViewById(R.id.button_info)

        // Bind the event data to the views
        eventName.text = event.eventName
        eventType.text = event.eventType
        eventLocation.text = event.eventLocation.address
        eventDate.text = event.eventDate
        eventDescription.text = event.eventDescription

        // Use Picasso to load the event photo
        Picasso.get()
            .load(event.eventPhotoUrl)
            .placeholder(R.drawable.baseline_refresh_24)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(eventPhoto)

        // Check if this event is in the favorites collection
        val favorites = dataViewModel.favorites.value ?: emptyList()
        val isFavorite = favorites.any { it.id == event.id }

        // Set the appropriate favorite icon based on status
        favoriteIcon.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )

        // Handle click on favorite icon
        favoriteIcon.setOnClickListener {
            if (isFavorite) {
                // Remove from favorites
                dataViewModel.removeFromFavorites(event.id)
            } else {
                // Add to favorites
                dataViewModel.addToFavorites(event)
            }
        }

        // Check if user is the owner of the event
        val isOwner = event.userId == auth.currentUser?.uid

        // Show/hide edit button based on ownership
        buttonEdit.visibility = if (isOwner) View.VISIBLE else View.GONE

        // Set click listener for edit button
        buttonEdit.setOnClickListener {
            // Show edit event dialog
            val dialog = EditEventDialogFragment.newInstance(event.id)
            dialog.show(fragmentManager, "EditEventDialog")
        }

        // The info button doesn't need to do anything special since all info is already visible
        // You could remove this button or use it for some other functionality
        buttonInfo.setOnClickListener {
            // Maybe expand the card to show more details, or toggle visibility of some elements
            // For now, this is a no-op
        }
    }
}