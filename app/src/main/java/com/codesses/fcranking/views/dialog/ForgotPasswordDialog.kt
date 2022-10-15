package com.codesses.fcranking.views.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.codesses.fcranking.R
import com.codesses.fcranking.interfaces.OnForgotPassClick
import com.codesses.fcranking.utils.UiHelper
import com.codesses.fcranking.utils.getDrawable
import com.codesses.fcranking.utils.preventDoubleClick
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordDialog : DialogFragment() {

    //     Interface
    private var onForgotPassClick: OnForgotPassClick? = null

    lateinit var etEmail: TextInputLayout

    lateinit var etEmailField: EditText

    lateinit var btnSendEmail: TextView

    lateinit var btnCancel: TextView


    //    Variables
    private var isEmail = false

    lateinit var mContext: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(mContext)
        val inflater: LayoutInflater = mContext.layoutInflater
        val view: View = inflater.inflate(R.layout.forgot_password_dialog, null)

        etEmail = view.findViewById(R.id.etEmail)
        etEmailField = view.findViewById(R.id.etEmailField)
        btnSendEmail = view.findViewById(R.id.btnSendEmail)
        btnCancel = view.findViewById(R.id.btnCancel)

        builder.setView(view)

//        Text changed listener
        textChangedListener()

//        Click listeners
        btnSendEmail.setOnClickListener {
            it.preventDoubleClick()
            sendEmail()
        }
        btnCancel.setOnClickListener {
            it.preventDoubleClick()
            dismiss()
        }
        return builder.create()
    }

    /*********************************************************************************************************************************************************
     *                                                                       override methods
     ********************************************************************************************************************************************************/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onForgotPassClick = context as OnForgotPassClick
        } catch (e: java.lang.ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "Must Implement ForgotDialogListener"
            )
        }
    }

    override fun onDetach() {
        onForgotPassClick = null
        super.onDetach()
    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private fun sendEmail() {
        val email = etEmailField.text.toString().trim { it <= ' ' }
        onForgotPassClick!!.onApply(email)
        dismiss()
    }

    private fun textChangedListener() {
        etEmailField.addTextChangedListener {
            validateEmail(it)
        }
    }

    private fun validateEmail(s: Editable?) {
        if (!TextUtils.isEmpty(s)) {
            if (UiHelper.isValidEmail(s.toString())) {
                isEmail = true
                etEmail.isErrorEnabled = false
            } else {
                isEmail = false
                etEmail.isErrorEnabled = true
                etEmail.error = resources.getString(R.string.invalid_email_format)
            }
            etEmail.isEndIconVisible = true
        } else {
            isEmail = false
            etEmail.isErrorEnabled = false
            etEmail.isEndIconVisible = false
        }

        updateButtonState()
    }

    private fun updateButtonState() {
        btnSendEmail.apply {
            isEnabled = isEmail
            background = getDrawable(activity as Context, isEnabled)
        }

    }


}