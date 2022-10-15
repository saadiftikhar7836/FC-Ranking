/*
 *
 * Created by Saad Iftikhar
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.codesses.fcranking.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirestoreRef {

    /*********************************************************************************************************************************************************
     *                                                                Firebase authentication
     *********************************************************************************************************************************************************/

    //   Firestore auth
    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }


    //   Current firebase user
    fun getCurrentUser(): FirebaseUser? {
        return getAuth().currentUser
    }


    //   Current user id
    fun getUserId(): String? {
        return getAuth().currentUser?.uid
    }

    //   Current User Email
    fun getUserEmail(): String? {
        return getAuth().currentUser?.email
    }


    /*********************************************************************************************************************************************************
     *                                                                Firestore Database
     *********************************************************************************************************************************************************/

    //   Database instance
    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    //    User reference
    fun getUserRef(): CollectionReference {
        return getInstance().collection("users")
    }

    //    User reference
    fun getCharacterRequestsRef(): CollectionReference {
        return getInstance().collection("character_requests")
    }

    //    Character reference
    fun getCharacterRef(): CollectionReference {
        return getInstance().collection("characters")
    }

    fun getVotesRef(characterId: String): CollectionReference {
        return getCharacterRef().document(characterId)
            .collection("votes")
    }

    fun getFollowersRef(userId: String): CollectionReference {
        return getInstance().collection("followers_following")
            .document("followers")
            .collection(userId)
    }

    fun getFollowingRef(userId: String): CollectionReference {
        return getInstance().collection("followers_following")
            .document("following")
            .collection(userId)
    }

    fun getFavouriteRef(): CollectionReference {
        return getInstance().collection("favourite_characters")
    }

    fun getRecentVotesRef(): CollectionReference {
        return getInstance().collection("recent_votes")
    }

    fun getCharacterCommentsRef(characterId: String): CollectionReference {
        return getInstance().collection("comments")
            .document("character_comments")
            .collection(characterId)
//            .collection()
    }

    fun getCommentsReplyRef(characterId: String, commentId: String): CollectionReference {
        return getCharacterCommentsRef(characterId)
            .document(commentId).collection("replies")
//            .collection()
    }

    fun getCommentLikesRef(characterId: String, commentId: String): CollectionReference {
        return getCharacterCommentsRef(characterId).document(commentId)
            .collection("likes")
    }

    fun getCommentReplyLikesRef(characterId: String, commentId: String, replyId: String): CollectionReference {
        return getCommentsReplyRef(characterId, commentId)
            .document(replyId).collection("likes")
    }

    /*********************************************************************************************************************************************************
     *                                                                Firestore storage
     *********************************************************************************************************************************************************/


    //    Get storage instance
    private fun getStorageInstance(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }

    //        User profile image reference
    fun getProfileStorage(): StorageReference {
        return getStorageInstance().child("users/profile_images")
    }

    //        User profile cover reference
    fun getProfileCoverStorage(): StorageReference {
        return getStorageInstance().child("users/profile_cover")
    }

    //        character image reference
    fun getCharacterStorage(): StorageReference {
        return getStorageInstance().child("character/character_images")
    }

    fun getCommentsStorage(): StorageReference {
        return getStorageInstance().child("comments/comment_images")
    }

}
