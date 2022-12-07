package com.momo.mymessage.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.momo.mymessage.R
import com.momo.mymessage.databinding.ActivityProfileBinding
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    var profile_image:Uri?=null
    val id=FirebaseAuth.getInstance().currentUser!!.uid
    val databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(id!!)
    val storageReference=FirebaseStorage.getInstance().getReference().child("/usersImage")
    lateinit var sp: SharedPreferences
    lateinit var editor:SharedPreferences.Editor
    val list= arrayListOf<String>()
    var flag=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sp=getSharedPreferences("info", MODE_PRIVATE)
        editor=sp.edit()
        setinfo()


        FirebaseDatabase.getInstance().getReference().child("users").get().addOnSuccessListener { data->

            val x=intent.getStringExtra("name")
            for (i in data.children){
               if( !i.child("name").getValue().toString().equals(x))
                list.add( i.child("name").getValue().toString() )}
            flag=true

        }


        binding.img.setOnClickListener{

            askforpemissionAndGo()

        }

        binding.add.setOnClickListener{

            askforpemissionAndGo()

        }

        binding.update.setOnClickListener{

            if(TextUtils.isEmpty(binding.username.text.toString()))
            binding.username.error="Required"
            else if(binding.username.text!!.length<4&&binding.username.text!!.length>20)
                binding.username.error=("Username shold be 4 charecter at least and not exceed 20 charecter ")
            else if(list.contains(binding.username.text.toString())){
                binding.username.error=("Username is not available ")}
            else if (!flag)
               Toast.makeText(this@ProfileActivity,"Faild",Toast.LENGTH_SHORT).show()
            else
                updateProfile()


        }



    }

    private fun updateProfile() {
        binding.pro.visibility=View.VISIBLE

        if(profile_image!=null){



                storageReference.child(id!!).putFile(profile_image!!).addOnSuccessListener {

                  it.storage.downloadUrl.addOnSuccessListener {
                      val url=it.toString()


                      val hashMap= hashMapOf<String,Any>().apply {
                          put("Imageurl",url)
                          put("name",binding.username.text.toString())
                      }


                      databaseReference.updateChildren(hashMap).addOnCompleteListener{

                          if(it.isSuccessful){
                              editor.putString("username",binding.username.text.toString())
                              editor.putString("imgUrl",url)
                              editor.apply()



                              finish()

                          }
                          else{
                              Toast.makeText(this@ProfileActivity,it.exception!!.message,Toast.LENGTH_LONG).show()
                          }



                      binding.pro.visibility=View.INVISIBLE
                  }




                }

            }



        }
        else{

            val hashMap= hashMapOf<String,Any>().apply {
                put("name",binding.username.text.toString())
            }
            databaseReference.updateChildren(hashMap).addOnCompleteListener {
                if(it.isSuccessful){
                    editor.putString("username",binding.username.text.toString())
                    editor.apply()
                    finish()

                }
                else{
                    Toast.makeText(this@ProfileActivity,it.exception!!.message,Toast.LENGTH_LONG).show()
                }
                binding.pro.visibility=View.INVISIBLE
            }


        }









    }

    private fun askforpemissionAndGo() {
        if(ContextCompat.checkSelfPermission(this@ProfileActivity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            getImage()

        }
        else if(ActivityCompat.shouldShowRequestPermissionRationale(this@ProfileActivity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val alertDialog= AlertDialog.Builder(this@ProfileActivity)

            alertDialog.apply {
                setTitle("Permission Required")
                alertDialog.setCancelable(false)
                setMessage("Please accept permission to access gallary")
                setPositiveButton("OK")
                {_, _ ->
                    permissionlancher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.create().show()


        }
        else{
            permissionlancher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun getImage() {


        mGetContent.launch("image/*")

    }


    private fun setinfo() {
        if(profile_image==null)
        Picasso.get().load(intent.getStringExtra("img")).into(binding.img)
        binding.username.setText(intent.getStringExtra("name"))
    }


    private val permissionlancher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted->
        if(isGranted){
            getImage()

        }
        else{
            Toast.makeText(this@ProfileActivity,"You can not access gallary", Toast.LENGTH_SHORT).show()

        }



    }

    private val mGetContent = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent())
    { uri -> // Handle the returned Uri
        profile_image=uri

        binding.img.setImageURI(profile_image)
    }

}