package com.codesses.fcranking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.codesses.fcranking.model.Comments
import com.codesses.fcranking.repository.CommentsRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommentsViewModel(application: Application) : AndroidViewModel(application) {
    private var commentsRepository = CommentsRepository()
    private var commentsList = mutableListOf<Comments>()
    private var repliesList = mutableListOf<Comments>()

    private val _comments = MutableLiveData<List<Comments>>()
    private val _replies = MutableLiveData<List<Comments>>()
    val comments: LiveData<List<Comments>> = _comments
    val replies: LiveData<List<Comments>> = _replies
    private val _isNextCommentExists = MutableLiveData<Boolean?>()
    private val _isNextReplyExists = MutableLiveData<Boolean?>()
    var isNextCommentExists: LiveData<Boolean?> = _isNextCommentExists
    var isNextReplyExists: LiveData<Boolean?> = _isNextReplyExists


    fun fetchComments(characterId: String) {
        viewModelScope.launch {
            commentsRepository.getComments(characterId)
                .collect {
                    commentsList.addAll(it.second)
                    commentsList = commentsList.distinctBy { it1 -> it1.comment_id }
                        .toMutableList()
                    _comments.value = commentsList
                    _isNextCommentExists.value = it.first
                }
        }
    }

    fun fetchReplies(characterId: String, commentId: String) {
        viewModelScope.launch {
            commentsRepository.getReplies(characterId, commentId)
                .collect {
                    repliesList.addAll(it.second)
                    repliesList = repliesList.distinctBy { it1 -> it1.comment_id }
                        .toMutableList()
                    _replies.value = repliesList
                    _isNextReplyExists.value = it.first
                }
        }
    }

    fun refreshComments(characterId: String) {
        commentsList.clear()
        commentsRepository.refreshComments()
        fetchComments(characterId)
    }

    fun refreshReplies(characterId: String, commentId: String) {
        repliesList.clear()
        commentsRepository.refreshReplies()
        fetchReplies(characterId, commentId)
    }
}