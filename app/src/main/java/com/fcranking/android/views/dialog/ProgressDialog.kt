package com.fcranking.android.views.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.fcranking.android.R


class ProgressDialog(private val mContext: Context) {

    private lateinit var progressDialog: Dialog


    // Show progress dialog
    fun show() {
        progressDialog = Dialog(mContext)

        progressDialog.setContentView(R.layout.custom_dialog_progress)


        /* Custom setting to change TextView text, Color and Text Size according to your Preference*/
        val progressTv = progressDialog.findViewById<TextView>(R.id.progress_tv)
//        progressTv.text = mContext.getString(text)
        progressTv.setTextColor(ContextCompat.getColor(mContext, R.color.white))
        progressTv.textSize = 16f

        if (progressDialog.window != null) progressDialog.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        progressDialog.setCancelable(false)
        if (this::progressDialog.isInitialized && !progressDialog.isShowing)
            progressDialog.show()

    }

    // Dismiss dialog
    fun dismiss() {
        if (this::progressDialog.isInitialized && progressDialog.isShowing)
            progressDialog.dismiss()
    }

}