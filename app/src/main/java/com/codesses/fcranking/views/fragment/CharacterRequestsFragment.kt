package com.codesses.fcranking.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.FragmentCharacterRequestsBinding
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.interfaces.RecyclerViewItemClick
import com.codesses.fcranking.model.Characters
import com.codesses.fcranking.enums.EnumIntents
import com.codesses.fcranking.utils.preventDoubleClick
import com.codesses.fcranking.utils.showSnackBar
import com.codesses.fcranking.views.adapter.CharacterRequestsAdapter
import com.codesses.fcranking.views.dialog.ProgressDialog

class CharacterRequestsFragment : Fragment(), RecyclerViewItemClick {

    //    binding
    lateinit var binding: FragmentCharacterRequestsBinding

    //    context
    lateinit var mContext: FragmentActivity

    //    progressDialog
    lateinit var progressDialog: ProgressDialog

    //    array list
    lateinit var charactersList: ArrayList<Characters>

    private val characterRequestsAdapter by lazy { CharacterRequestsAdapter(mContext, this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = requireActivity()
        progressDialog = ProgressDialog(mContext)
        charactersList = ArrayList()

    }

    /*********************************************************************************************************************************************************
     *                                                                       Override methods
     ********************************************************************************************************************************************************/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentCharacterRequestsBinding.bind(
            inflater.inflate(
                R.layout.fragment_character_requests,
                container,
                false
            )
        )

        binding.btnBack.setOnClickListener {
            it.preventDoubleClick()
            mContext.onBackPressed()
        }

        progressDialog.show()
//        //  get character requests
        setAdapter()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        //  get character requests
        getCharacterRequests()
    }

    private fun setAdapter() {
        binding.rvCharacterRequests.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = characterRequestsAdapter
        }
    }

    override fun onItemClick(position: Int) {
        val navDirections =
            CharacterRequestsFragmentDirections.actionCharacterRequestsFragmentToProposeCharacterFragment().setCharacterRequest(charactersList[position])
        binding.root.findNavController().navigate(navDirections)
    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private fun getCharacterRequests() {
        charactersList.clear()
        FirestoreRef.getCharacterRequestsRef()
            .whereEqualTo("status", EnumIntents.PENDING.value)
            .get()
            .addOnCompleteListener { querySnapshot ->
                if (querySnapshot.result?.size()!! > 0) {
                    Log.e("Size", querySnapshot.result?.size().toString())
                    binding.conNoDataFound.visibility = View.GONE
                    binding.rvCharacterRequests.visibility = View.VISIBLE
                    querySnapshot.result?.map {
                        val characterRequest = it.toObject(Characters::class.java)
                        characterRequest.character_id = it.id
                        charactersList.add(characterRequest)
                    }
                    charactersList = ArrayList(charactersList.sortedByDescending { it.time_stamp })
                    Log.e("Size", charactersList.size.toString())
                    characterRequestsAdapter.submitList(charactersList)
                    characterRequestsAdapter.notifyDataSetChanged()
                } else {
                    binding.conNoDataFound.visibility = View.VISIBLE
                    binding.rvCharacterRequests.visibility = View.GONE
                }
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                showSnackBar(binding.root, mContext.getString(R.string.something_went_wrong))
            }
    }
}