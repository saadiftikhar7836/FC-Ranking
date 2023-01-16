/*
 *
 * Created by Saad Iftikhar on 9/10/21, 4:32 PM
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.fcranking.android.repository

import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.AppConstants
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot


class SearchCharactersRepository {
    var next: Query? = null


    fun getCharacters(onSuccess: (List<Characters>) -> Unit) {
        val nextQuery = next

        if (nextQuery != null) {
            getData(nextQuery, onSuccess)
        } else {
            val start = FirestoreRef.getCharacterRef()
                .limit(AppConstants.DEFAULT_CHARACTERS_COUNT)

            getData(start, onSuccess)

        }
    }

    private fun getData(query: Query, onSuccess: (List<Characters>) -> Unit) {
        query.get()
            .addOnSuccessListener { recordSnapshots ->
                if (recordSnapshots.size() > 0) {

                    updateNextQuery(recordSnapshots)

                    onSuccess(recordSnapshots.map {
                        val characters = it.toObject(Characters::class.java)
                        characters.character_id = it.id
                        characters
                    })
                } else {

                    // If there is no data available
                    onSuccess(recordSnapshots.map {

                        val userEvents = it.toObject(Characters::class.java)
                        userEvents.character_id = "null"
                        userEvents

                    })
                }

            }
    }

    private fun updateNextQuery(recordSnapshots: QuerySnapshot) {
        val lastVisible = recordSnapshots.documents[recordSnapshots.size() - 1]

        next = FirestoreRef.getCharacterRef()
            .limit(AppConstants.DEFAULT_CHARACTERS_COUNT)
//            .startAfter(lastVisible)

    }

    fun refresh() {
        next = null
    }

}