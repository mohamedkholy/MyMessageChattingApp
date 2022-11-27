package com.momo.mymessage.ui

import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.momo.mymessage.databinding.ActivityFullPhotoBinding
import com.squareup.picasso.Picasso

class FullPhotoActivity : AppCompatActivity() {


    lateinit var binding:ActivityFullPhotoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFullPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Picasso.get().load(intent.getStringExtra("photo")).into(binding.img)



    }
}