package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dk.itu.moapd.copenhagenbuzz.frnw.adapter.EventAdapter
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment() {
    private var _binding: FragmentTimelineBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    // Initialize the ViewModel using viewModels(
    private val dataViewModel: DataViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the events LiveData and update the ListView when data changes
        dataViewModel.events.observe(viewLifecycleOwner) { events ->
            val adapter = EventAdapter(requireContext(), ArrayList(events), dataViewModel)
            binding.listView.adapter = adapter
        }

        dataViewModel.favorites.observe(viewLifecycleOwner) {
            (binding.listView.adapter as? EventAdapter)?.notifyDataSetChanged()
        }

        // Fetch events when the fragment is created
        dataViewModel.fetchEvents()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
