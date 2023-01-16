package com.fcranking.android.views.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fcranking.android.R
import com.fcranking.android.databinding.LayoutItemMyRequestBinding
import com.fcranking.android.enums.EnumIntents
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.capitalizeWords
import com.fcranking.android.utils.loadImage


class MyRequestAdapter : ListAdapter<Characters, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Characters>() {
        override fun areItemsTheSame(oldItem: Characters, newItem: Characters): Boolean =
            oldItem.character_id == newItem.character_id

        override fun areContentsTheSame(oldItem: Characters, newItem: Characters): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = LayoutItemMyRequestBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.layout_item_my_request, parent, false))

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val character = currentList[position]
        viewHolder.binding.apply {

            imgCharacterImage.loadImage(this.root.context, character.character_image, R.drawable.square_grey)
            tvShowName.text = character.show_name.capitalizeWords
            tvCharacterName.text = character.character_name.capitalizeWords
            tvStatus.text = character.status.capitalizeWords
            tvDateTime.text = tvDateTime.context.getString(R.string.at_date_time, character.date, character.time)

            when (character.status) {
                EnumIntents.PENDING.value  -> {
                    tvStatus.setTextColor(ContextCompat.getColor(this.root.context, R.color.Orange))
                }
                EnumIntents.ACCEPTED.value -> {
                    tvStatus.setTextColor(ContextCompat.getColor(this.root.context, R.color.colorStatusGreen))
                }
                EnumIntents.REJECTED.value -> {
                    tvStatus.setTextColor(ContextCompat.getColor(this.root.context, R.color.colorRed))
                }
            }
        }
    }


    class ViewHolder(var binding: LayoutItemMyRequestBinding) :
        RecyclerView.ViewHolder(binding.root)
}