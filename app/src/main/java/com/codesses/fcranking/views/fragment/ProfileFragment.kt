package com.codesses.fcranking.views.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.FragmentProfileBinding
import com.codesses.fcranking.enums.EnumIntents
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.interfaces.OnCharacterClick
import com.codesses.fcranking.model.Characters
import com.codesses.fcranking.model.FavouriteCharacter
import com.codesses.fcranking.model.RecentVotesCharacter
import com.codesses.fcranking.model.User
import com.codesses.fcranking.utils.*
import com.codesses.fcranking.views.adapter.CharacterAdapter
import com.codesses.fcranking.views.dialog.ProgressDialog
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class ProfileFragment : Fragment(), OnCharacterClick {

    //    binding
    lateinit var binding: FragmentProfileBinding

    //    context
    lateinit var mContext: FragmentActivity

    //    variable
    lateinit var userId: String
    private lateinit var parentFragment: String
    lateinit var user: User
    lateinit var selectedType: String

    //    adapter
    private lateinit var favouriteCharactersAdapter: CharacterAdapter
    private lateinit var recentVotesCharAdapter: CharacterAdapter

    // Arraylist
    private val favCharList by lazy { ArrayList<Characters>() }
    private val recentVotesList by lazy { ArrayList<Characters>() }


    //    progress dialog
    lateinit var progressDialog: ProgressDialog

    lateinit var navigationController: NavController

    //    call back
    private var galleryPickLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data

                if (imageUri.toString().isNotEmpty()) {

                    // Show progress dialog
                    progressDialog.show()

                    // Upload profile image
                    when (selectedType) {

                        EnumIntents.PHOTO.value -> {
                            binding.civProfileImage.loadImage(mContext, imageUri.toString(), R.drawable.circle_grey)
                            imageUri?.let { uploadProfileImage(it) }
                        }

                        EnumIntents.COVER.value -> {
                            binding.imgCoverPhoto.loadImage(mContext, imageUri.toString(), R.drawable.circle_grey)
                            imageUri?.let { uploadCoverImage(it) }
                        }
                    }

                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.get("user_id").toString()
        parentFragment = arguments?.get("parent_fragment").toString()
        mContext = requireActivity()
        progressDialog = ProgressDialog(mContext)
        favouriteCharactersAdapter = CharacterAdapter(mContext = mContext, this)
        recentVotesCharAdapter = CharacterAdapter(mContext = mContext, this)

        navigationController = findNavController()

    }

    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.bind(getView(R.layout.fragment_profile, mContext, container!!))

        // Check current user is following or not
        checkIsUserFollower()

        // Get followers and following count
        getFollowersCount()
        getFollowingsCount()

        // Get favourite characters id
        getFavouriteCharactersId()

        // Get recent votes characters id
        getRecentVoteCharId()

        //  appBar listener
        setAppBarListener()

        setAdapters()

        //  setting visibility of camera buttons for current user and click handling for current user
        setUiForCurrentUser()


        //  show progress dialog
        progressDialog.show()

        // getting user data
        getUserData()

        // initialization of clickListeners
        initializeClickListeners()

        return binding.root
    }


    override fun onClick(character: Characters) {
        if (parentFragment == EnumIntents.ACCOUNT_FRAGMENT.value)
            navigationController.navigate(ProfileFragmentDirections.actionProfileFragmentToCharacterProfileFragment(character))
        else {
            mContext.supportFragmentManager.setFragmentResult("profile_character", bundleOf("character" to character))
            mContext.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        mContext.setStatusBarColor(R.color.white, true)
    }


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/



    private fun showChooser(type: String, url: String) {
        selectedType = type
        val navDirections = ProfileFragmentDirections.actionProfileFragmentToPhotoBottomSheet()
        navDirections.type = type
        navDirections.url = url
        navigationController.navigate(navDirections)
    }

    private fun showImage(url: String) {
        navigationController.currentBackStackEntry?.savedStateHandle?.remove<String>("url")

        val navDirection: NavDirections =
            ProfileFragmentDirections.actionProfileFragmentToFullScreenImageFragment()
                .setUrl(url)
        navigationController.navigate(navDirection)
    }

    private fun setAdapters() {
        binding.rvFavouriteCharacter.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = favouriteCharactersAdapter
        }
        binding.rvRecentVotedCharacter.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentVotesCharAdapter
        }
    }

    private fun setUiForCurrentUser() {

        if (userId == FirestoreRef.getUserId()) {
            binding.conCameraCover.visible(true)
            binding.conCameraProfile.visible(true)
            binding.civProfileImage.setOnClickListener {
                it.preventDoubleClick()
                showChooser(EnumIntents.PHOTO.value, user.profile_image)
            }
            binding.imgCoverPhoto.setOnClickListener {
                it.preventDoubleClick()
                showChooser(EnumIntents.COVER.value, user.cover_photo)
            }
        } else {
            binding.conCameraCover.visible(false)
            binding.conCameraProfile.visible(false)

            binding.civProfileImage.setOnClickListener {
                it.preventDoubleClick()
                showImage(user.profile_image)
            }
            binding.imgCoverPhoto.setOnClickListener {
                it.preventDoubleClick()
                showImage(user.cover_photo)
            }
        }

    }

    private fun initializeClickListeners() {
        binding.btnBack.setOnClickListener {
            it.preventDoubleClick()
            mContext.onBackPressed()
        }

        binding.conCameraProfile.setOnClickListener {
            it.preventDoubleClick()
            showChooser(EnumIntents.PHOTO.value, user.profile_image)
        }
        binding.conCameraCover.setOnClickListener {
            it.preventDoubleClick()
            showChooser(EnumIntents.COVER.value, user.cover_photo)
        }
        binding.btnFollow.setOnClickListener { onFollowClick() }
        binding.btnUnfollow.setOnClickListener { onUnFollowClick() }

        setFragmentResultListener("url") { key, bundle ->
            // read from the bundle
            navigationController.navigateUp()
            if (bundle.getString("url") != "pick" && bundle.getString("url")
                    .toString()
                    .isNotEmpty()
            ) {
                showImage(bundle.getString("url").toString())
            } else if (bundle.getString("url") == "pick") {
                pickGalleryImage()
            }
        }
    }

    private fun setAppBarListener() {
        binding.appBar.apply {
            var isShow = true
            var scrollRange: Int = -1
            addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    mContext.setStatusBarColor(R.color.color_application, false)
                    binding.userName.visible(true)
                    isShow = true
                } else if (isShow) {
                    mContext.setStatusBarColor(R.color.white, true)
                    binding.userName.visible(false)
                    isShow = false
                }
            })
        }
    }

    private fun getUserData() {
        FirestoreRef.getUserRef()
            .document(userId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    user = it.result?.toObject(User::class.java) ?: User()
                    binding.userName.text = user.full_name
                    binding.tvNoOfComments.text = user.comments_count.toString()
                    setUserProfileData()
                } else {
                    progressDialog.dismiss()
                    showSnackBar(binding.root, resources.getString(R.string.something_went_wrong))
                }
            }
    }

    private fun setUserProfileData() {
        binding.imgCoverPhoto.loadImage(mContext, user.cover_photo, R.drawable.square_grey)
        binding.civProfileImage.loadImage(mContext, user.profile_image, R.drawable.circle_grey)
        progressDialog.dismiss()
    }

    private fun pickGalleryImage() {
        if (mContext.isGalleryPermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            galleryPickLauncher.launch(intent)
        }
    }

    private fun uploadProfileImage(profileImage: Uri) {
        val ref = FirestoreRef.getProfileStorage()
            .child(
                FirestoreRef.getUserId()
                    .toString()
            )

        ref.putFile(profileImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { taskUrl ->
                        if (taskUrl.isSuccessful) {
                            updateUser(taskUrl.result)
                        }
                    }
                } else
                    showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))

            }
    }

    private fun updateUser(result: Uri?) {

        var keyToUpdate = "profile_image"

        when (selectedType) {
            EnumIntents.COVER.value -> {
                keyToUpdate = "cover_photo"
            }
            EnumIntents.PHOTO.value -> {
                keyToUpdate = "profile_image"
            }
        }
        FirestoreRef.getUserRef().document(FirestoreRef.getUserId().toString())
            .update(keyToUpdate, result.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showSnackBar(binding.root, mContext.getString(R.string.data_updated_successfully))

                    // Update local data
                    user = FCSharedStorage.getUserObject()
                    when (selectedType) {
                        EnumIntents.COVER.value -> {
                            user.cover_photo = result.toString()
                        }
                        EnumIntents.PHOTO.value -> {
                            user.profile_image = result.toString()
                        }
                    }
                    FCSharedStorage.saveUserData(user)

                    // Dismiss progress bar
                    progressDialog.dismiss()

                } else {
                    showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
                }
            }
    }

    private fun uploadCoverImage(coverPhoto: Uri) {
        val ref = FirestoreRef.getProfileCoverStorage().child(FirestoreRef.getUserId().toString())

        ref.putFile(coverPhoto)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { taskUrl ->
                        if (taskUrl.isSuccessful) {
                            updateUser(taskUrl.result)
                        }
                    }
                } else
                    showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))

            }

    }

    private fun getFollowersCount() {
        FirestoreRef.getFollowersRef(userId)
            .addSnapshotListener { snapshot, error ->
                snapshot?.let {
                    binding.tvNoOfFollowers.text = it.count().toString()
                } ?: kotlin.run {
                    getFollowersCount()
                }
            }
    }

    private fun getFollowingsCount() {
        FirestoreRef.getFollowingRef(userId)
            .addSnapshotListener { snapshot, error ->
                snapshot?.let {
                    binding.tvNoOfFollowing.text = it.count().toString()
                } ?: kotlin.run {
                    getFollowingsCount()
                }
            }
    }

    private fun checkIsUserFollower() {
        if (userId != FirestoreRef.getUserId().toString()) {

            FirestoreRef.getFollowersRef(userId)
                .orderBy(FirestoreRef.getUserId().toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty)
                        querySnapshot.map {
                            val value = it.get(FirestoreRef.getUserId().toString()) as Boolean

                            Log.d("Follow_Unfollow", "checkIsUserFollower: $value || $querySnapshot || $it")
                            if (value)
                                setFollowState()
                            else
                                setUnfollowState()
                        }
                    else setUnfollowState()
                }
        } else hideFollowUnfollowCtas()
    }

    private fun getFavouriteCharactersId() {
        favCharList.clear()
        FirestoreRef.getFavouriteRef()
            .whereEqualTo("user_id", userId)
            .get().addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty) {
                    handleRvFavoriteCharacter(true)
                    querySnapshot.map { snapshot ->
                        val favourite = snapshot.toObject(FavouriteCharacter::class.java)
                        getCharacter(favourite.character_id)
                    }
                } else
                    handleRvFavoriteCharacter(false)

            }.addOnFailureListener {
                getFavouriteCharactersId()
            }

    }

    private fun getCharacter(characterId: String) {

        FirestoreRef.getCharacterRef().document(characterId)
            .get().addOnSuccessListener { snapshot ->

                val favCharacter = snapshot.toObject(Characters::class.java)
                favCharacter?.character_id = snapshot.id
                favCharacter?.let { favCharList.add(it) }
                favouriteCharactersAdapter.submitList(favCharList)
                favouriteCharactersAdapter.notifyDataSetChanged()

            }.addOnFailureListener {
                getCharacter(characterId)
            }

    }

    private fun getRecentVoteCharId() {
        recentVotesList.clear()
        FirestoreRef.getRecentVotesRef()
            .whereEqualTo("user_id", userId)
            .get().addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty) {

                    handleRvRecentVoteCharacter(true)
                    val charIdsList = ArrayList<RecentVotesCharacter>()
                    querySnapshot.map {
                        val votedCharId = it.toObject(RecentVotesCharacter::class.java)
                        charIdsList.add(votedCharId)
                    }

                    val list = charIdsList.sortedByDescending { it.timestamp }.take(3)
                    Log.d("Recent_Votes_Id", "getRecentVoteCharId: $$list")

                    for (votedList in list) {
                        getVotedCharacter(votedList.character_id)
                    }


                } else handleRvRecentVoteCharacter(false)


            }
    }

    private fun getVotedCharacter(characterId: String) {

        FirestoreRef.getCharacterRef().document(characterId)
            .get().addOnSuccessListener { snapshot ->

                val votedCharacter = snapshot.toObject(Characters::class.java)
                votedCharacter?.character_id = snapshot.id
                votedCharacter?.let { recentVotesList.add(it) }
                recentVotesCharAdapter.submitList(recentVotesList)
                recentVotesCharAdapter.notifyDataSetChanged()

            }.addOnFailureListener {
                getVotedCharacter(characterId)
            }

    }

    private fun onFollowClick() {
        handleCtaProgress("follow", true)
        val batch = FirestoreRef.getInstance().batch()

        val followerMap: MutableMap<String, Any> = HashMap()
        val followingMap: MutableMap<String, Any> = HashMap()

        followerMap[FirestoreRef.getUserId().toString()] = true
        followingMap[userId] = true

        val followersRef = FirestoreRef.getFollowersRef(userId).document(FirestoreRef.getUserId().toString())
        val followingRef = FirestoreRef.getFollowingRef(FirestoreRef.getUserId().toString()).document(userId)

        batch
            .set(followersRef, followerMap)
            .set(followingRef, followingMap)
            .commit()
            .addOnSuccessListener {
                handleCtaProgress("follow", false)
                setFollowState()
            }.addOnFailureListener { onFollowClick() }

    }

    private fun onUnFollowClick() {
        handleCtaProgress("unfollow", true)
        val batch = FirestoreRef.getInstance().batch()

        val followersRef = FirestoreRef.getFollowersRef(userId).document(FirestoreRef.getUserId().toString())
        val followingRef = FirestoreRef.getFollowingRef(FirestoreRef.getUserId().toString()).document(userId)

        batch
            .delete(followersRef)
            .delete(followingRef)
            .commit()
            .addOnSuccessListener {
                handleCtaProgress("unfollow", false)
                setUnfollowState()
            }.addOnFailureListener { onUnFollowClick() }

    }

    private fun handleCtaProgress(ctaType: String, value: Boolean) {

        if (ctaType == "follow") {
            binding.pbFollow.visible(value)
            binding.btnFollow.enable(!value)
        } else {
            binding.pbUnfollow.visible(value)
            binding.btnUnfollow.enable(!value)
        }

    }

    private fun setFollowState() {
        binding.btnFollow.enable(false)
        binding.btnUnfollow.visible(true)
        binding.btnFollow.visible(true)
        binding.btnFollow.text = mContext.getString(R.string.following)
        binding.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.black))
    }

    private fun setUnfollowState() {
        binding.btnUnfollow.visible(false)
        binding.btnFollow.enable(true)
        binding.btnFollow.visible(true)
        binding.btnFollow.text = mContext.getString(R.string.follow)
        binding.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white))
    }

    private fun hideFollowUnfollowCtas() {
        binding.btnFollow.visible(false)
        binding.btnUnfollow.visible(false)
    }

    private fun handleRvFavoriteCharacter(value: Boolean) {
        binding.tvNoFavoriteCharacter.visible(!value)
        binding.rvFavouriteCharacter.visible(value)
    }

    private fun handleRvRecentVoteCharacter(value: Boolean) {
        binding.tvNoRecentVotesCharacter.visible(!value)
        binding.rvRecentVotedCharacter.visible(value)
    }

}