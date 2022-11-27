package com.momo.mymessage.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.momo.mymessage.R
import com.momo.mymessage.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityResetPasswordBinding
    val auth=FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.resetButton.setOnClickListener{
            if (TextUtils.isEmpty(binding.email.text.toString()))
                binding.email.error="Email is required for this step"
            else{
                binding.pro.visibility=View.VISIBLE
                auth.sendPasswordResetEmail(binding.email.text.toString()).addOnCompleteListener {

                    if(it.isSuccessful)
                    {
                      Toast.makeText(this@ResetPasswordActivity,"Email sent successfully",Toast.LENGTH_LONG).show()
                      finish()
                    }
                    else
                    {
                        Toast.makeText(this@ResetPasswordActivity,it.exception!!.message,Toast.LENGTH_LONG).show()



                    }
                    binding.pro.visibility=View.INVISIBLE

                }



            }

        }







    }
}