package com.fcranking.android.views.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fcranking.android.R
import com.fcranking.android.databinding.LayoutItemCharacterCommentsBinding
import com.fcranking.android.enums.CommentType
import com.fcranking.android.model.Comments
import com.fcranking.android.utils.getView
import com.fcranking.android.utils.loadGif
import com.fcranking.android.utils.loadImage
import com.fcranking.android.utils.setImageSrc

class CommentsAdapter(val mContext: Context, val characterId: String, private val isComment: Boolean, val onClick: (Comments, Int) -> Unit) : ListAdapter<Comments, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Comments>() {

        override fun areItemsTheSame(oldItem: Comments, newItem: Comments): Boolean =
            oldItem.comment_id == newItem.comment_id

        override fun areContentsTheSame(oldItem: Comments, newItem: Comments): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            LayoutItemCharacterCommentsBinding.bind(getView(R.layout.layout_item_character_comments, parent.context, parent))
        val viewHolder = ViewHolder(binding)
        viewHolder.setIsRecyclable(false)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val comment = currentList[position]

        viewHolder.binding.apply {
            tvDateTime.text = tvDateTime.context.getString(R.string.at_date_time, comment.date, comment.time)
            tvUsername.text = comment.user.full_name
            civUserProfileImage.loadImage(mContext, comment.user.profile_image, R.drawable.circle_grey)
            tvUsername.setOnClickListener {
                onClick(comment, 2)
            }
            llLike.setOnClickListener {
                comment.isLiked = !comment.isLiked
                if (comment.isLiked) {
                    comment.likes_count = comment.likes_count + 1L
                } else {
                    comment.likes_count = comment.likes_count - 1L
                }
                setCount(tvLike, comment.likes_count.toInt(), R.plurals.like, R.string.like)
                ivLike.setImageSrc(comment.isLiked)
                onClick(comment, 0)
            }
            ivLike.setImageSrc(comment.isLiked)

            // conditions
            setCount(tvLike, comment.likes_count.toInt(), R.plurals.like, R.string.like)
            setCount(tvComment, comment.repliesCount, R.plurals.reply, R.string.reply)
            if (isComment) {
                llComment.setOnClickListener {
                    onClick(comment, 1)
                }
            } else {
                llComment.visibility = View.INVISIBLE
            }
            when (comment.type) {
                CommentType.TEXT_ONLY.value      -> {
                    tvCommentText.isVisible = true
                    ivCommentImage.isVisible = false
                    givGif.isVisible = false
                    tvCommentText.text = comment.text
                }
                CommentType.IMAGE_ONLY.value     -> {
                    tvCommentText.isVisible = false
                    ivCommentImage.isVisible = true
                    givGif.isVisible = false
                    ivCommentImage.loadImage(mContext, comment.image_url, R.drawable.square_grey)
                }
                CommentType.GIF_ONLY.value       -> {
                    tvCommentText.isVisible = false
                    ivCommentImage.isVisible = false
                    givGif.isVisible = true
                    givGif.loadGif(mContext, comment.gif_url, R.drawable.square_grey)
                }
                CommentType.TEXT_AND_IMAGE.value -> {
                    tvCommentText.isVisible = true
                    ivCommentImage.isVisible = true
                    givGif.isVisible = false
                    ivCommentImage.loadImage(mContext, comment.image_url, R.drawable.square_grey)
                    tvCommentText.text = comment.text
                }
                CommentType.TEXT_AND_GIF.value   -> {
                    tvCommentText.isVisible = true
                    ivCommentImage.isVisible = false
                    givGif.isVisible = true
                    givGif.loadGif(mContext, comment.gif_url, R.drawable.square_grey)
                    tvCommentText.text = comment.text
                }
            }
        }
    }

    private fun setCount(textView: TextView, count: Int, pluralId: Int, singularId: Int) {
        if (count > 0) {
            textView.text = textView.context.resources.getQuantityString(pluralId, count, count)
        } else {
            textView.text = textView.context.resources.getString(singularId)
        }

    }

    class ViewHolder(var binding: LayoutItemCharacterCommentsBinding, var retryCount: Int = 0) :
        RecyclerView.ViewHolder(binding.root)


}