package com.momo.mymessage.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.momo.mymessage.Adapter.HomeChatsAdabter
import com.momo.mymessage.pogo.Token
import com.momo.mymessage.R
import com.momo.mymessage.ViewModels.HomeChatsViewModel
import com.momo.mymessage.databinding.ActivityHomeBinding
import com.momo.mymessage.db.dbChatsManage
import com.momo.mymessage.pogo.User
import com.squareup.picasso.Picasso


class home : AppCompatActivity() {

    val auth=FirebaseAuth.getInstance()
    val databaseReference=FirebaseDatabase.getInstance().getReference()
    val id=auth.uid
    lateinit var binding:ActivityHomeBinding
    lateinit var dbChatsManage:dbChatsManage
    lateinit var chatschats_Adabter:HomeChatsAdabter
    val  chats_list= arrayListOf<User>()
    lateinit var sp:SharedPreferences
    lateinit var  spp:SharedPreferences
    lateinit var  editor:SharedPreferences.Editor
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawer_header: View
    lateinit var header_img:ImageView
    lateinit var header_text:TextView
    lateinit var homeChatsViewModel:HomeChatsViewModel







    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      binding=ActivityHomeBinding.inflate(layoutInflater)
      setContentView(binding.root)


       initial()


        homeChatsViewModel= ViewModelProvider(this).get(HomeChatsViewModel::class.java)
        homeChatsViewModel.getChats()
        homeChatsViewModel.liveData.observe(this@home,androidx.lifecycle.Observer{
            Log.d("hhhhhhhhhhhhhh",it.toString())
            chats_list.clear()
            chats_list.addAll(it)
            chatschats_Adabter.notifyDataSetChanged()
            binding.pro.visibility=View.GONE

        })


        updateToken(FirebaseInstanceId.getInstance().token!!)


        binding.img.setOnClickListener{

            val intent=Intent(this@home,ProfileActivity::class.java)
            intent.putExtra("name",sp.getString("username",null))
            intent.putExtra("img",sp.getString("imgUrl",null))
            startActivity(intent)


        }




      //ActionBar
      setSupportActionBar(binding.toolbar)
      supportActionBar?.setDisplayShowTitleEnabled(false)
        header_text.setText(sp.getString("username",null))
        Picasso.get().load(sp.getString("imgUrl",null)).into( header_img)
        toggle= ActionBarDrawerToggle(this,binding.drawerLayout,binding.toolbar,R.string.open,R.string.close)
        toggle.drawerArrowDrawable.color=resources.getColor(R.color.white)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()



        binding.nav.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.logout ->{
                    auth.signOut()
                    val intent=Intent(this@home, login::class.java)
                    intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()



                }

                R.id.option->{
                    val intent=Intent(this@home,ProfileActivity::class.java)
                    intent.putExtra("name",sp.getString("username",null))
                    intent.putExtra("img",sp.getString("imgUrl",null))
                    startActivity(intent)


                }

            }
            true

        }



        //addchat
       binding.addChat.setOnClickListener{

           val intent=Intent(this@home,add_chat_Activity::class.java)
          startActivityForResult(intent,1)
       }

    }


    override fun onStart() {
        super.onStart()

        val hashMap= mutableMapOf<String,Any>()
        hashMap.put("status","online")
        databaseReference.child("users").child(id!!).updateChildren(hashMap)
        Firebase.messaging.isAutoInitEnabled = true

    }

    override fun onResume() {
        super.onResume()

        if (chatschats_Adabter!=null)
            chatschats_Adabter.notifyDataSetChanged()


        header_text.setText(sp.getString("username",null))
        Picasso.get().load(sp.getString("imgUrl",null)).into( header_img)


        editor.putString("userid",null)
        editor.apply()
    }






    private fun initial() {
        spp=getSharedPreferences("CurrentUser", MODE_PRIVATE)
        editor=spp.edit()
        sp=getSharedPreferences("info", MODE_PRIVATE)
        drawer_header=binding.nav.getHeaderView(0)
        header_img=drawer_header.findViewById(R.id.profilePhoto)
        header_text=drawer_header.findViewById(R.id.drawer_text)
        chatschats_Adabter=HomeChatsAdabter(this@home, chats_list)
        binding.recvChats.adapter =chatschats_Adabter
    }

    fun updateToken(token:String){
        val ref=FirebaseDatabase.getInstance().getReference().child("users")
        val token1= Token(token)
        ref.child(id!!).child("token").setValue(token1)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode==1){


            var user=data!!.getSerializableExtra("user") as? User
            homeChatsViewModel.addChat(user!!)


            }
    }

    override fun onPause() {
        super.onPause()
        val hashMap= mutableMapOf<String,Any>()
        hashMap.put("status","offline")
        databaseReference.child("users").child(id!!).updateChildren(hashMap)
    }


    override fun onDestroy() {
        super.onDestroy()
        if(homeChatsViewModel.flag)
        homeChatsViewModel.removeListener()
    }

}