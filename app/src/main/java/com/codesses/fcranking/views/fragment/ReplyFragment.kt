package com.codesses.fcranking.views.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.FragmentReplyBinding
import com.codesses.fcranking.enums.CommentType
import com.codesses.fcranking.enums.EnumIntents
import com.codesses.fcranking.enums.WidgetType
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.model.Comments
import com.codesses.fcranking.model.User
import com.codesses.fcranking.utils.*
import com.codesses.fcranking.viewmodel.CommentsViewModel
import com.codesses.fcranking.views.adapter.CommentsAdapter
import com.codesses.fcranking.views.dialog.ProgressDialog
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import java.util.*

class ReplyFragment : Fragment(), GiphyDialogFragment.GifSelectionListener {
    private var _binding: FragmentReplyBinding? = null

    lateinit var mContext: FragmentActivity
    private lateinit var navigationController: NavController
    private val binding get() = _binding!!

    //    variables
    var imageUrl = ""
    var gifUrl = ""
    var characterId = ""
    var commentId = ""
    var comment = ""
    var retryCount = 0

    //    user object
    lateinit var user: User

    lateinit var comments: Comments

    val progressDialog: ProgressDialog by lazy { ProgressDialog(mContext) }

    val viewModel: CommentsViewModel by viewModels()

    private val commentsAdapter: CommentsAdapter by lazy {
        CommentsAdapter(mContext, characterId, false, onClick = { comment, widgetClicked ->
            when (widgetClicked) {
                WidgetType.LIKE.value      -> {
                    changeLikesCount(comment)
                }
                WidgetType.USER_NAME.value -> {
                    val direction = CommentsFragmentDirections.actionCommentsBottomSheetToProfileFragment()
                    direction.userId = comment.user.userId
                    direction.parentFragment = EnumIntents.REPLY.value
                    navigationController.navigate(direction)
                }
            }
        })
    }

    lateinit var linearLayoutManager: LinearLayoutManager

    lateinit var commentsList: MutableList<Comments>

    var isLoading = false
    var isNextItemExists = false

    val settings = GPHSettings(GridType.waterfall, GPHTheme.Dark)

    lateinit var giphyDialog: GiphyDialogFragment

    //    call back
    private var galleryPickLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data

                if (imageUri.toString()
                        .isNotEmpty()
                ) {
                    imageUrl = imageUri.toString()
                    gifUrl = ""
                    binding.ivPostComment.setColorFilter(
                        ContextCompat.getColor(mContext, R.color.color_application), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    hideOrShowGif(false)
                    hideOrShowSelectedImage(true)
                    binding.ivSelectedImage.loadImage(mContext, imageUrl, R.drawable.square_grey)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        characterId = arguments?.get("characterId") as String
        comments = arguments?.get("comment") as Comments
        commentId = comments.comment_id
        user = FCSharedStorage.getUserObject()
        linearLayoutManager = LinearLayoutManager(mContext)
        commentsList = ArrayList()

        giphyDialog =
            GiphyDialogFragment.newInstance(settings.copy(selectedContentType = GPHContentType.gif), AppConstants.GIPHY_APP_KEY)

        giphyDialog.gifSelectionListener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReplyBinding.inflate(inflater, container, false)

        navigationController = findNavController()

        initializeTextChangeListener()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.ivSelectImage.setOnClickListener {
            pickGalleryImage()
        }

        binding.ivCrossImage.setOnClickListener {
            hideOrShowSelectedImage(false)
            hideOrShowGif(false)
            imageUrl = ""
            gifUrl = ""
            binding.ivPostComment.setColorFilter(
                ContextCompat.getColor(mContext, R.color.dimGrey), android.graphics.PorterDuff.Mode.SRC_IN
            )

        }

        binding.ivPostComment.setOnClickListener {
            retryCount = 0
            prepareCommentPostRequest()
        }

        binding.ivBackPress.setOnClickListener {
            navigationController.navigateUp()
        }

        binding.ivGif.setOnClickListener {
            giphyDialog.gifSelectionListener = this
            giphyDialog.show(mContext.supportFragmentManager, "")
        }

        viewModel.fetchReplies(characterId, commentId)

        observer()

        addScrollListener()

        addSwipeRefreshListener()

    }

    private fun addSwipeRefreshListener() {
        binding.srlSwipe.setOnRefreshListener {
            viewModel.refreshReplies(characterId, commentId)
        }
    }

    private fun addScrollListener() {
        binding.rvCommentsReply.apply {
            layoutManager = linearLayoutManager
            adapter = commentsAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!isLoading) {

                        val lastVisibleItemPosition: Int =
                            linearLayoutManager.findLastCompletelyVisibleItemPosition()
                        val totalItemCount: Int = linearLayoutManager.itemCount

                        if (totalItemCount > 0) {
                            if (lastVisibleItemPosition == totalItemCount - 1) {
                                if (isNextItemExists) {
                                    if (totalItemCount >= 10)
                                        binding.progressBar.isVisible = true
                                    viewModel.fetchReplies(characterId, commentId)
                                    isLoading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun observer() {
        viewModel.replies.observe(viewLifecycleOwner) {
            isLoading = false
            binding.srlSwipe.isRefreshing = false
            binding.progressBar.isVisible = false
            commentsList.clear()
            commentsList.addAll(it)
            commentsAdapter.submitList(commentsList)
            commentsAdapter.notifyDataSetChanged()
        }
        viewModel.isNextReplyExists
            .observe(viewLifecycleOwner) {
                isNextItemExists = it ?: false
            }
    }

    private fun prepareCommentPostRequest() {
        if (comment.isEmpty() && imageUrl.isEmpty() && gifUrl.isEmpty()) {
            showSnackBar(binding.root, "Please enter the comment")
            return
        }

        val map: MutableMap<String, Any> = HashMap()
        map["text"] = comment
        map["timestamp"] = System.currentTimeMillis()
        map["date"] = DateTime.currentDateWithDay()
            .toString()
        map["time"] = DateTime.currentTime()
            .toString()
        map["commented_by"] = ""
        map["replied_by"] = FirestoreRef.getUserId()
            .toString()
        when {
            imageUrl.isNotEmpty() -> getImageUrl(map)
            gifUrl.isNotEmpty()   -> {
                progressDialog.show()
                map["image_url"] = ""
                map["gif_url"] = gifUrl
                map["type"] = getCommentType()
                saveDataToFirestore(map)
            }
            else                  -> {
                progressDialog.show()
                map["image_url"] = ""
                map["gif_url"] = ""
                map["type"] = getCommentType()
                saveDataToFirestore(map)
            }
        }

    }

    @JvmName("getImageUrl1")
    private fun getImageUrl(map: MutableMap<String, Any>) {
        progressDialog.show()
        val ref =
            FirestoreRef.getCommentsStorage()
                .child(
                    UUID.randomUUID()
                        .toString()
                )
        ref.putFile(imageUrl.toUri())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { it1 ->
                        if (it1.isSuccessful) {
                            map["image_url"] = it1.result.toString()
                            map["type"] = getCommentType()
                            map["gif_url"] = ""
                            saveDataToFirestore(map)
                        }
                    }
                } else {
                    showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
                }
            }

    }

    private fun saveDataToFirestore(map: MutableMap<String, Any>) {
        val userMap: MutableMap<String, Any> = HashMap()
        userMap["comments_count"] = user.comments_count + 1L

        val replyRef = FirestoreRef.getCommentsReplyRef(characterId, commentId = commentId)
        val userRef = FirestoreRef.getUserRef()
            .document(
                FirestoreRef.getUserId()
                    .toString()
            )

        progressDialog.dismiss()

        FirestoreRef.getInstance()
            .runBatch {
                it.set(replyRef.document(), map)
                    .update(userRef, userMap)
            }
            .addOnSuccessListener {
                user.comments_count = user.comments_count + 1L
                FCSharedStorage.saveUserData(user)
                hideOrShowSelectedImage(false)
                hideOrShowGif(false)
                comment = ""
                binding.etCommentBox.setText(comment)
                imageUrl = ""
                gifUrl = ""
                viewModel.refreshReplies(characterId, comments.comment_id)
            }
            .addOnFailureListener {
                if (retryCount < 3) {
                    retryCount++
                    saveDataToFirestore(map)
                } else {
                    showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
                }
            }

    }

    private fun hideOrShowSelectedImage(value: Boolean) {
        binding.ivSelectedImage.isVisible = value
        binding.ivCrossImage.isVisible = value
    }

    private fun hideOrShowGif(value: Boolean) {
        binding.givGif.isVisible = value
        binding.ivCrossImage.isVisible = value
    }

    private fun getCommentType(): Int {
        var type = 0
        if (comment.isNotEmpty() && gifUrl.isNotEmpty())
            type = CommentType.TEXT_AND_GIF.value
        else if (comment.isNotEmpty() && imageUrl.isNotEmpty())
            type = CommentType.TEXT_AND_IMAGE.value
        else if (gifUrl.isNotEmpty())
            type = CommentType.GIF_ONLY.value
        else if (imageUrl.isNotEmpty())
            type = CommentType.IMAGE_ONLY.value
        else if (comment.isNotEmpty())
            type = CommentType.TEXT_ONLY.value

        return type
    }

    private fun pickGalleryImage() {
        if (mContext.isGalleryPermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            galleryPickLauncher.launch(intent)
        }
    }

    private fun initializeTextChangeListener() {
        binding.etCommentBox.addTextChangedListener {
            comment = it.toString()
            if (it.toString().isNotEmpty() || imageUrl.isNotEmpty() || gifUrl.isNotEmpty()) {
                binding.ivPostComment.setColorFilter(
                    ContextCompat.getColor(mContext, R.color.color_application), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.ivPostComment.setColorFilter(
                    ContextCompat.getColor(mContext, R.color.dimGrey), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun changeLikesCount(comment: Comments) {
        val db = FirestoreRef.getInstance()

        val likeMap: MutableMap<String, Any> = HashMap()
        likeMap[FirestoreRef.getUserId().toString()] = comment.isLiked
        val likeRef =
            FirestoreRef.getCommentReplyLikesRef(characterId, commentId, comment.comment_id)
                .document(FirestoreRef.getUserId().toString())

        val commentRef = FirestoreRef.getCommentsReplyRef(characterId, commentId)
            .document(comment.comment_id)
        val commentMap: MutableMap<String, Any> = HashMap()
        commentMap["likes_count"] = comment.likes_count

        db.runBatch {
            it.set(likeRef, likeMap)
                .update(commentRef, commentMap)
        }.addOnSuccessListener {
            viewModel.refreshReplies(characterId, commentId)
        }.addOnFailureListener {
            changeLikesCount(comment)
        }
    }


    override fun didSearchTerm(term: String) {

    }

    override fun onDismissed(selectedContentType: GPHContentType) {

    }

    override fun onGifSelected(media: Media, searchTerm: String?, selectedContentType: GPHContentType) {
        giphyDialog.dismiss()
        gifUrl = media.images.original?.gifUrl.toString()
        binding.ivPostComment.setColorFilter(
            ContextCompat.getColor(mContext, R.color.color_application), android.graphics.PorterDuff.Mode.SRC_IN
        )
        hideOrShowGif(true)
        binding.ivSelectedImage.visibility = View.INVISIBLE
        binding.givGif.loadGif(mContext, media.images.original?.gifUrl.toString(), R.drawable.square_grey)
    }
}