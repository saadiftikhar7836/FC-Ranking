package com.codesses.fcranking.views.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.BottomSheetChooserBinding
import com.codesses.fcranking.utils.preventDoubleClick
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PhotoBottomSheet : BottomSheetDialogFragment() {

    //    context
    lateinit var mContext: FragmentActivity

    //    variable
    lateinit var type: String
    lateinit var url: String

    //    binding
    lateinit var binding: BottomSheetChooserBinding

    lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        type = arguments?.get("type")
            ?.toString() ?: "photo"
        url = arguments?.get("url")
            ?.toString() ?: "photo"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            BottomSheetChooserBinding.bind(inflater.inflate(R.layout.bottom_sheet_chooser, null))
        setData()

        return binding.root
    }

    private fun setData() {
        binding.tvUploadPhoto.text = "Upload profile $type"
        binding.tvViewPhoto.text = "View profile $type"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.conViewPhoto.setOnClickListener {
            it.preventDoubleClick()
            navigationController = findNavController()
            val bundle = Bundle()
            bundle.putString("url", url)
            setFragmentResult("url", bundle)

        }
        binding.conUploadPhoto.setOnClickListener {
            it.preventDoubleClick()
            navigationController = findNavController()
            val bundle = Bundle()
            bundle.putString("url", "pick")
            setFragmentResult("url", bundle)
        }
    }
}