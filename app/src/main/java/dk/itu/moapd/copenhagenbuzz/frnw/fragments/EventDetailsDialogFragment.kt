package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentEventDetailsDialogBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel

class EventDetailsDialogFragment : DialogFragment() {
    private var _binding: FragmentEventDetailsDialogBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()
    private var eventId: String = ""

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: String): EventDetailsDialogFragment {
            val fragment = EventDetailsDialogFragment()
            val args = Bundle()
            args.putString(ARG_EVENT_ID, eventId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getString(ARG_EVENT_ID) ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentEventDetailsDialogBinding.inflate(layoutInflater)

        // Find the event
        val event = dataViewModel.events.value?.find { it.id == eventId }

        // Populate UI with event details
        event?.let {
            binding.textViewEventName.text = it.eventName
            binding.textViewEventType.text = it.eventType
            binding.textViewEventLocation.text = it.eventLocation.address
            binding.textViewEventDate.text = it.eventDate
            binding.textViewEventDescription.text = it.eventDescription

            // Load image
            Picasso.get()
                .load(it.eventPhotoUrl)
                .placeholder(R.drawable.baseline_refresh_24)
                .error(R.drawable.baseline_image_not_supported_24)
                .into(binding.imageViewEvent)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}