package com.fcranking.android.views.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.fcranking.android.R
import com.fcranking.android.databinding.FragmentAccountBinding
import com.fcranking.android.enums.EnumIntents
import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.model.User
import com.fcranking.android.utils.*
import com.fcranking.android.views.auth.SignInActivity
import com.fcranking.android.views.dialog.ChangePasswordDialog
import com.fcranking.android.views.dialog.ProgressDialog

class AccountFragment : Fragment() {

    //    binding
    private lateinit var binding: FragmentAccountBinding

    // Context
    private lateinit var mContext: FragmentActivity

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    // Model class
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        progressDialog = ProgressDialog(mContext)
    }

    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.bind(inflater.inflate(R.layout.fragment_account, container, false)
        )

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set current user data
        setUserData()

        // Click listener
//        binding.editProfileImage.setOnClickListener { pickGalleryImage() }
        binding.tvEditProfile.setOnClickListener {
            it.preventDoubleClick()
            val navDirections: NavDirections =
                AccountFragmentDirections.actionNvAccountToProfileFragment()
                    .setUserId(FirestoreRef.getUserId().toString())
                    .setParentFragment(EnumIntents.ACCOUNT_FRAGMENT.value)
            it.findNavController().navigate(navDirections)
        }
        binding.tvNotifications.setOnClickListener {
            it.preventDoubleClick()
//            activity.openActivity(NotificationsActivity::class.java)
        }

        binding.tvChangePassword.setOnClickListener {
            it.preventDoubleClick()
            val changePasswordDialog = ChangePasswordDialog()
            activity?.supportFragmentManager?.let { it1 ->
                changePasswordDialog.show(
                    it1,
                    "Change Password Dialog"
                )
            }
        }

        binding.tvProposeCharacter.setOnClickListener {
            it.preventDoubleClick()
            val navDirections =
                AccountFragmentDirections.actionNvAccountToProposeCharacterFragment()
            it.findNavController().navigate(navDirections)
        }

        binding.tvPolicies.setOnClickListener {
            it.preventDoubleClick()
//            activity.openActivity(IapSendInvitationActivity::class.java)
        }

        binding.btnLogout.setOnClickListener {
            it.preventDoubleClick()
            userLogout()
        }

        binding.tvCharacterRequests.setOnClickListener {
            it.preventDoubleClick()
            val navDirections =
                AccountFragmentDirections.actionNvAccountToCharacterRequestsFragment()
            it.findNavController().navigate(navDirections)
        }

        binding.tvMyRequests.setOnClickListener {
            it.preventDoubleClick()
            val navDirections = AccountFragmentDirections.actionNvAccountToMyRequestsFragment()
            findNavController().navigate(navDirections)
        }

    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private fun setUserData() {
        user = FCSharedStorage.getUserObject()

        binding.tvFullName.text = user.full_name
        binding.tvEmail.text = user.email
        if (isAdmin()) {
            binding.tvCharacterRequests.visible(true)
            binding.tvMyRequests.visible(false)
        } else {
            binding.tvCharacterRequests.visible(false)
            binding.tvMyRequests.visible(true)
        }
        binding.civProfileImage.loadImage(mContext, user.profile_image, R.drawable.circle_grey)
    }


    private fun userLogout() {
        AlertDialog.Builder(mContext)
            .setCancelable(false)
            .setMessage(mContext.getString(R.string.text_logout_surety))
            .setNegativeButton(mContext.getText(R.string.label_no), null)
            .setPositiveButton(
                mContext.getText(R.string.label_yes)
            ) { _: DialogInterface?, _: Int ->
                progressDialog.show()
                removeFcmToken()
            }.create().show()
    }

    private fun removeFcmToken() {
        val map: MutableMap<String, Any> = HashMap()
        map["fcm_token"] = ""

        FirestoreRef.getUserRef().document(FirestoreRef.getUserId().toString())
            .update(map)
            .addOnCompleteListener {
                if (it.isSuccessful) {
//                    FirebaseMessaging.getInstance().unsubscribeFromTopic(EnumNotificationType.EVENT_UPLOAD.toString())

                    FirestoreRef.getAuth().signOut()
                    progressDialog.dismiss()
                    mContext.startNewActivity(SignInActivity::class.java)
                } else
                    Log.d("User_Logout", "Failed removeFcmToken: ${it.result}")
            }


    }

}