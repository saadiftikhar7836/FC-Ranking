package com.fcranking.android.views.auth

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.fcranking.android.R
import com.fcranking.android.databinding.ActivitySignInBinding
import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.interfaces.OnForgotPassClick
import com.fcranking.android.model.User
import com.fcranking.android.utils.*
import com.fcranking.android.views.activity.MainActivity
import com.fcranking.android.views.dialog.ForgotPasswordDialog
import com.fcranking.android.views.dialog.ProgressDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging

class SignInActivity : AppCompatActivity(), OnForgotPassClick {

    //    Data binding
    private lateinit var binding: ActivitySignInBinding

    //    Variables
    private var isEmail = false
    private var isPassword = false

    //    Dialog
    lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        progressDialog = ProgressDialog(this)
        textChangeListeners()


        //        Click listeners
        binding.clRoot.setOnClickListener {
            it.preventDoubleClick()
            hideKeyboard(this)
        }
        binding.forgotPass.setOnClickListener {
            it.preventDoubleClick()
            forgotPassword()
        }
        binding.btnSignIn.setOnClickListener {
            it.preventDoubleClick()
            signIn()
        }
        binding.llCreateAccount.setOnClickListener {
            it.preventDoubleClick()
            openActivity(SignUpActivity::class.java)
        }

    }

    /*********************************************************************************************************************************************************
     *                                                                       override methods
     ********************************************************************************************************************************************************/

    override fun onApply(email: String?) {
        email?.let {
            FirestoreRef.getAuth().sendPasswordResetEmail(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast(resources.getString(R.string.check_email))
                    } else {
                        val error: String = task.exception.toString()
                        Log.d("ERROR: ", "Error: $error")
                        showToast(message = error)
                    }
                }
        }
    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/

    private fun textChangeListeners() {

        binding.etPasswordField.addTextChangedListener {
            validateField(it!!, binding.etPasswordField)
        }
        binding.etEmailField.addTextChangedListener {
            validateField(it!!, binding.etEmailField)
        }

    }

    private fun validateField(s: Editable, view: TextInputEditText) {
        when (view) {
            binding.etEmailField -> {
                binding.etEmail.apply {
                    if (UiHelper.isValidEmail(s.toString())) {
                        isEmail = true
                    } else {
                        error = resources.getString(R.string.invalid_email_format)
                        isEmail = false
                    }
                    isEndIconVisible = isEmail
                    isErrorEnabled = !isEmail
                }
            }

            binding.etPasswordField -> {
                binding.etPassword.apply {
                    if (UiHelper.isValidPassword(s.toString())) {
                        isPassword = true
                    } else {
                        error = resources.getString(R.string.invalid_password_format)
                        isPassword = false
                    }
                    isEndIconVisible = isPassword
                    isErrorEnabled = !isPassword

                }
            }
        }

        updateButtonState()
    }

    //    function for updating state
    private fun updateButtonState() {
        binding.btnSignIn.apply {
            isEnabled =
                isEmail && isPassword
            background = getDrawable(this@SignInActivity, isEnabled)
        }
    }

    //    forgot password
    private fun forgotPassword() {
        val forgotPasswordDialog: ForgotPasswordDialog = ForgotPasswordDialog()
        forgotPasswordDialog.isCancelable = false
        forgotPasswordDialog.show(supportFragmentManager, "Forgot Password Dialog")

    }

    //    signIn Method
    private fun signIn() {
        val email = binding.etEmailField.text.toString().trim()
        val pass = binding.etPasswordField.text.toString().trim()

        hideKeyboard(this)

        progressDialog.show()
        signInWithEmail(email, pass)
    }

    private fun signInWithEmail(email: String, pass: String) {
        FirestoreRef.getAuth()
            .signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser = FirestoreRef.getCurrentUser()!!
                    if (user.isEmailVerified) {

//                            Get current user information
                        getCurrentUserInfo(user.uid, pass)
                    } else {
                        user.sendEmailVerification()

//                            Dismiss dialog
                        progressDialog.dismiss()
                        showSnackBar(binding.root, resources.getString(R.string.email_verify_msg))

//                            SignOut for email verification
                        FirestoreRef.getAuth().signOut()
                    }
                } else {

                    //            TODO: DISMISS DIALOG
                    progressDialog.dismiss()
                    showSnackBar(binding.root, task.exception?.message.toString())
                }
            }


    }

    private fun getCurrentUserInfo(uid: String, pass: String) {
        FirestoreRef.getUserRef().document(uid).get().addOnSuccessListener {
            updateFcm()
        }
    }

    private fun updateFcm() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val map: MutableMap<String, Any> = HashMap()
                map["password"] = binding.etPasswordField.text.toString()
                map["fcm_token"] = it.result.toString()
                FirestoreRef.getUserRef()
                    .document(FirestoreRef.getUserId().toString())
                    .update(map)
                    .addOnSuccessListener {
                        getCurrentUserData()
                    }
            } else {
                updateFcm()
            }
        }

    }

    private fun getCurrentUserData() {
        FirestoreRef.getUserRef().document(FirestoreRef.getUserId().toString())
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    user?.let {
                        it.userId = snapshot.id
                        FCSharedStorage.saveUserData(user)
                        startNewActivity(MainActivity::class.java)
                    }
                } else
                    showSnackBar(binding.root, resources.getString(R.string.something_went_wrong))
                progressDialog.dismiss()

            }
    }


}