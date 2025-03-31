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
 * A simple [Fragment] subclass.
 * Use the [UserInfoDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserInfoDialogFragment<FragmentManager> : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentUserInfoDialogBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentUserInfoDialogBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onCreateDialog(
        savedInstanceState: Bundle?): Dialog {
        // Get the current user.
        val currentUser =
            FirebaseAuth.getInstance().currentUser
        // Populate the dialog view with user information.
        currentUser?.let { user ->
            with(binding) {
                textViewName.text = user.displayName
                textViewEmail.text = user.email
                user.photoUrl?.let { url ->
                    imageViewPhoto.imageTintMode = null
                    Picasso.get().load(url).into(imageViewPhoto)
                }
            }
        }
        // Create and return a new dialog.
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.user_info_title)
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}