package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.activities.MyApplication
import dk.itu.moapd.copenhagenbuzz.frnw.adapter.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentFavoritesBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView with a LinearLayoutManager
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the Firebase adapter for favorites
        setupFirebaseAdapter(recyclerView)

        // Observe favorites LiveData for UI updates
        dataViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            Log.d("FavoritesFragment", "Favorites count: ${favorites.size}")
        }
    }

    private fun setupFirebaseAdapter(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        try {
            // Get database reference
            val database = FirebaseDatabase.getInstance(MyApplication.DATABASE_URL)

            // Create query to get only favorite events ordered by date
            val favoritesQuery = database.reference.child("events")
                .orderByChild("isFavorite")
                .equalTo(true)

            // Create FirebaseRecyclerOptions
            val options = FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(favoritesQuery) { snapshot ->
                    // Convert snapshot to Event object
                    val event = snapshot.getValue(Event::class.java)

                    // Make sure id is set from the snapshot key
                    event?.id = snapshot.key ?: ""

                    event!!
                }
                .build()

            // Create and set the adapter
            favoriteAdapter = FavoriteAdapter(options, dataViewModel)
            recyclerView.adapter = favoriteAdapter

            Log.d("FavoritesFragment", "Firebase adapter setup complete")
        } catch (e: Exception) {
            Log.e("FavoritesFragment", "Error setting up Firebase adapter", e)
        }
    }

    override fun onStart() {
        super.onStart()
        // Start listening for database changes
        if (::favoriteAdapter.isInitialized) {
            favoriteAdapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop listening for database changes
        if (::favoriteAdapter.isInitialized) {
            favoriteAdapter.stopListening()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}