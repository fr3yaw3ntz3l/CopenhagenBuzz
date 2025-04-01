package dk.itu.moapd.copenhagenbuzz.frnw.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.frnw.R
import dk.itu.moapd.copenhagenbuzz.frnw.databinding.FragmentUserInfoDialogBinding


/**
 * A dialog fragment that displays the current user's information.
 * This includes their name, email, and profile picture (if available).
 */
class UserInfoDialogFragment<FragmentManager> : DialogFragment() {

    // View binding for accessing UI elements
    private var _binding: FragmentUserInfoDialogBinding? = null

    // Ensures that _binding is not null when accessed
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    /**
     * Inflates the layout and initializes view binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Saved state for restoring the fragment.
     * @return The root view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentUserInfoDialogBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root


    /**
     * Creates the dialog displaying user information.
     *
     * @param savedInstanceState Saved instance state for restoring dialog state.
     * @return A new MaterialAlertDialog containing the user information.
     */
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        // Get the current Firebase user.
        val currentUser =
            FirebaseAuth.getInstance().currentUser

        // Populate the dialog view with user information.
        currentUser?.let { user ->
            with(binding) {
                textViewName.text = user.displayName
                textViewEmail.text = user.email

                // Load user profile image if available
                user.photoUrl?.let { url ->
                    imageViewPhoto.imageTintMode = null
                    Picasso.get().load(url).into(imageViewPhoto)
                }
            }
        }

        // Create and return a styled Material Alert Dialog
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.user_info_title)
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * Cleans up binding when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}