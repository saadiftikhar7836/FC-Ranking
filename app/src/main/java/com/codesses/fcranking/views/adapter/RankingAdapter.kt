package com.codesses.fcranking.views.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.LayoutItemCharacterRankingBinding
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.model.Characters
import com.codesses.fcranking.utils.*

class RankingAdapter(val mContext: Context, var isFirst: Boolean, private val onClick: (Characters) -> Unit) : ListAdapter<Characters, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Characters>() {
        override fun areItemsTheSame(oldItem: Characters, newItem: Characters): Boolean =
            oldItem.character_id == newItem.character_id

        override fun areContentsTheSame(oldItem: Characters, newItem: Characters): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            LayoutItemCharacterRankingBinding.bind(getView(R.layout.layout_item_character_ranking, mContext, parent))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val character = currentList[position]
        viewHolder.binding.apply {

            if (position == currentList.size - 1)
                viewSeparator.visible(false)
            imgCharacterImage.loadImage(mContext, character.character_image, R.drawable.circle_grey)
            tvCharacterName.text = character.character_name.capitalizeWords
            tvShowName.text = character.show_name.capitalizeWords
            tvCharacterRank.text = "${position + 1}"
            val value = character.last_rank - (position + 1)

            when {
                value < 0 -> {
                    tvRankVariation.visible(true)
                    ivRankVariation.setImageResource(R.drawable.ic_arrow_red)
                    tvRankVariation.changeTextColor(R.color.Red)
                    ivRankVariation.rotation = 270f
                    tvRankVariation.text = "$value"
                    tvRankVariation.visibility = View.VISIBLE
                }
                value > 0 -> {
                    tvRankVariation.visible(true)
                    ivRankVariation.setImageResource(R.drawable.ic_arrow_green)
                    ivRankVariation.rotation = 90f
                    tvRankVariation.changeTextColor(R.color.color_green)
                    tvRankVariation.text = "+$value"
                    tvRankVariation.visibility = View.VISIBLE
                }
                else      -> {
                    if (isFirst) {
                        ivRankVariation.isVisible = false
                        tvRankVariation.isVisible = false
                    } else {
                        ivRankVariation.isVisible = !isFirst
                        tvRankVariation.visibility = View.INVISIBLE
                        ivRankVariation.setImageResource(R.drawable.ic_equal)
                        ivRankVariation.rotation = 90f
                    }
                }
            }
            if (value.toInt() != 0) {
                updateCharacterRank(character, position + 1)
            }
        }

        viewHolder.itemView.setOnClickListener {
            onClick(character)
        }
    }

    private fun updateCharacterRank(characters: Characters, rank: Int) {
        val map: MutableMap<String, Any> = HashMap()
        if (rank > characters.worst_rank) {
            map["worst_rank"] = rank
        } else if (rank < characters.best_rank) {
            map["best_rank"] = rank
        }
        map["last_rank"] = rank
        FirestoreRef.getCharacterRef()
            .document(characters.character_id)
            .update(map)
    }

    class ViewHolder(var binding: LayoutItemCharacterRankingBinding) :
        RecyclerView.ViewHolder(binding.root)
}