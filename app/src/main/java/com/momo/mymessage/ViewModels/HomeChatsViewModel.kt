package com.momo.mymessage.ViewModels

import android.app.Activity
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.momo.mymessage.db.db_chats_manage
import com.momo.mymessage.pogo.User

class HomeChatsViewModel(activity: Activity) : ViewModel() {

    val liveData=MutableLiveData<ArrayList<User>>()
    lateinit var chats_list:ArrayList<User>
    lateinit var chatsname:ArrayList<String>
    val databaseReference= FirebaseDatabase.getInstance().getReference()
    val dbChatsManage= db_chats_manage(activity)
    val id=FirebaseAuth.getInstance().currentUser!!.uid



    fun getChats(){
        chatsname=dbChatsManage.getChatsid()
        chats_list=dbChatsManage.getChats(id!!)
        liveData.value=chats_list

        databaseReference.child("users").child(id!!).child("messages").addChildEventListener(object :
            ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Log.d("ggg", list_to_get.toString())
                if(!chatsname.contains( snapshot.key.toString())) {
                    chatsname.add(snapshot.key.toString())
                    getChat(snapshot.key.toString())
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }


        })


    }


    fun addChat(user:User){

        if(user!=null&&!chatsname.contains(user.userid)) {
            dbChatsManage.addChat(id,user)
            chats_list .add(user)
            chatsname.add(user.userid!!)
            liveData.value=chats_list
        }

    }


    private fun getChat(idd: String) {



        databaseReference.child("users").child(idd).get().addOnSuccessListener{
            val user=it.getValue(User::class.java)

            dbChatsManage.addChat(id!!,user!!)
            chats_list.add(user)
            liveData.value=chats_list
        }







    }

}