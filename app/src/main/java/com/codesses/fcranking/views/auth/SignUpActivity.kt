package com.codesses.fcranking.views.auth

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.codesses.fcranking.R
import com.codesses.fcranking.databinding.ActivitySignUpBinding
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.utils.*
import com.codesses.fcranking.views.dialog.ProgressDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity(), OnFocusChangeListener {

    lateinit var binding: ActivitySignUpBinding

    //   Variables
    private var fullName = ""
    private var email = ""
    private var password = ""
    private var confirmPass = ""
    private var fcmToken = ""

    private var isFullName = false
    private var isEmail = false
    private var isPassword = false
    private var isConfirmPassword = false

    //    progress dialog object
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)


        progressDialog = ProgressDialog(this)
        //       Click listeners
        binding.clRoot.setOnClickListener {
            it.preventDoubleClick()
            hideKeyboard(this)
        }
        binding.backPress.setOnClickListener {
            it.preventDoubleClick()
            finish()
        }
        binding.btnSignUp.setOnClickListener {
            it.preventDoubleClick()
            getInputFromUser()
        }
        binding.llAlreadyAccount.setOnClickListener {
            it.preventDoubleClick()
            finish()
        }
        textChangeListeners()
    }

    /*********************************************************************************************************************************************************
     *                                                                       override methods
     ********************************************************************************************************************************************************/

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            binding.clPasswordHint.visible(true)
        } else {
            binding.clPasswordHint.visible(false)
        }
    }

    /*********************************************************************************************************************************************************
     *                                                                       Calling methods
     ********************************************************************************************************************************************************/


    private fun textChangeListeners() {

        binding.etFullNameField.addTextChangedListener {
            validateField(it!!, binding.etFullNameField)
        }

        binding.etPasswordField.apply {

            onFocusChangeListener = this@SignUpActivity
            addTextChangedListener {
                hintChangeOnPassword(it!!)
                validateField(it, binding.etPasswordField)
            }
        }

        binding.etConfirmPasswordField.addTextChangedListener {
            validateField(it!!, binding.etConfirmPasswordField)
        }

        binding.etEmailField.addTextChangedListener {
            validateField(it!!, binding.etEmailField)
        }

    }

    //    function for validating fields

    private fun validateField(s: Editable, view: TextInputEditText) {
        when (view) {
            binding.etFullNameField -> {
                binding.etFullName.apply {
                    if (s.length >= 3) {
                        isFullName = true
                    } else {
                        error = resources.getString(R.string.invalid_first_name)
                        isFullName = false
                    }
                    isEndIconVisible = isFullName
                    isErrorEnabled = !isFullName
                }
            }

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

            binding.etConfirmPasswordField -> {
                val pass = binding.etPasswordField.text.toString().trim()
                val confirmPass = s.toString().trim { it <= ' ' }
                binding.etConfirmPassword.apply {
                    if (isPassword) {
                        binding.etPassword.isErrorEnabled = !isPassword
                        if (pass == confirmPass) {
                            isConfirmPassword = true
                        } else {
                            error = resources.getString(R.string.invalid_confirm_password_format)
                            isConfirmPassword = false
                        }
                        isEndIconVisible = isConfirmPassword
                        isErrorEnabled = !isConfirmPassword
                    } else {
                        isConfirmPassword = isPassword
                        binding.etPassword.isErrorEnabled = !isPassword
                        binding.etPassword.error = resources.getString(R.string.password_required)
                        binding.etPassword.isEndIconVisible = isPassword
                    }
                }
            }
        }

        updateButtonState()
    }

    //    function for variables initialization
    private fun getInputFromUser() {
        fullName = binding.etFullNameField.text.toString().trim()
        email = binding.etEmailField.text.toString().trim()
        password = binding.etPasswordField.text.toString().trim()
        confirmPass = binding.etConfirmPasswordField.text.toString().trim()
        fcmToken = FirebaseMessaging.getInstance().token.toString()


        //  Hide keyboard
        hideKeyboard(this)

        //        show dialog
        progressDialog.show()


//          User creating
        createUser(email, password)

    }

    private fun createUser(email: String, password: String) {
        FirestoreRef.getAuth()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

//                            TODO: Get User & Send Verification mail
                    val user: FirebaseUser = FirestoreRef.getCurrentUser()!!
                    user.sendEmailVerification()

//                            TODO: User Data Saved To Firebase Database
                    saveInfoToFirebase()
                } else {
//                            TODO: Dismiss Dialog
                    progressDialog.dismiss()
                    val error: String = task.exception?.message.toString()
                    showSnackBar(binding.root, "Error: $error")
                }
            }
    }

    private fun saveInfoToFirebase() {
        val map: MutableMap<String, Any> = HashMap()

        map["full_name"] = fullName
        map["email"] = email
        map["password"] = password
        map["fcm_token"] = fcmToken
        map["role"] = "user"

        FirestoreRef.getUserRef()
            .document(FirestoreRef.getUserId().toString())
            .set(map)
            .addOnSuccessListener {
                //    TODO: Dismiss Dialog
                progressDialog.dismiss()
                Toast.makeText(
                    this@SignUpActivity,
                    getString(R.string.email_verify_msg),
                    Toast.LENGTH_SHORT
                ).show()
                FirestoreRef.getAuth().signOut()
                this.startNewActivity(SignInActivity::class.java)
            }
            .addOnFailureListener {
                //    TODO: Dismiss Dialog
                progressDialog.dismiss()
                showSnackBar(binding.root, it.message.toString())
            }
    }

    //    function for updating state
    private fun updateButtonState() {
        binding.btnSignUp.apply {
            isEnabled =
                isFullName && isEmail && isPassword && isConfirmPassword
            background =
                getDrawable(
                    this@SignUpActivity,
                    isEnabled
                )
        }
    }

    private fun hintChangeOnPassword(s: CharSequence) {
        val digitPattern = Pattern.compile("(.)*(\\d)(.)*")
        val lettersPattern = Pattern.compile("(.)*[a-zA-Z](.)*")
        val digits = digitPattern.matcher(s).find()
        val letters = lettersPattern.matcher(s).find()

//                        Check length of the password
        if (s.length >= 8) {
            binding.tvPasswordHint2.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorAquaMarine
                )
            )
        } else {
            binding.tvPasswordHint2.setTextColor(ContextCompat.getColor(this, R.color.Red))
        }
        if (digits) {
            binding.tvPasswordHint4.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorAquaMarine
                )
            )
        } else {
            binding.tvPasswordHint4.setTextColor(
                ContextCompat.getColor(this, R.color.Red)
            )
        }
        if (letters) {
            binding.tvPasswordHint6.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorAquaMarine
                )
            )
        } else {
            binding.tvPasswordHint6.setTextColor(ContextCompat.getColor(this, R.color.Red))
        }
    }


}