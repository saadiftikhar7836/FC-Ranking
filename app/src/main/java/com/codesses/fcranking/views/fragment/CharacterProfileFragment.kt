package com.codesses.fcranking.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.FragmentCharacterProfileBinding
import com.codesses.fcranking.enums.EnumIntents
import com.codesses.fcranking.enums.EnumVotes
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.model.Characters
import com.codesses.fcranking.model.FavouriteCharacter
import com.codesses.fcranking.model.User
import com.codesses.fcranking.utils.*
import com.codesses.fcranking.views.dialog.ProgressDialog

class   CharacterProfileFragment : Fragment() {

    companion object {
        private const val TAG: String = "VOTE_UPDATE_TRANSACTION"
    }

    // Context
    private lateinit var mContext: FragmentActivity

    // Data binding
    private lateinit var binding: FragmentCharacterProfileBinding

    // Model class
    private lateinit var character: Characters

    // Variables
    private var isVotedState = EnumVotes.NOT_VOTED_YET.value
    private var isVoteUndo = false
    private var ctaClick: String = ""
    private var caseStudy = 0
    private var isNotVoted = true
    private var isUpVoted = false
    private var isDevoted = false
    private var upvoteColor = R.color.dim_grey
    private var devoteColor = R.color.dim_grey
    private var favouriteCharactersCount = 0L
    private var isInFavouriteList = false

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        progressDialog = ProgressDialog(mContext)
        progressDialog.show()

        arguments?.let {
            character = arguments?.get("character") as Characters
        }

        mContext.supportFragmentManager
            .setFragmentResultListener("profile_character", this) { requestKey, bundle ->
                character = bundle.get("character") as Characters
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_character_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get character owner info
        getCharacterOwnerInfo()

        // Get favourite count
        getCurrentUserFavouriteCount()

        // Check user vote
        checkUserVote()

        // Check character is added in the favourite list
        checkFavouriteCharacter()

        // Set data
        setData()

        // Click listener
        binding.ivBackPress.setOnClickListener { mContext.onBackPressed() }
        binding.ivUpvote.setOnClickListener { handleCaseStudy(it, EnumVotes.UPVOTE.value) }
        binding.ivDevote.setOnClickListener { handleCaseStudy(it, EnumVotes.DEVOTE.value) }
        binding.tvUserName.setOnClickListener { openUserProfile() }
        binding.ivCharacterImage.setOnClickListener { openCharacterImage() }
        binding.ivFavourite.setOnClickListener { onFavouriteClick() }
        binding.tvWriteYourComment.setOnClickListener { openCommentsSheet() }
        binding.tvComment.setOnClickListener { openCommentsSheet() }

    }

    private fun openCharacterImage() {
        val navDirection = CharacterProfileFragmentDirections.actionCharacterProfileFragmentToFullScreenImageFragment()
            .setUrl(character.character_image)

        findNavController().navigate(navDirection)

    }


    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private fun getCurrentUserFavouriteCount() {
        if (character.proposed_by != FirestoreRef.getUserId().toString())
            FirestoreRef.getUserRef().document(FirestoreRef.getUserId().toString())
                .get().addOnSuccessListener {
                    it.toObject(User::class.java)?.let { currentUser ->
                        favouriteCharactersCount = currentUser.favourite_characters_count
                    }
                }.addOnFailureListener {
                    getCurrentUserFavouriteCount()
                }
    }

    private fun getCharacterOwnerInfo() {
        FirestoreRef.getUserRef().document(character.proposed_by)
            .get().addOnSuccessListener {
                val characterOwner = it.toObject(User::class.java)

                characterOwner?.let { user ->

                    user.userId = character.proposed_by
                    character.user = user

                    // Set user profile image
                    Glide.with(mContext)
                        .load(user.profile_image)
                        .placeholder(R.drawable.square_grey)
                        .thumbnail(0.1F)
                        .into(binding.civUserProfile)

                    // Set user name
                    binding.tvUserName.text = user.full_name.capitalizeWords

                }

            }.addOnFailureListener {
                getCharacterOwnerInfo()
            }
    }

    private fun checkUserVote() {
        FirestoreRef.getVotesRef(character.character_id)
            .orderBy(FirestoreRef.getUserId().toString())
            .get()
            .addOnSuccessListener { querySnapshot ->
                progressDialog.dismiss()
                querySnapshot.map {
                    val value = it.get(FirestoreRef.getUserId().toString()) as Boolean

                    isVotedState = if (value) {
                        EnumVotes.UPVOTE.value
                    } else {
                        EnumVotes.DEVOTE.value
                    }

                    updateCaseStudy()

                    Log.d(TAG, "checkUserVote: $value")

                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                mContext.showToast(mContext.getString(R.string.something_went_wrong))
            }
    }

    private fun checkFavouriteCharacter() {

        FirestoreRef.getFavouriteRef()
            .whereEqualTo("character_id", character.character_id)
            .whereEqualTo("user_id", FirestoreRef.getUserId().toString())
            .get()
            .addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty)
                    querySnapshot.map {
                        val favouriteCharacter = it.toObject(FavouriteCharacter::class.java)
                        favouriteCharacter.favouriteCharId = it.id

                        if (favouriteCharacter.character_id == character.character_id
                            && favouriteCharacter.user_id == FirestoreRef.getUserId().toString()
                        ) {
                            setFavoriteIcon()
                        }
                    }
                else setNotFavoriteIcon()

            }.addOnFailureListener {
                checkFavouriteCharacter()
            }

    }

    private fun setData() {

        // Character image
        Glide.with(mContext)
            .load(character.character_image)
            .placeholder(R.drawable.square_grey)
            .into(binding.ivCharacterImage)


        val dateTime = String.format(mContext.getString(R.string.at_date_time), character.date, character.time)

        binding.tvDateTime.text = dateTime
        binding.tvCharacterName.text = character.character_name.capitalizeWords
        binding.tvShowName.text = character.show_name
        binding.tvVoteCount.text = character.votes_count.toString()
        binding.tvRank.text = character.last_rank.toString()
        binding.tvBestRank.text = character.best_rank.toString()
        binding.tvWorstRank.text = character.worst_rank.toString()

    }

    private fun handleCaseStudy(view: View, value: String) {
        view.preventDoubleClick()
        ctaClick = value

        when {
            isCaseStudy1() -> {
                updateVoteCount(1)
            }
            isCaseStudy2() -> {
                updateVoteCount(1)
            }
            isCaseStudy3() -> {
                updateVoteCount(1)
            }
            isCaseStudy4() -> {
                updateVoteCount(2)
            }
            isCaseStudy5() -> {
                updateVoteCount(1)
            }
            isCaseStudy6() -> {
                updateVoteCount(2)
            }
        }

    }

    private fun updateVoteCount(incDecValue: Int) {
        var updatedVote: Long = -1000000
        var isVotePositive = false
        val characterRef = FirestoreRef.getCharacterRef()
            .document(character.character_id)

        FirestoreRef.getInstance()
            .runTransaction { transaction ->

                val snapshot = transaction.get(characterRef)
                val previousVote = (snapshot.getLong("votes_count") ?: 0).toInt()

                updatedVote = if (isCaseStudy1() || isCaseStudy5() || isCaseStudy6())
                    previousVote.plus(incDecValue)
                        .toLong()
                        .also { isVotePositive = true }
                else
                    previousVote.minus(incDecValue)
                        .toLong()
                        .also { isVotePositive = false }


                // Save count
                transaction.update(characterRef, "votes_count", updatedVote)

                Log.d(TAG, "updateVoteCount: $caseStudy $updatedVote")

                // Success
                null
            }
            .addOnSuccessListener {

                // confirmation popup
                showSnackBar(binding.root, mContext.getString(R.string.vote_has_been_counted))
                binding.tvVoteCount.text = updatedVote.toString()

                if (!isVoteUndo) {

                    if (isVotePositive)
                        saveUserId(true)
                    else
                        saveUserId(false)

                } else {
                    removeVoteData()
                }

                // Update case study
                updateCaseStudy()  // It must be called here otherwise not working here

                Log.d(TAG, "Transaction success!")
            }
            .addOnFailureListener {
                Log.d(TAG, "onFailure: ${it.message}")
            }
    }

    private fun saveUserId(value: Boolean) {
        val map: MutableMap<String, Any> = HashMap()
        map[FirestoreRef.getUserId()
            .toString()] = value

        FirestoreRef.getVotesRef(character.character_id)
            .document(FirestoreRef.getUserId().toString())
            .set(map)

        val batch = FirestoreRef.getInstance().batch()

        val recentVoteMap: MutableMap<String, Any> = HashMap()
        val votedUserMap: MutableMap<String, Any> = HashMap()

        recentVoteMap["character_id"] = character.character_id
        recentVoteMap["timestamp"] = System.currentTimeMillis()
        recentVoteMap["user_id"] = FirestoreRef.getUserId().toString()

        votedUserMap[FirestoreRef.getUserId().toString()] = value

        val recentVotesRef = FirestoreRef.getRecentVotesRef().document(character.character_id)
        val votedUserRef = FirestoreRef.getVotesRef(character.character_id).document(FirestoreRef.getUserId().toString())

        batch.set(recentVotesRef, recentVoteMap)
            .set(votedUserRef, votedUserMap)
            .commit()
            .addOnSuccessListener {
                Log.d(TAG, "saveUserId: $it")
            }
    }


    private fun removeVoteData() {

        FirestoreRef.getVotesRef(character.character_id).document(FirestoreRef.getUserId().toString()).delete()
        FirestoreRef.getRecentVotesRef().document(character.character_id).delete()

    }

    private fun updateCaseStudy() {
        when {
            isCaseStudy1() || (isVotedState == EnumVotes.UPVOTE.value) -> {
                isUpVoted = true
                isNotVoted = false
                isDevoted = false
                upvoteColor = R.color.Orange
                devoteColor = R.color.dim_grey
                isVotedState = EnumVotes.NOT_VOTED_YET.value
            }
            isCaseStudy2() || (isVotedState == EnumVotes.DEVOTE.value) -> {
                isDevoted = true
                isUpVoted = false
                isNotVoted = false
                upvoteColor = R.color.dim_grey
                devoteColor = R.color.Orange
                isVotedState = EnumVotes.NOT_VOTED_YET.value
            }
            isCaseStudy3()                                             -> {
                isNotVoted = true
                isDevoted = false
                isUpVoted = false
                upvoteColor = R.color.dim_grey
                devoteColor = R.color.dim_grey
            }
            isCaseStudy4() || (isVotedState == EnumVotes.DEVOTE.value) -> {
                isDevoted = true
                isNotVoted = false
                isUpVoted = false
                upvoteColor = R.color.dim_grey
                devoteColor = R.color.Orange
                isVotedState = EnumVotes.NOT_VOTED_YET.value
            }
            isCaseStudy5()                                             -> {
                isNotVoted = true
                isDevoted = false
                isUpVoted = false
                upvoteColor = R.color.dim_grey
                devoteColor = R.color.dim_grey
            }
            isCaseStudy6() || (isVotedState == EnumVotes.UPVOTE.value) -> {
                isUpVoted = true
                isDevoted = false
                isNotVoted = false
                upvoteColor = R.color.Orange
                devoteColor = R.color.dim_grey
                isVotedState = EnumVotes.NOT_VOTED_YET.value
            }
        }

        // Update upvote devote arrow colors
        updateArrowsColor()
    }

    private fun updateArrowsColor() {

        binding.ivUpvote.setColorFilter(
            ContextCompat.getColor(mContext, upvoteColor), android.graphics.PorterDuff.Mode.SRC_IN
        )

        binding.ivDevote.setColorFilter(
            ContextCompat.getColor(mContext, devoteColor), android.graphics.PorterDuff.Mode.SRC_IN
        )

    }

    private fun isCaseStudy1(): Boolean {
        // inc 1, upvote colored
        return if (isNotVoted && !isUpVoted && !isDevoted && ctaClick == EnumVotes.UPVOTE.value) {
            caseStudy = 1
            isVoteUndo = false
            true
        } else false
    }

    private fun isCaseStudy2(): Boolean {
        // dec 1, devote colored
        return if (isNotVoted && !isUpVoted && !isDevoted && ctaClick == EnumVotes.DEVOTE.value) {
            caseStudy = 2
            isVoteUndo = false
            true
        } else false
    }

    private fun isCaseStudy3(): Boolean {
        // inc 1 means user undo, upvote & devote uncolored
        return if (!isNotVoted && isUpVoted && !isDevoted && ctaClick == EnumVotes.UPVOTE.value) {
            caseStudy = 3
            isVoteUndo = true
            true
        } else false
    }

    private fun isCaseStudy4(): Boolean {
        // dec 2 means user already upvoted and now its devoted, devoted colored
        return if (!isNotVoted && isUpVoted && !isDevoted && ctaClick == EnumVotes.DEVOTE.value) {
            caseStudy = 4
            isVoteUndo = false
            true
        } else false
    }

    private fun isCaseStudy5(): Boolean {
        // inc 1 means user undo devote, upvote & devote uncolored
        return if (!isNotVoted && !isUpVoted && isDevoted && ctaClick == EnumVotes.DEVOTE.value) {
            caseStudy = 5
            isVoteUndo = true
            true
        } else false
    }

    private fun isCaseStudy6(): Boolean {
        // dec 2 means user already devoted and now its upvoted, upvote colored
        return if (!isNotVoted && !isUpVoted && isDevoted && ctaClick == EnumVotes.UPVOTE.value) {
            caseStudy = 6
            isVoteUndo = false
            true
        } else false
    }


    private fun openUserProfile() {
        val navDirections =
            CharacterProfileFragmentDirections.actionCharacterProfileFragmentToProfileFragment()
                .setUserId(character.user?.userId)
                .setParentFragment(EnumIntents.CHARACTER_PROFILE_FRAGMENT.value)
        findNavController().navigate(navDirections)
    }

    private fun onFavouriteClick() {
        if (!isInFavouriteList) {
            if (favouriteCharactersCount < 3) {

                favouriteCharactersCount += 1

                val batch = FirestoreRef.getInstance().batch()

                val favouriteCharMap: MutableMap<String, Any> = HashMap()
                val favouriteCharCountMap: MutableMap<String, Any> = HashMap()

                favouriteCharMap["user_id"] = FirestoreRef.getUserId().toString()
                favouriteCharMap["character_id"] = character.character_id

                favouriteCharCountMap["favourite_characters_count"] = favouriteCharactersCount

                setFavoriteIcon()

                val favouriteCharRef = FirestoreRef.getFavouriteRef().document()
                val favouriteCharCountRef = FirestoreRef.getUserRef()
                    .document(FirestoreRef.getUserId().toString())

                batch.set(favouriteCharRef, favouriteCharMap)
                    .update(favouriteCharCountRef, favouriteCharCountMap)
                    .commit()
                    .addOnSuccessListener {
                        setFavoriteIcon()
                    }.addOnFailureListener {
                        setNotFavoriteIcon()
                    }
            } else {
                showSnackBar(binding.root, mContext.getString(R.string.favourite_character_limitation))
            }
        } else {
            setNotFavoriteIcon()
            removeFavouriteCharacter()
        }
    }

    private fun removeFavouriteCharacter() {
        FirestoreRef.getFavouriteRef()
            .whereEqualTo("character_id", character.character_id)
            .whereEqualTo("user_id", FirestoreRef.getUserId().toString())
            .get().addOnSuccessListener { querySnapshot ->
                querySnapshot.map {

                    favouriteCharactersCount -= 1
                    val favouriteCharCountMap: MutableMap<String, Any> = HashMap()
                    favouriteCharCountMap["favourite_characters_count"] = favouriteCharactersCount


                    val favouriteCharRef = FirestoreRef.getFavouriteRef().document(it.id)
                    val favouriteCharCountRef = FirestoreRef.getUserRef()
                        .document(FirestoreRef.getUserId().toString())

                    FirestoreRef.getInstance().batch()
                        .delete(favouriteCharRef)
                        .update(favouriteCharCountRef, favouriteCharCountMap)
                        .commit().addOnSuccessListener { setNotFavoriteIcon() }
                        .addOnFailureListener { setFavoriteIcon() }
                }
            }.addOnFailureListener {
                setFavoriteIcon()
            }
    }


    private fun setFavoriteIcon() {
        isInFavouriteList = true
        binding.ivFavourite.visible(true)
        binding.ivFavourite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_solid))
    }

    private fun setNotFavoriteIcon() {
        isInFavouriteList = false
        binding.ivFavourite.visible(true)
        binding.ivFavourite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_stroke))

    }

    private fun openCommentsSheet() {
        val navDirection =
            CharacterProfileFragmentDirections.actionCharacterProfileFragmentToCommentsBottomSheet()
                .setCharacterId(character.character_id)
        findNavController().navigate(navDirection)
    }


}