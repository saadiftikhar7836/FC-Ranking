package com.fcranking.android.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.fcranking.android.R
import com.fcranking.android.databinding.FragmentFullScreenImageBinding
import com.fcranking.android.utils.getView
import com.fcranking.android.utils.loadImage
import com.fcranking.android.utils.preventDoubleClick

class FullScreenImageFragment : Fragment() {

    //    binding
    lateinit var binding: FragmentFullScreenImageBinding

    //    context
    lateinit var mContext: FragmentActivity

    //    variable
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        url = arguments?.get("url")
            ?.toString()
            .toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFullScreenImageBinding.bind(
            getView(R.layout.fragment_full_screen_image, mContext, container!!)
        )

        //        click listeners
        binding.btnBack.setOnClickListener {
            it.preventDoubleClick()
            findNavController().navigateUp()
        }

        binding.image.loadImage(mContext, url, R.drawable.circle_grey)
        return binding.root
    }
}