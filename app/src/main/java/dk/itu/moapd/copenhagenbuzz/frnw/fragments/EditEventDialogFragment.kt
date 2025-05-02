package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentEditEventDialogBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event
import dk.itu.moapd.copenhagenbuzz.frnw.models.EventLocation
import dk.itu.moapd.copenhagenbuzz.frnw.models.OperationStatus

/**
 * Dialog fragment for editing an existing event.
 * Only allows editing of events created by the current user.
 */
class EditEventDialogFragment : DialogFragment() {
    private var _binding: FragmentEditEventDialogBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    private var eventId: String = ""
    private lateinit var currentEvent: Event

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: String): EditEventDialogFragment {
            val fragment = EditEventDialogFragment()
            val args = Bundle()
            args.putString(ARG_EVENT_ID, eventId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getString(ARG_EVENT_ID) ?: ""
        if (eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Event ID not provided", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentEditEventDialogBinding.inflate(LayoutInflater.from(requireContext()))

        // Find event by ID
        findEventById()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // The dialog's view is already created in onCreateDialog
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe operation status
        observeOperationStatus()

        // Set up buttons
        setupButtons()
    }

    private fun findEventById() {
        val events = dataViewModel.events.value ?: emptyList()
        val event = events.find { it.id == eventId }

        if (event != null) {
            currentEvent = event

            // Check if user is the owner
            if (currentEvent.userId != auth.currentUser?.uid) {
                Toast.makeText(requireContext(), "You can only edit events you created", Toast.LENGTH_SHORT).show()
                dismiss()
                return
            }

            populateFormWithEventData()
        } else {
            // Event not found, show error and dismiss
            Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun populateFormWithEventData() {
        with(binding) {
            editTextEventName.setText(currentEvent.eventName)
            editTextEventLocation.setText(currentEvent.eventLocation.address)
            editTextEventDate.setText(currentEvent.eventDate)
            editTextEventType.setText(currentEvent.eventType)
            editTextEventDescription.setText(currentEvent.eventDescription)
        }
    }

    private fun observeOperationStatus() {
        dataViewModel.operationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is OperationStatus.Success -> {
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                    if (status.message.contains("updated") || status.message.contains("deleted")) {
                        dismiss()
                    }
                }
                is OperationStatus.Error -> {
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupButtons() {
        with(binding) {
            // Update button
            btnUpdateEvent.setOnClickListener {
                if (validateForm()) {
                    updateEvent()
                } else {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            // Delete button
            btnDeleteEvent.setOnClickListener {
                showDeleteConfirmationDialog()
            }

            // Cancel button
            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun validateForm(): Boolean {
        return with(binding) {
            editTextEventName.text.toString().isNotEmpty() &&
                    editTextEventLocation.text.toString().isNotEmpty() &&
                    editTextEventDate.text.toString().isNotEmpty() &&
                    editTextEventType.text.toString().isNotEmpty() &&
                    editTextEventDescription.text.toString().isNotEmpty()
        }
    }

    private fun updateEvent() {
        // Check if user is the owner of the event
        if (currentEvent.userId != auth.currentUser?.uid) {
            Toast.makeText(requireContext(), "You can only edit events you created", Toast.LENGTH_SHORT).show()
            return
        }

        // Update event with form data
        val updatedEvent = currentEvent.copy(
            eventName = binding.editTextEventName.text.toString(),
            eventLocation = EventLocation(
                latitude = currentEvent.eventLocation.latitude,
                longitude = currentEvent.eventLocation.longitude,
                address = binding.editTextEventLocation.text.toString()
            ),            eventDate = binding.editTextEventDate.text.toString(),
            eventType = binding.editTextEventType.text.toString(),
            eventDescription = binding.editTextEventDescription.text.toString()
        )

        // Use DataViewModel to update event
        dataViewModel.updateEvent(updatedEvent)
    }

    private fun showDeleteConfirmationDialog() {
        // Check if user is the owner of the event
        if (currentEvent.userId != auth.currentUser?.uid) {
            Toast.makeText(requireContext(), "You can only delete events you created", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                deleteEvent()
            }
            .show()
    }

    private fun deleteEvent() {
        dataViewModel.deleteEvent(currentEvent.id)
        Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}