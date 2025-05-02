package dk.itu.moapd.copenhagenbuzz.frnw.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event

class CalendarAdapter(
    private var days: List<Int>,
    private var events: Map<Int, List<Event>>,
    private val listener: OnDayClickListener
) : RecyclerView.Adapter<CalendarAdapter.CalenderViewHolder>() {

    interface OnDayClickListener {
        fun onDayClicked(day: Int, events: List<Event>?)
    }

    inner class CalenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.day_text)
        val eventIndicator: View = itemView.findViewById(R.id.event_indicator)
        val dayCell: CardView = itemView.findViewById(R.id.day_cell)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.calendar_day_item, parent, false)
        return CalenderViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalenderViewHolder, position: Int) {
        val day = days[position]

        // Configure cell appearance based on if it's a valid day
        if (day > 0) {
            // Valid day in current month
            holder.dayText.text = day.toString()
            holder.dayText.alpha = 1.0f

            // Check if there are any events for this day
            val eventsForDay = events[day]
            if (!eventsForDay.isNullOrEmpty()) {
                // Show event indicator
                holder.eventIndicator.visibility = View.VISIBLE
            } else {
                // Hide event indicator
                holder.eventIndicator.visibility = View.GONE
            }

            holder.dayCell.setOnClickListener {
                listener.onDayClicked(day, eventsForDay)

            }
        } else {
            // Empty cell or day from another month
            holder.dayText.text = ""
            holder.eventIndicator.visibility = View.INVISIBLE
            holder.dayCell.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<Int>, newEvents: Map<Int, List<Event>>) {
        days = newDays
        events = newEvents
        notifyDataSetChanged()
    }
}