package com.fcranking.android.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fcranking.android.R
import com.fcranking.android.databinding.CharacterRequestItemLayoutBinding
import com.fcranking.android.interfaces.RecyclerViewItemClick
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.loadImage
import com.fcranking.android.utils.preventDoubleClick

class CharacterRequestsAdapter(val mContext: Context, var onRecyclerViewItemClick: RecyclerViewItemClick) :
    ListAdapter<Characters, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<Characters>() {
            override fun areItemsTheSame(oldItem: Characters, newItem: Characters): Boolean =
                oldItem.character_id == newItem.character_id

            override fun areContentsTheSame(oldItem: Characters, newItem: Characters): Boolean =
                oldItem == newItem

        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CharacterRequestItemLayoutBinding.bind(
            LayoutInflater.from(mContext)
                .inflate(R.layout.character_request_item_layout, parent, false)
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.imgCharacterImage.loadImage(
            mContext,
            currentList[position].character_image,
            R.drawable.circle_grey
        )

        viewHolder.binding.tvCharacterName.text = currentList[position].character_name
        viewHolder.binding.tvShowName.text = currentList[position].show_name
        viewHolder.binding.tvDate.text = currentList[position].date
        viewHolder.binding.tvTime.text = currentList[position].time

        viewHolder.binding.tvViewRequest.setOnClickListener {
            it.preventDoubleClick()
            onRecyclerViewItemClick.onItemClick(position)
        }
    }

    class ViewHolder(var binding: CharacterRequestItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}