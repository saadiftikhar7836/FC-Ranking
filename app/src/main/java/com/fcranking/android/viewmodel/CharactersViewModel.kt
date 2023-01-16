package com.fcranking.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fcranking.android.model.Characters
import com.fcranking.android.repository.CharacterRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CharactersViewModel(application: Application) : AndroidViewModel(application) {

    val charactersRepository: CharacterRepository = CharacterRepository()
    var myRequestsList = mutableListOf<Characters>()
    var isNextRequestExists = false
    private val _characters = MutableLiveData<MutableList<Characters>>()
    private val _myCharacterRequest = MutableLiveData<MutableList<Characters>>()
    private val _isNextRequest = MutableLiveData<Boolean>()
    val characters: LiveData<MutableList<Characters>> = _characters
    val myCharacterRequests: LiveData<MutableList<Characters>> = _myCharacterRequest
    val isNextRequest: LiveData<Boolean> = _isNextRequest



    fun getCharacters() {
        Log.e("getCharacters Called", "In View Model")
        viewModelScope.launch {
            charactersRepository.getCharacters()
                .collect {
                    _characters.value = it
                }
        }
    }

    fun fetchMyCharacterRequests() {
        viewModelScope.launch {
            charactersRepository.getMyRequestedCharacters().collect {
                myRequestsList.addAll(it.first)
                _myCharacterRequest.value = myRequestsList
                isNextRequestExists = it.second
                _isNextRequest.value = isNextRequestExists
            }
        }
    }
}