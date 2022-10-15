package com.codesses.fcranking.views.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.codesses.fcranking.firestore.FirestoreRef
import com.codesses.fcranking.utils.startNewActivity
import com.codesses.fcranking.views.auth.SignInActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    //    Handler
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())

//      Hide status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        if (FirestoreRef.getCurrentUser() != null) {
            runnable = Runnable {
                startNewActivity(MainActivity::class.java)
                finish()
            }
            handler.postDelayed(runnable, 2000)
        } else {
            runnable = Runnable {
                startNewActivity(SignInActivity::class.java)
                finish()
            }
            handler.postDelayed(runnable, 2000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}