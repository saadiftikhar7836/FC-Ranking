/*
 *
 * Created by Saad Iftikhar on 9/10/21, 5:11 PM
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.fcranking.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fcranking.android.model.Characters
import com.fcranking.android.repository.SearchCharactersRepository


class SearchCharactersViewModel : ViewModel() {
    private val searchCharactersRepository: SearchCharactersRepository =
        SearchCharactersRepository()
    private var charactersListList = mutableListOf<Characters>()

    private val _characters = MutableLiveData<List<Characters>>()
    val searchedCharacters: LiveData<List<Characters>> = _characters

    init {
        fetchCharacters()
    }

    fun fetchCharacters() {
        if (searchedCharacters.value == null) {
            searchCharactersRepository.getCharacters {
                charactersListList.addAll(it)
                charactersListList = charactersListList.distinctBy { it.character_id }
                    .toMutableList()
                _characters.postValue(charactersListList)
            }
        }
    }

    fun refresh() {
        charactersListList.clear()
        searchCharactersRepository.refresh()
        fetchCharacters()
    }
}