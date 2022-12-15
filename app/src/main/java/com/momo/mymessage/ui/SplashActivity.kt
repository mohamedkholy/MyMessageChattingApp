package com.momo.mymessage.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.momo.mymessage.R


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth=FirebaseAuth.getInstance()
        val user=auth.currentUser

       Handler(Looper.getMainLooper()).postDelayed({
            if(user==null){
           var intent=Intent(this, login::class.java)
          startActivity(intent)
            }
          else
            {    var intent=Intent(this, home::class.java)
                startActivity(intent)

            }
           finish()

       },2500)

    }



}