package com.momo.mymessage.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.momo.mymessage.databinding.ActivityLoginBinding


class login : AppCompatActivity() {

    lateinit   var binding:ActivityLoginBinding
    val auth=FirebaseAuth.getInstance()
    val realdataref= FirebaseDatabase.getInstance().getReference()
    lateinit var sp:SharedPreferences
    lateinit var editor:SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sp=getSharedPreferences("info", MODE_PRIVATE)
        editor=sp.edit()
        binding.email.setText(intent.getStringExtra("email"))
        binding.password.setText(intent.getStringExtra("password"))


        binding.signuup.setOnClickListener{
            val intent=Intent(this, SignUp::class.java)
            startActivity(intent)

           }



        binding.loginB.setOnClickListener{

            if(notEmpty()&&isvalid()){
            login()
                binding.lin.visibility=View.VISIBLE
            }
        }


        binding.passwordForgetButton.setOnClickListener{


            val intent=Intent(this@login,ResetPasswordActivity::class.java)
            startActivity(intent)



        }


    }


    fun notEmpty():Boolean{
        if(!binding.email.text!!.isEmpty()&&!binding.password.text!!.isEmpty())
        return true
          else{
              Toast.makeText(baseContext,"Empty Field",Toast.LENGTH_SHORT).show()
        return false}

    }

    fun isvalid():Boolean{
        if(binding.email.text!!.length<8){
            binding.email.setError("Email shold be 8 charecter at least")
        return false
        }

       if(binding.password.text!!.length<6)
       {  binding.password.setError("Password shold be 6 charecter at least")
        return false}
return true

    }


    fun login(){
      auth.signInWithEmailAndPassword(binding.email.text.toString(),binding.password.text.toString())
          .addOnCompleteListener{

              if(it.isSuccessful){
                  realdataref.child("users").child(auth.currentUser!!.uid).get().addOnSuccessListener {
                      val username=it.child("name").getValue().toString()
                      val imgUrl=it.child("Imageurl").getValue().toString()
                      editor.putString("username",username)
                      editor.putString("imgUrl",imgUrl)
                      editor.apply()
                      binding.lin.visibility=View.INVISIBLE
                      val intent=Intent(baseContext, home::class.java)
                      intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                      startActivity(intent)
                      finish()
                       }

              }
              else{binding.lin.visibility=View.INVISIBLE

                  Toast.makeText(baseContext,it.exception!!.localizedMessage.toString(),Toast.LENGTH_LONG).show()

              }


          }


   }


}