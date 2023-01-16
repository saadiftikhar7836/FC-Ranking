package com.fcranking.android.views.fragment

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.fcranking.android.R
import com.fcranking.android.databinding.FragmentProposeCharacterBinding
import com.fcranking.android.enums.EnumIntents
import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.*
import com.fcranking.android.views.dialog.ProgressDialog
import com.google.android.material.textfield.TextInputEditText
import java.util.*


class ProposeCharacterFragment : Fragment() {

    //    binding
    lateinit var binding: FragmentProposeCharacterBinding

    //    progress dialog
    lateinit var progressDialog: ProgressDialog

    //    variables
    var isImageSelected = false
    var isCharacterName = false
    var isShowName = false
    var isAdmin = false

    var showName = ""
    var characterName = ""
    var imageUrl = ""
    var imageFirebaseUrl = ""

    var characters: Characters? = null

    //    context
    lateinit var mContext: FragmentActivity


    //    call back
    private var galleryPickLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri.toString()
                        .isNotEmpty()
                ) {
                    this.imageUrl = imageUri.toString()
                    binding.imgCharacterImage.loadImage(mContext, imageUrl, R.drawable.circle_grey)
                    isImageSelected = true
                    updateButtonState()
                }
            } else {
                isImageSelected = imageUrl.isNotEmpty()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        progressDialog = ProgressDialog(mContext)
        isAdmin = isAdmin()
        characters = arguments?.get("characterRequest") as Characters?
    }

    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProposeCharacterBinding.bind(
            inflater.inflate(
                R.layout.fragment_propose_character,
                container,
                false
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdmin && characters != null) {

            //  for character request send by a user
            handleCharacterRequest()

        } else {
            binding.btnSubmit.visibility = View.VISIBLE
            binding.conAcceptReject.visibility = View.GONE
            binding.imgCharacterImage.setOnClickListener {
                it.preventDoubleClick()
                pickImageFromGallery()
            }
        }

        //  text change listener
        textChangeListeners()

        //  click listener
        binding.btnSubmit.setOnClickListener {
            it.preventDoubleClick()
            progressDialog.show()
            uploadImageToFirebase()
        }

        binding.btnAccept.setOnClickListener {
            it.preventDoubleClick()
            progressDialog.show()
            updateRequestStatus(EnumIntents.ACCEPTED.value)
        }
        binding.btnReject.setOnClickListener {
            it.preventDoubleClick()
            progressDialog.show()
            updateRequestStatus(EnumIntents.REJECTED.value)
        }

        binding.btnBack.setOnClickListener {
            it.preventDoubleClick()
            mContext.onBackPressed()
        }
    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private fun uploadImageToFirebase() {
        val ref = FirestoreRef.getCharacterStorage()
            .child(
                UUID.randomUUID()
                    .toString()
            )
        ref
            .putFile(imageUrl.toUri())
            .addOnCompleteListener { it1 ->
                if (it1.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            imageFirebaseUrl = it.result.toString()
                            if (isAdmin) {
                                saveCharacterToFirebase()
                            } else {
                                sendCharacterRequest()
                            }
                        } else {
                            progressDialog.dismiss()
                            showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
                }
            }

    }

    private fun sendCharacterRequest() {

        val map: MutableMap<String, Any> = HashMap()

        map["character_name"] = characterName.lowercase()
        map["show_name"] = showName
        map["character_image"] = imageFirebaseUrl
        map["timestamp"] = System.currentTimeMillis()
        map["proposed_by"] = FirestoreRef.getUserId()
            .toString()
        map["date"] = DateTime.currentDateWithDay()
            .toString()
        map["time"] = DateTime.currentTime()
            .toString()
        map["status"] = "pending"

        FirestoreRef.getCharacterRequestsRef()
            .add(map)
            .addOnCompleteListener {
                progressDialog.dismiss()
                requireActivity().showToast(resources.getString(R.string.character_proposed_successfully))
                mContext.onBackPressed()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
            }

    }

    private fun pickImageFromGallery() {
        if (mContext.isGalleryPermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            galleryPickLauncher.launch(intent)
        }
    }

    private fun textChangeListeners() {
        binding.etCharacterNameField.addTextChangedListener {
            validateField(it.toString(), binding.etCharacterNameField)
        }
        binding.etShowNameField.addTextChangedListener {
            validateField(it.toString(), binding.etShowNameField)
        }
    }

    private fun validateField(input: String, editText: TextInputEditText) {
        when (editText) {
            binding.etCharacterNameField -> {

                characterName = input
                binding.etCharacterName.apply {
                    if (input.length >= 2) {
                        isCharacterName = true
                    } else {
                        error = resources.getString(R.string.invalid_character_name)
                        isCharacterName = false
                    }
                    isEndIconVisible = isCharacterName
                    isErrorEnabled = !isCharacterName
                }

            }

            binding.etShowNameField      -> {
                showName = input
                binding.etShowName.apply {
                    if (input.length >= 2) {
                        isShowName = true
                    } else {
                        error = resources.getString(R.string.invalid_character_name)
                        isShowName = false
                    }
                    isEndIconVisible = isShowName
                    isErrorEnabled = !isShowName
                }

            }
        }

        updateButtonState()
    }


    private fun updateButtonState() {
        binding.btnSubmit.apply {
            isEnabled =
                isImageSelected && isCharacterName && isShowName
            background = getDrawable(mContext, isEnabled)
        }
    }

    private fun updateRequestStatus(status: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status

        FirestoreRef.getCharacterRequestsRef()
            .document(characters?.character_id.toString())
            .update(map)
            .addOnCompleteListener {
                when (status) {
                    EnumIntents.ACCEPTED.value -> {
                        saveCharacterToFirebase()
                    }
                    EnumIntents.REJECTED.value -> {
                        progressDialog.dismiss()
                        mContext.onBackPressed()
                    }
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
            }
    }

    private fun saveCharacterToFirebase() {

        val map: MutableMap<String, Any> = HashMap()

        map["character_name"] = characterName.lowercase()
        map["show_name"] = showName
        map["character_image"] = imageFirebaseUrl
        map["proposed_by"] = FirestoreRef.getUserId()
            .toString()
        map["timestamp"] = System.currentTimeMillis()
        map["date"] = DateTime.currentDateWithDay()
            .toString()
        map["time"] = DateTime.currentTime()
            .toString()


        FirestoreRef.getCharacterRef()
            .add(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                requireActivity().showToast(resources.getString(R.string.character_posted_successfully))
                mContext.onBackPressed()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
            }
    }

    private fun handleCharacterRequest() {

        binding.btnSubmit.visibility = View.GONE
        binding.conAcceptReject.visibility = View.VISIBLE
        binding.etCharacterNameField.setText(characters?.character_name)
        binding.etShowNameField.setText(characters?.show_name)
        characterName = characters?.character_name.toString()
        showName = characters?.show_name.toString()
        imageFirebaseUrl = characters?.character_image.toString()


        //  disabling edit fields for character request send by a user for the admin
        binding.etCharacterName.isEnabled = false
        binding.etShowName.isEnabled = false

        binding.imgCharacterImage.apply {

            background = with(TypedValue()) {

                context.theme.resolveAttribute(
                    R.attr.selectableItemBackground, this, true
                )
                ContextCompat.getDrawable(context, resourceId)

            }

            this.loadImage(mContext, characters?.character_image.toString(), R.drawable.ic_add_image)
            setOnClickListener {

                it.preventDoubleClick()
                val navDirections =
                    ProposeCharacterFragmentDirections.actionProposeCharacterFragmentToFullScreenImageFragment()
                        .setUrl(imageFirebaseUrl)
                it.findNavController()
                    .navigate(navDirections)

            }

        }
    }
}