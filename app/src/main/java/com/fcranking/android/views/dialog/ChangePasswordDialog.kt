package com.fcranking.android.views.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.fcranking.android.R
import com.fcranking.android.interfaces.OnChangePassClick
import com.fcranking.android.utils.preventDoubleClick

class ChangePasswordDialog : DialogFragment() {
    //  Widgets
    var oldPass: EditText? = null

    var oldPassHide: ImageView? = null

    var newPass: EditText? = null

    var newPassHide: ImageView? = null

    private var updatePass: Button? = null

    //    TODO: Interface
    var onChangePassClick: OnChangePassClick? = null

    lateinit var mContext: FragmentActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onChangePassClick = context as OnChangePassClick
        } catch (e: java.lang.ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "Must Implement ForgotDialogListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(mContext)
        val view: View = mContext.layoutInflater.inflate(R.layout.chagne_password_dialog, null)
        alertDialog.setView(view)
            .setTitle(getString(R.string.change_pass))

//        Assigning id's
        oldPassHide = view.findViewById(R.id.old_pass_hide)
        oldPass = view.findViewById(R.id.old_pass)
        newPassHide = view.findViewById(R.id.new_pass_hide)
        newPass = view.findViewById(R.id.new_pass)
        updatePass = view.findViewById(R.id.update_pass)


//        TODO: Click Listeners
        oldPassHide!!.setOnClickListener {
            it.preventDoubleClick()
            oldPass?.let { it1 ->
                hideOrShowPassword(it as ImageView, it1, mContext)
            }
        }
        newPassHide!!.setOnClickListener {
            it.preventDoubleClick()
            newPass?.let { it1 ->
                hideOrShowPassword(it as ImageView, it1, mContext)
            }
        }
        updatePass!!.setOnClickListener {
            it.preventDoubleClick()
            updatePass()
        }
        return alertDialog.show()
    }


    override fun onDetach() {
        onChangePassClick = null
        super.onDetach()
    }


    /*****************************************
     * Methods Call In Current Fragment Dialog
     */
    private fun updatePass() {
        val oldPass = oldPass!!.text.toString().trim { it <= ' ' }
        val newPass = newPass!!.text.toString().trim { it <= ' ' }
        onChangePassClick?.onClick(oldPass, newPass)
        dismiss()

    }

    private fun hideOrShowPassword(
        view: ImageView,
        editText: EditText,
        activity: Context
    ) {
        if (editText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            view.setColorFilter(
                ContextCompat.getColor(activity, R.color.Charcoal),
                PorterDuff.Mode.SRC_IN
            )
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            view.setColorFilter(
                ContextCompat.getColor(activity, R.color.color_application),
                PorterDuff.Mode.SRC_IN
            )
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
    }
}