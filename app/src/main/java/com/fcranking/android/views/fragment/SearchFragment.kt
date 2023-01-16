package com.fcranking.android.views.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcranking.android.R
import com.fcranking.android.databinding.FragmentSearchBinding
import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.interfaces.OnCharacterClick
import com.fcranking.android.model.Characters
import com.fcranking.android.utils.toCharacters
import com.fcranking.android.utils.visible
import com.fcranking.android.viewmodel.SearchCharactersViewModel
import com.fcranking.android.views.adapter.SearchedCharactersAdapter


class SearchFragment : Fragment(), OnCharacterClick {

    // Context
    private lateinit var mContext: FragmentActivity

    // Data binding
    private lateinit var binding: FragmentSearchBinding

    private lateinit var linearLayoutManager: LinearLayoutManager

    // Adapter
    private val searchedCharactersAdapter by lazy { SearchedCharactersAdapter(mContext, this) }
    private val defaultCharactersList by lazy { ArrayList<Characters>() }

    // View model
    private val viewModel: SearchCharactersViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recyclerview
        linearLayoutManager = LinearLayoutManager(mContext)
        binding.rvCharacters.layoutManager = linearLayoutManager
        binding.rvCharacters.adapter = searchedCharactersAdapter


        // Fetch characters
        viewModel.fetchCharacters()

        // observer
        observer()

        // text change listener
        binding.etSearch.addTextChangedListener {
            if (!TextUtils.isEmpty(it.toString())) {
                searchCharacter(it.toString().lowercase())
            } else {
                handleDataLayout(true)
                updateAdapterList(defaultCharactersList)
            }
        }

    }


    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/


    override fun onClick(character: Characters) {
        findNavController().navigate(SearchFragmentDirections.actionNvSearchToCharacterProfileFragment(character))
    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private fun observer() {
        viewModel.searchedCharacters.observe(viewLifecycleOwner, Observer {
            binding.progressBar.visible(false)

            if (it.isNotEmpty()) {
                handleDataLayout(true)
                defaultCharactersList.clear()
                defaultCharactersList.addAll(it)
                updateAdapterList(defaultCharactersList)
            } else {
                handleDataLayout(false)
            }


        })

    }

    private fun searchCharacter(value: String) {

        FirestoreRef.getCharacterRef()
            .orderBy("character_name")
            .startAt(value)
            .endAt("${value}~")
            .addSnapshotListener { snapshot, error ->
                snapshot?.let {
                    if (it.count() > 0) {
                        val list = ArrayList<Characters>()
                        snapshot.map {
                            val character = it.toCharacters()
                            list.add(character ?: Characters())
                        }
                        Log.d("Searched_Character", "lowercase: ${snapshot.count()} $value $list")

                        updateAdapterList(list)
                        handleDataLayout(true)
                    } else {
                        handleDataLayout(false)
                        updateAdapterList(ArrayList())

                    }
                }
            }

    }

    private fun updateAdapterList(list: ArrayList<Characters>) {
        searchedCharactersAdapter.submitList(list)
        searchedCharactersAdapter.notifyDataSetChanged()
    }

    private fun handleDataLayout(value: Boolean) {

        binding.rvCharacters.visible(value)
        binding.tvNoCharacter.visible(!value)

    }

}