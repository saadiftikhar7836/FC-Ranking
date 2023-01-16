/*
 *
 * Created by Saad Iftikhar on 10/11/21, 5:19 PM
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.fcranking.android.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.fcranking.android.R
import com.fcranking.android.databinding.LayoutItemSearchCharacterBinding
import com.fcranking.android.interfaces.OnCharacterClick
import com.fcranking.android.model.Characters
import com.fcranking.android.views.viewholder.SearchedCharactersViewHolder

class SearchedCharactersAdapter(val mContext: Context, private val listener: OnCharacterClick) :
    ListAdapter<Characters, SearchedCharactersViewHolder>(
        object : DiffUtil.ItemCallback<Characters>() {
            override fun areItemsTheSame(old: Characters, aNew: Characters): Boolean =
                old.character_id == aNew.character_id

            override fun areContentsTheSame(old: Characters, aNew: Characters): Boolean =
                old == aNew
        }) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedCharactersViewHolder {
        val binding: LayoutItemSearchCharacterBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.layout_item_search_character, parent, false
        )
        return SearchedCharactersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchedCharactersViewHolder, position: Int) {
        val userEvent = getItem(position)
        holder.bind(mContext, userEvent, listener)
    }


}