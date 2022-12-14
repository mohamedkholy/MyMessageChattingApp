package com.momo.mymessage.ui

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.momo.mymessage.Adapter.SearchUsersAdapter
import com.momo.mymessage.R
import com.momo.mymessage.databinding.ActivityAddChatBinding
import com.momo.mymessage.pogo.User



class add_chat_Activity : AppCompatActivity() {

    val auth= FirebaseAuth.getInstance()
    lateinit  var binding: ActivityAddChatBinding
    val databaseReference= FirebaseDatabase.getInstance().getReference()
    var searchUsers_Adapter:SearchUsersAdapter?=null
    var searchBy="name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddChatBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.search.setOnClickListener{

           binding. search.setIconified(false)

        }

        binding.NamesearchButton.setOnClickListener {
            searchBy="name"
            binding.NamesearchButton.background=resources.getDrawable(R.drawable.button_primary_item)
            binding.EmailsearchButton.background=resources.getDrawable(R.drawable.search_choices_background)
            if(!binding.search.query.toString().isEmpty())
            search(binding.search.query.toString())
        }
        binding.EmailsearchButton.setOnClickListener {
            searchBy="email"
            binding.EmailsearchButton.background=resources.getDrawable(R.drawable.button_primary_item)
            binding.NamesearchButton.background=resources.getDrawable(R.drawable.search_choices_background)
            if(!binding.search.query.toString().isEmpty())
                search(binding.search.query.toString())
        }


        binding.search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(p0: String?): Boolean {

                search(p0!!)
                if(p0.length==0){
                    binding.recv.visibility=View.INVISIBLE
                    searchUsers_Adapter!!.stopListening()}

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
                  if(searchUsers_Adapter!=null){
                      binding.recv.visibility=View.INVISIBLE
                searchUsers_Adapter!!.stopListening()}


                return false
            }

        } )





    }




    private fun search(query: String) {
        var options=
            FirebaseRecyclerOptions.Builder<User>().setQuery(databaseReference.child("users").orderByChild(searchBy)
                .startAt(query).endAt(query+"\uf8ff"), User::class.java).build()

         searchUsers_Adapter= SearchUsersAdapter(options,this@add_chat_Activity)
        searchUsers_Adapter!!.startListening()
        binding.recv.adapter=searchUsers_Adapter
        binding.recv.visibility=View.VISIBLE
    }
}