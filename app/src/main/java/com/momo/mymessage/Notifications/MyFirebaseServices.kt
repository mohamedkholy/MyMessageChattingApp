package com.momo.mymessage.Notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.momo.mymessage.pogo.Token

class MyFirebaseServices : FirebaseInstanceIdService() {



    val firebaseUser=FirebaseAuth.getInstance().currentUser

    override fun onTokenRefresh() {
        super.onTokenRefresh()
      val refreshToken=FirebaseInstanceId.getInstance().token
        Log.d("ggggggggg",refreshToken.toString())
        if(firebaseUser!=null)
            updateToken(refreshToken)

    }

    private fun updateToken(refreshToken: String?) {
      val ref=FirebaseDatabase.getInstance().getReference()
        val token= Token(refreshToken)
        ref.child("users").child(firebaseUser!!.uid).setValue(token)
    }


}