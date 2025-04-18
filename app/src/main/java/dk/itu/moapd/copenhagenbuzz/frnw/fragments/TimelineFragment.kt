package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.copenhagenbuzz.frnw.activities.MyApplication
import dk.itu.moapd.copenhagenbuzz.frnw.adapter.EventAdapter
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.models.OperationStatus

class TimelineFragment : Fragment() {
    private var _binding: FragmentTimelineBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the Firebase adapter
        setupFirebaseAdapter()

        // Observe operation status for feedback messages
        observeOperationStatus()

        // Observe favorites LiveData to update UI when favorites change
        dataViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            eventAdapter.notifyDataSetChanged()
            Log.d("TimelineFragment", "Favorites updated: ${favorites.size}")
        }
    }

    private fun setupFirebaseAdapter() {
        try {
            Log.d("TimelineFragment", "Setting up Firebase adapter")

            // Get database reference
            val database = FirebaseDatabase.getInstance(MyApplication.DATABASE_URL)

            // Create query to get all events ordered by date
            val eventQuery = database.reference.child("events").orderByChild("eventDate")

            // Build FirebaseListOptions
            val options = FirebaseListOptions.Builder<Event>()
                .setQuery(eventQuery, Event::class.java)
                .setLayout(dk.itu.moapd.copenhagenbuzz.frnw.R.layout.event_row_item)
                .setLifecycleOwner(this)
                .build()

            // Create adapter with the options
            eventAdapter = EventAdapter(options, requireContext(), dataViewModel, parentFragmentManager)
            binding.listView.adapter = eventAdapter

            Log.d("TimelineFragment", "Firebase adapter setup completed")
        } catch (e: Exception) {
            Log.e("TimelineFragment", "Error setting up Firebase adapter", e)
        }
    }

    private fun observeOperationStatus() {
        dataViewModel.operationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is OperationStatus.Success -> {
                    Log.d("TimelineFragment", "Operation success: ${status.message}")
                }
                is OperationStatus.Error -> {
                    Log.e("TimelineFragment", "Operation error: ${status.message}")
                }
                else -> {}
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Start listening for database changes
        if (::eventAdapter.isInitialized) {
            eventAdapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop listening for database changes
        if (::eventAdapter.isInitialized) {
            eventAdapter.stopListening()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}