/*
 * This file is part of CopenhagenBuzz
 *
 * Copyright (c) 2025 Freya Nørlund Wentzel
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the root of this project for more details.
 */

package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentAddEventBinding
import dk.itu.moapd.copenhagenbuzz.frnw.models.Event

/**
 * A Fragment for adding events in the CopenhagenBuzz application.
 *
 * This fragment provides UI components for user input and a submission button.
 * It handles user interactions and displays feedback when an event is added.
 */
class AddEventFragment : Fragment() {
    private var _binding: FragmentAddEventBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    // An instance of the `Event` class.
    private val event: Event = Event("", "", "", "", "", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAddEventBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            fabAddEvent.setOnClickListener {
                // Only execute the following code when the user fills all `EditText` fields.
                if (editTextEventName.text.toString().isNotEmpty() &&
                    editTextEventLocation.text.toString().isNotEmpty() &&
                    editTextEventDate.text.toString().isNotEmpty() &&
                    editTextEventType.text.toString().isNotEmpty() &&
                    editTextEventDescription.text.toString().isNotEmpty()
                ) {
                    // Show success message
                    showMessage()
                }
            }
        }
    }

    /**
     * Displays a Snackbar message with event details.
     */
    private fun showMessage() {
        val message = "Event added using\n$event"
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}