package dk.itu.moapd.copenhagenbuzz.frnw.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event

class FavoriteAdapter(
    options: FirebaseRecyclerOptions<Event>,
    private val dataViewModel: DataViewModel
) : FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    // Inner class to hold the views for each list item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.event_name)
        val eventType: TextView = itemView.findViewById(R.id.event_type)
        val eventPhoto: ImageView = itemView.findViewById(R.id.event_photo)
        val removeFavoriteBtn: ImageView = itemView.findViewById(R.id.remove_favorite_btn)
    }

    // Create new views for each list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_row_item, parent, false)
        return ViewHolder(view)
    }

    // Bind data to the views in the list item
    override fun onBindViewHolder(holder: ViewHolder, position: Int, event: Event) {
        // Populate UI elements with event data
        holder.eventName.text = event.eventName
        holder.eventType.text = event.eventType

        // Load event image using Picasso
        Picasso.get()
            .load(event.eventPhotoUrl)
            .placeholder(R.drawable.baseline_refresh_24)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(holder.eventPhoto)

        // Set up remove favorite button click listener
        holder.removeFavoriteBtn.setOnClickListener {
            // Get current favorites
            val updatedFavorites = dataViewModel.favorites.value?.toMutableList() ?: mutableListOf()

            // Remove this event from favorites
            updatedFavorites.removeAll { it.id == event.id }

            // Update favorites in the DataViewModel
            dataViewModel.updateFavorites(updatedFavorites)
        }
    }
}