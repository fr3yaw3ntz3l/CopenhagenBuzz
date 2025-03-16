package dk.itu.moapd.copenhagenbuzz.frnw.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event

class FavoriteAdapter(
    private val favoriteEvents: List<Event> // List of favorite events
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    // Inner class to hold the views for each list item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.event_name)
        val eventType: TextView = itemView.findViewById(R.id.event_type)
        val eventPhoto: ImageView = itemView.findViewById(R.id.event_photo)
    }

    // Create new views for each list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_row_item, parent, false) // Inflate favorite_row_item.xml
        return ViewHolder(view)
    }

    // Bind data to the views in the list item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = favoriteEvents[position]

        // Populate UI elements with event data
        holder.eventName.text = event.eventName
        holder.eventType.text = event.eventType

        // Load event image using Picasso
        Picasso.get()
            .load(event.eventPhotoUrl)
            .placeholder(R.drawable.baseline_refresh_24)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(holder.eventPhoto)

        // Force correct height
        val params = holder.itemView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = params
    }

    // Return the number of favorite events
    override fun getItemCount(): Int = favoriteEvents.size
}
