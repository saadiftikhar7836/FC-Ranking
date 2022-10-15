package com.codesses.fcranking.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.FragmentMyRequestsBinding
import com.codesses.fcranking.model.Characters
import com.codesses.fcranking.utils.visible
import com.codesses.fcranking.viewmodel.CharactersViewModel
import com.codesses.fcranking.views.adapter.MyRequestAdapter
import com.codesses.fcranking.views.dialog.ProgressDialog

class MyRequestsFragment : Fragment() {

    //    context
    lateinit var mContext: FragmentActivity

    //    binding
    lateinit var binding: FragmentMyRequestsBinding

    //    adapter
    val myRequestsAdapter: MyRequestAdapter by lazy { MyRequestAdapter() }

    var myRequestList: MutableList<Characters> = ArrayList()

    val charactersViewModel: CharactersViewModel by viewModels()

    var isLoading = false
    var isNextItemExists = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentMyRequestsBinding.bind(inflater.inflate(R.layout.fragment_my_requests, container, false))
        binding.rvMyRequests.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = myRequestsAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!isLoading) {

                        val linearLayoutManager = layoutManager as LinearLayoutManager
                        val lastVisibleItemPosition: Int =
                            linearLayoutManager.findLastCompletelyVisibleItemPosition()
                        val totalItemCount: Int = linearLayoutManager.itemCount

                        if (totalItemCount > 0) {
                            if (lastVisibleItemPosition == totalItemCount - 1) {
                                if (isNextItemExists) {
                                    binding.progressBar.isVisible = true
                                    charactersViewModel.fetchMyCharacterRequests()
                                    isLoading = true
                                }
                            }
                        }
                    }
                }
            })
        }

        binding.apply {
            btnBack.setOnClickListener {
                mContext.onBackPressed()
            }
        }
        charactersViewModel.fetchMyCharacterRequests()
        observer()
        return binding.root
    }

    private fun observer() {
        charactersViewModel.myCharacterRequests.observe(viewLifecycleOwner) {
            binding.progressBar.visible(false)
            isLoading = false
            binding.progressBar.isVisible = false
            myRequestList.clear()
            myRequestList.addAll(it)
            myRequestList = myRequestList.sortedByDescending { item ->
                item.time_stamp
            }.toMutableList()
            myRequestsAdapter.submitList(myRequestList)
            myRequestsAdapter.notifyDataSetChanged()
        }
        charactersViewModel.isNextRequest.observe(viewLifecycleOwner) {
            isNextItemExists = it ?: false
        }
    }


}