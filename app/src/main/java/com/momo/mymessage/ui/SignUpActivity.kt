package com.momo.mymessage.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.momo.mymessage.databinding.ActivitySignupBinding






class SignUpActivity : AppCompatActivity() {

     lateinit var binding:ActivitySignupBinding
     val auth=FirebaseAuth.getInstance()
     val ref=FirebaseDatabase.getInstance().getReference()
     var url="https://firebasestorage.googleapis.com/v0/b/mymessage-4eb68.appspot.com/o/profile.png?alt=media&token=5b34dbe5-2019-46c0-b26a-c4b0ef51dfee"
     val StorageRef=FirebaseStorage.getInstance().getReference()
      var  profile_image:Uri? = null
    val list=ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ref.child("users").get().addOnSuccessListener {

                data->
            for (i in data.children){
                list.add( i.child("name").getValue().toString() )}

        }


        //signupButton_Click
        binding.signup.setOnClickListener{
            if(isvalid()&&notEmpty()&&isUsernameAvaliable()){
                sign()
                binding.pro.visibility=View.VISIBLE
            }
        }


        //selectphoto
        binding.img.setOnClickListener{
            askStroagePer_getImage()

        }

        binding.add.setOnClickListener{

            askStroagePer_getImage()
        }








    }

    private fun isUsernameAvaliable(): Boolean {


        if(list.contains(binding.username.text.toString())){


        binding.username.setError("Username is not available")
            return false
        }

        return true
    }

    private fun askStroagePer_getImage() {
        if(ContextCompat.checkSelfPermission(this@SignUpActivity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
        {
            getImage()

        }
        else if(ActivityCompat.shouldShowRequestPermissionRationale(this@SignUpActivity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val alertDialog=AlertDialog.Builder(this@SignUpActivity)

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



    fun notEmpty():Boolean{
        if(!TextUtils.isEmpty(binding.email.text)&&!TextUtils.isEmpty(binding.password.text)&&!TextUtils.isEmpty(binding.username.text))
            return true
        else{
            Toast.makeText(baseContext,"Empty Field", Toast.LENGTH_SHORT).show()
            return false}

    }

    fun isvalid():Boolean{
        if(binding.email.text!!.length<8)
        {binding.email.setError("Email shold be 8 charecter at least")
            return false
        }

        if(binding.password.text!!.length<6)
        {  binding.password.setError("Password shold be 6 charecter at least")
            return false
        }

        if(binding.username.text!!.length<4||binding.username.text!!.length>20)
        { binding.username.error=("Username shold be 4 charecter at least and not exceed 20 charecter ")
            return false
        }




        return true

    }


    fun sign(){
        auth.createUserWithEmailAndPassword(binding.email.text.toString(),binding.password.text.toString())
            .addOnCompleteListener{

                if(it.isSuccessful){
                    if (profile_image != null) {
                        StorageRef.child("/usersImage").child(auth.currentUser!!.uid).putFile(
                            profile_image!!
                        )
                            .addOnSuccessListener{ taskSnapshot ->
                                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                                    url=it.toString()
                                    var id=auth.currentUser!!.uid

                                    var hashMap= hashMapOf<String,String>()
                                    hashMap.put("userid",id)
                                    hashMap.put("name", binding.username.text.toString())
                                    hashMap.put("email",binding.email.text.toString())
                                    hashMap.put("Imageurl",url)
                                    ref.child("users").child(id).setValue(hashMap)
                                    binding.pro.visibility= View.INVISIBLE
                                    val intent= Intent(baseContext, login::class.java)
                                    intent.putExtra("email",binding.email.text.toString())
                                    intent.putExtra("password",binding.password.text.toString())
                                    startActivity(intent)
                                    auth.signOut()
                                    finish()
                                }
                            }

                            ?.addOnFailureListener(OnFailureListener { e ->
                                print(e.message)
                            })
                    }

                    else{

                        var id=auth.currentUser!!.uid

                        var hashMap= hashMapOf<String,String>()
                        hashMap.put("name", binding.username.text.toString())
                        hashMap.put("userid",id)
                        hashMap.put("email",binding.email.text.toString())
                        hashMap.put("Imageurl",url)
                        ref.child("users").child(id).setValue(hashMap)
                        binding.pro.visibility= View.INVISIBLE
                        val intent= Intent(baseContext, login::class.java)
                        intent.putExtra("email",binding.email.text.toString())
                        intent.putExtra("password",binding.password.text.toString())
                        startActivity(intent)
                        auth.signOut()
                        finish()
                    }




                }
                else{  binding.pro.visibility= View.INVISIBLE
                    Toast.makeText(baseContext,it.exception!!.localizedMessage.toString(),Toast.LENGTH_LONG).show()

                }


            }


    }

    private val permissionlancher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted->
        if(isGranted){
            getImage()

        }
        else{
            Toast.makeText(this@SignUpActivity,"You can not access gallary",Toast.LENGTH_SHORT).show()

        }



    }

    private val mGetContent = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent())
    { uri -> // Handle the returned Uri
        profile_image=uri
        binding.img.setImageURI(profile_image)
    }


    /*  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
          super.onActivityResult(requestCode, resultCode, data)
          val h = data!!.data
          val a: StorageReference = firebaseref.child(System.currentTimeMillis().toString() + "")
          a.putFile(h!!).addOnSuccessListener {
              a.downloadUrl.addOnSuccessListener { uri ->
                  if (uri != null) {
                      var p: Bitmap? = null
                      try {
                          p = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), h)
                      } catch (e: IOException) {
                          e.printStackTrace()
                      }
                      img.setImageBitmap(p)
                  }
              }
          }
      }*/

}