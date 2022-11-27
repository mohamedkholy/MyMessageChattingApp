package com.momo.mymessage.ui

import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.momo.mymessage.Adapter.SearchUsers_Adapter
import com.momo.mymessage.databinding.ActivityAddChatBinding
import com.momo.mymessage.pogo.User

val auth= FirebaseAuth.getInstance()
lateinit  var binding: ActivityAddChatBinding
val databaseReference= FirebaseDatabase.getInstance().getReference()
 var searchUsers_Adapter:SearchUsers_Adapter?=null

class add_chat_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddChatBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.search.setOnClickListener{

           binding. search.setIconified(false)

        }





        binding.search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(p0: String?): Boolean {

                search(p0!!)
                if(p0.length==0)
                    searchUsers_Adapter!!.stopListening()

              return false
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                search(p0!!)
                return false
            }


        })

        binding.search.setOnCloseListener ( object :SearchView.OnCloseListener,
            androidx.appcompat.widget.SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                  if(searchUsers_Adapter!=null)
                searchUsers_Adapter!!.stopListening()


                return false
            }

        } )





    }




    private fun search(query: String) {
        var options=
            FirebaseRecyclerOptions.Builder<User>().setQuery(databaseReference.child("users").orderByChild("name")
                .startAt(query).endAt(query+"\uf8ff"), User::class.java).build()
         searchUsers_Adapter= SearchUsers_Adapter(options,this@add_chat_Activity)
        searchUsers_Adapter!!.startListening()
        binding.recv.adapter=searchUsers_Adapter

    }
}