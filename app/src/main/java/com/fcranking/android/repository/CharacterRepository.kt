package com.fcranking.android.repository

import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.AppConstants
import com.fcranking.android.utils.toCharacters
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CharacterRepository {
    var next: Query? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getCharacters(): Flow<MutableList<Characters>> {

        val db = FirestoreRef.getCharacterRef()

        return callbackFlow {
            db.orderBy("votes_count", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val map: MutableList<Characters> = querySnapshot.map {
                        it.toCharacters()!!
                    }.toMutableList()
                    trySend(map)
                }
                .addOnFailureListener {
                    cancel(cause = it, message = "Something wrong on server side")
                }
            awaitClose { cancel() }
        }
    }

    suspend fun getMyRequestedCharacters(): Flow<Pair<MutableList<Characters>, Boolean>> {
        var nextQuery = next
        return if (nextQuery != null) {
            getMyCharacterRequests(nextQuery)
        } else {
            nextQuery = FirestoreRef.getCharacterRequestsRef()
                .whereEqualTo("proposed_by", FirestoreRef.getUserId())
                .limit(AppConstants.DEFAULT_MY_CHARACTERS_REQUEST_COUNT)
            getMyCharacterRequests(nextQuery)
        }
    }

    suspend fun getMyCharacterRequests(query: Query): Flow<Pair<MutableList<Characters>, Boolean>> {
        return callbackFlow {
            query.get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.count() > 0) {
                    val map = querySnapshot.map {
                        val character = it.toCharacters()
                        character
                    } as MutableList<Characters>

                    launch {
                        updateQuery(querySnapshot, map = map)
                            .collect {
                                trySend(it)
                            }
                    }
                } else {
                    val map = querySnapshot.map {
                        val character = it.toCharacters()
                        character
                    } as MutableList<Characters>

                    trySend(Pair(map, false))
                }
            }.addOnFailureListener {
                cancel(cause = it, message = "Something wrong on server side")
            }
            awaitClose { cancel() }
        }
    }

    private suspend fun updateQuery(recordSnapshots: QuerySnapshot, map: MutableList<Characters>): Flow<Pair<MutableList<Characters>, Boolean>> {
        val lastVisible = recordSnapshots.documents[recordSnapshots.size() - 1]

        return callbackFlow {
            next = FirestoreRef.getCharacterRequestsRef()
                .whereEqualTo("proposed_by", FirestoreRef.getUserId())
                .limit(AppConstants.DEFAULT_MY_CHARACTERS_REQUEST_COUNT)
                .startAfter(lastVisible)
            next?.get()
                ?.addOnSuccessListener {
                    trySend(Pair(map, it.count() > 0))
                }?.addOnFailureListener {
                    trySend(Pair(map, false))
                }
            awaitClose { cancel() }
        }


    }


}


