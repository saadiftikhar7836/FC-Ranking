/*
 *
 * Created by Saad Iftikhar on 10/11/21, 5:20 PM
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.codesses.fcranking.views.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.LayoutItemSearchCharacterBinding
import com.codesses.fcranking.interfaces.OnCharacterClick
import com.codesses.fcranking.model.Characters
import com.codesses.fcranking.utils.capitalizeWords
import com.codesses.fcranking.utils.preventDoubleClick


class SearchedCharactersViewHolder(val binding: LayoutItemSearchCharacterBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(mContext: Context, character: Characters, listener: OnCharacterClick) {

        Glide.with(mContext)
            .load(character.character_image)
            .placeholder(R.drawable.circle_grey)
            .into(binding.civCharacterProfile)

        binding.tvCharacterName.text = character.character_name.capitalizeWords
        binding.tvShowName.text = character.show_name
        binding.tvBestRanking.text = "BR: ${character.best_rank}"
        binding.tvWorstRanking.text = "WR: ${character.worst_rank}"
        binding.tvRanking.text = "R: ${character.last_rank}"


        // Click listener
        itemView.setOnClickListener {
            it.preventDoubleClick()
            listener.onClick(character)
        }

    }

}