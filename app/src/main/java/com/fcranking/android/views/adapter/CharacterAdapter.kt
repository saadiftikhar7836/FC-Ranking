package com.fcranking.android.views.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fcranking.android.R
import com.fcranking.android.databinding.LayoutItemProfileCharactersBinding
import com.fcranking.android.interfaces.OnCharacterClick
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.getView
import com.fcranking.android.utils.loadImage

class CharacterAdapter(val mContext: Context, val listener: OnCharacterClick)
    : ListAdapter<Characters, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Characters>() {
        override fun areItemsTheSame(oldItem: Characters, newItem: Characters): Boolean =
            oldItem.character_id == newItem.character_id

        override fun areContentsTheSame(oldItem: Characters, newItem: Characters): Boolean =
            oldItem == newItem
    }
) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = LayoutItemProfileCharactersBinding.bind(
            getView(R.layout.layout_item_profile_characters, mContext, parent)
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val character = currentList[position]

        // Character image
        viewHolder.binding.ivCharacterImage.loadImage(mContext, character.character_image, R.drawable.circle_grey)

        // Click listener
        viewHolder.binding.ivCharacterImage.setOnClickListener {
            listener.onClick(character)
        }
    }

    class ViewHolder(var binding: LayoutItemProfileCharactersBinding) :
        RecyclerView.ViewHolder(binding.root)
}