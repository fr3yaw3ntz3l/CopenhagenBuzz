package dk.itu.moapd.copenhagenbuzz.frnw.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel


class EventAdapter(

    private val context: Context,
    private val events: ArrayList<Event>,
    private val dataViewModel: DataViewModel

) : BaseAdapter() {

    // Inflater to inflate the list item layout
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: View) {

        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventType: TextView = view.findViewById(R.id.event_type)
        val eventLocation: TextView = view.findViewById(R.id.event_location)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        val eventPhoto: ImageView = view.findViewById(R.id.event_photo)
        //val circle: TextView = view.findViewById(R.id.circle_text)
        val favoriteIcon: ImageView = view.findViewById(R.id.favorite_icon)
    }

    override fun getCount(): Int = events.size

    override fun getItem(position: Int): Event = events[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            // Inflate the list item layout
            view = inflater.inflate(R.layout.event_row_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder // Store ViewHolder in view's tag
        } else {
            // Reuse existing view and ViewHolder
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Get the event at the current position
        val event = getItem(position)

        // Bind the event data to the views
        viewHolder.eventName.text = event.eventName
        viewHolder.eventType.text = event.eventType
        viewHolder.eventLocation.text = event.eventLocation
        viewHolder.eventDate.text = event.eventDate
        viewHolder.eventDescription.text = event.eventDescription
        // viewHolder.circle.text = event.eventType.first().toString()

        // Use Picasso to load the event photo
        Picasso.get()
            .load(event.eventPhotoUrl) // URL or resource ID of the photo
            .placeholder(R.drawable.baseline_refresh_24) // Placeholder image while loading
            .error(R.drawable.baseline_image_not_supported_24) // Error image if loading fails
            .into(viewHolder.eventPhoto)

        val isFavorite = dataViewModel.favorites.value?.contains(event) == true
        viewHolder.favoriteIcon.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
        )

        viewHolder.favoriteIcon.setOnClickListener {
            val updatedFavorites = dataViewModel.favorites.value?.toMutableList() ?: mutableListOf()
            if (isFavorite) {
                updatedFavorites.remove(event)
            } else {
                updatedFavorites.add(event)
            }
            dataViewModel.updateFavorites(updatedFavorites)
        }

        return view
    }
}
