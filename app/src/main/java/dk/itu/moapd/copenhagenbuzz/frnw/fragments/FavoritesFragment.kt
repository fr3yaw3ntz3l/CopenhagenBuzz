package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.adapter.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentFavoritesBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe LiveData for favorite events
        dataViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            Log.d("FavoritesFragment", "Favorites count: ${favorites.size}, Content: $favorites")
            val adapter = FavoriteAdapter(favorites)
            recyclerView.adapter = adapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}