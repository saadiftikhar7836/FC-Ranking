package com.fcranking.android.views.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.fcranking.android.R
import com.fcranking.android.databinding.ActivityMainBinding
import com.fcranking.android.firestore.FirestoreRef
import com.fcranking.android.interfaces.OnChangePassClick
import com.fcranking.android.utils.showToast
import com.fcranking.android.utils.visible
import com.fcranking.android.views.dialog.ProgressDialog
import com.google.firebase.auth.EmailAuthProvider

class MainActivity : AppCompatActivity(), OnChangePassClick {

    //    binding
    lateinit var binding: ActivityMainBinding

    //    nav controller
    private var navigationController: NavController? = null

    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        progressDialog = ProgressDialog(this)
        // Get the navigation host fragment from this Activity
        navigationController = Navigation.findNavController(this, R.id.mainHostFragment)
        setupWithNavController(binding.bottomNavigation, navigationController!!)

        addDestinationChangeListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigationController!!.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onClick(oldPassword: String, newPassword: String) {
        progressDialog.show()
        reAuthenticatePassword(oldPassword, newPassword)
    }

    private fun addDestinationChangeListener() {
        navigationController?.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.fullScreenImageFragment ||
                destination.id == R.id.commentsBottomSheet ||
                destination.id == R.id.replyFragment
            ) {
                binding.bottomNavigation.visible(false)
            } else {
                binding.bottomNavigation.visible(true)
            }
        }
    }

    private fun reAuthenticatePassword(oldPass: String, newPass: String) {
        val authCredential =
            FirestoreRef.getUserEmail()?.let { EmailAuthProvider.getCredential(it, oldPass) }
        authCredential?.let {
            FirestoreRef.getCurrentUser()?.reauthenticate(it)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirestoreRef.getCurrentUser()?.updatePassword(newPass)
                        ?.addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                passSaveInDatabase(newPass)
                            } else {
                                showToast(task1.exception?.message.toString())
                            }
                        }
                } else {
                    showToast(task.exception?.message.toString())
                }
            }
        }
    }

    private fun passSaveInDatabase(pass: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["password"] = pass

        FirestoreRef.getUserRef()
            .document(FirestoreRef.getUserId().toString())
            .update(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    showToast(getString(R.string.pass_updated))
                } else {
                    progressDialog.dismiss()
                    showToast(task.exception?.message.toString())
                }
            }

    }

}