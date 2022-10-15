package com.codesses.fcranking.repository

import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.model.Comments
import com.codesses.fcranking.model.User
import com.codesses.fcranking.utils.AppConstants
import com.codesses.fcranking.utils.toComments
import com.codesses.fcranking.utils.toUser
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommentsRepository {
    var nextCommentQuery: Query? = null
    var nextReplyQuery: Query? = null

    /*********************************************************************************************************************************************************
     *                                                                       Comments functions
     ********************************************************************************************************************************************************/

    suspend fun getComments(characterId: String): Flow<Pair<Boolean, MutableList<Comments>>> {
        val nextQuery = nextCommentQuery
        return if (nextQuery != null) {
            getCommentsData(characterId, nextQuery)
        } else {
            val start = FirestoreRef.getCharacterCommentsRef(characterId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(AppConstants.DEFAULT_COMMENTS_COUNT)
            getCommentsData(characterId, start)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getCommentsData(characterId: String, query: Query): Flow<Pair<Boolean, MutableList<Comments>>> {

        return callbackFlow {
            query
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.size() > 0) {
                        val map: MutableList<Comments> = querySnapshot.map {
                            val comment = it.toComments() ?: Comments()

                            //  launch for collecting user data
                            launch {
                                getUserData(comment.commented_by).collect { user ->
                                    comment.user = user
                                }
                            }
                            launch {
                                getRepliesCount(comment.comment_id, characterId).collect { repliesCount ->
                                    comment.repliesCount = repliesCount
                                }
                            }
                            //  launch for collecting isRepliesExists
                            launch {
                                checkIsLiked(comment.comment_id, characterId).collect { isLiked ->
                                    comment.isLiked = isLiked
                                }
                            }
                            comment
                        }
                                as MutableList<Comments>

                        launch {
                            updateCommentQuery(querySnapshot, characterId, map)
                                .collect {
                                    trySend(it)
                                }
                        }
                    } else {
                        val map = querySnapshot.map {
                            val comment = it.toComments()
                            comment
                        } as MutableList<Comments>

                        trySend(Pair(false, map))
                    }
                }
                .addOnFailureListener {
                    cancel(cause = it, message = "Something wrong on server side")
                }
            awaitClose { cancel() }
        }
    }

    private fun getRepliesCount(commentId: String, characterId: String): Flow<Int> {
        return callbackFlow {
            FirestoreRef.getCommentsReplyRef(characterId, commentId)
                .get()
                .addOnSuccessListener {
                    trySend(it.count())
                }.addOnFailureListener {
                    trySend(0)
                }
            awaitClose { cancel() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun checkIsLiked(commentId: String, characterId: String): Flow<Boolean> {
        return callbackFlow {
            FirestoreRef.getCommentLikesRef(characterId, commentId)
                .orderBy(FirestoreRef.getUserId().toString())
                .get()
                .addOnSuccessListener {
                    it.map { querySnapshot ->
                        trySend(querySnapshot.get(FirestoreRef.getUserId().toString()) as Boolean)
                    }
                }.addOnFailureListener {
                    trySend(false)
                }
            awaitClose { cancel() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun updateCommentQuery(recordSnapshots: QuerySnapshot, characterId: String, map: MutableList<Comments>): Flow<Pair<Boolean, MutableList<Comments>>> {
        val lastVisible = recordSnapshots.documents[recordSnapshots.size() - 1]

        return callbackFlow {
            nextCommentQuery = FirestoreRef.getCharacterCommentsRef(characterId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(AppConstants.DEFAULT_COMMENTS_COUNT)
                .startAfter(lastVisible)
            nextCommentQuery?.get()
                ?.addOnSuccessListener {
                    trySend(Pair(it.count() > 0, map))
                }
            awaitClose { cancel() }

        }


    }

    fun refreshComments() {
        nextCommentQuery = null
    }

    /*********************************************************************************************************************************************************
     *                                                                       Replies functions
     ********************************************************************************************************************************************************/


    suspend fun getReplies(characterId: String, commentId: String): Flow<Pair<Boolean, MutableList<Comments>>> {
        val nextQuery = nextReplyQuery
        return if (nextQuery != null) {
            getRepliesData(characterId, nextQuery, commentId)
        } else {
            val start = FirestoreRef.getCommentsReplyRef(characterId, commentId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(AppConstants.DEFAULT_COMMENTS_COUNT)
            getRepliesData(characterId, start, commentId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getRepliesData(characterId: String, query: Query, commentId: String): Flow<Pair<Boolean, MutableList<Comments>>> {
        return callbackFlow {
            query
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.size() > 0) {
                        val map: MutableList<Comments> = querySnapshot.map {
                            val comments = it.toComments() ?: Comments()
                            launch {
                                getUserData(comments.replied_by).collect { user ->
                                    comments.user = user
                                }
                            }
                            launch {
                                checkIsReplyLiked(characterId, commentId, comments.comment_id).collect { isLiked ->
                                    comments.isLiked = isLiked
                                }
                            }
                            comments
                        }
                            .toMutableList()

                        launch {
                            updateRepliesQuery(querySnapshot, characterId, commentId, map)
                                .collect {
                                    trySend(it)
                                }
                        }
                    } else {
                        val map = querySnapshot.map {
                            val comments = it.toComments()
                            comments
                        } as MutableList<Comments>

                        trySend(Pair(false, map))
                    }
                }
                .addOnFailureListener {
                    cancel(cause = it, message = "Something wrong on server side")
                }
            awaitClose { cancel() }
        }
    }

    private fun checkIsReplyLiked(characterId: String, commentId: String, replyId: String): Flow<Boolean> {
        return callbackFlow {
            FirestoreRef.getCommentReplyLikesRef(characterId, commentId, replyId)
                .orderBy(FirestoreRef.getUserId().toString())
                .get()
                .addOnSuccessListener {
                    it.map { querySnapshot ->
                        trySend(querySnapshot.get(FirestoreRef.getUserId().toString()) as Boolean)
                    }
                }.addOnFailureListener {
                    trySend(false)
                }
            awaitClose { cancel() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun updateRepliesQuery(recordSnapshots: QuerySnapshot, characterId: String, commentId: String, map: MutableList<Comments>): Flow<Pair<Boolean, MutableList<Comments>>> {
        val lastVisible = recordSnapshots.documents[recordSnapshots.size() - 1]

        return callbackFlow {
            nextReplyQuery = FirestoreRef.getCommentsReplyRef(characterId, commentId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(AppConstants.DEFAULT_COMMENTS_COUNT)
                .startAfter(lastVisible)
            nextReplyQuery?.get()
                ?.addOnSuccessListener {
                    trySend(Pair(it.count() > 0, map))
                }?.addOnFailureListener {
                    trySend(Pair(false, map))
                }
            awaitClose { cancel() }

        }


    }

    fun refreshReplies() {
        nextReplyQuery = null
    }

    /*********************************************************************************************************************************************************
     *                                                                       Common functions
     ********************************************************************************************************************************************************/

    //    get data of the user that has commented on the character or reply on comment
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getUserData(userId: String?): Flow<User> {
        return callbackFlow {
            FirestoreRef.getUserRef().document(userId.toString())
                .get()
                .addOnSuccessListener {
                    it.toUser()?.let { it1 -> trySend(it1) }
                }
                .addOnFailureListener {
                    trySend(User())
                }
            awaitClose { cancel() }
        }
    }


}