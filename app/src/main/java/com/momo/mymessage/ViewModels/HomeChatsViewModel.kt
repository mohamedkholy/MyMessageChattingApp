package com.momo.mymessage.ViewModels

import android.app.Activity
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.momo.mymessage.db.db_chats_manage
import com.momo.mymessage.pogo.Message
import com.momo.mymessage.pogo.User
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.awaitAll
import java.util.logging.Handler

class HomeChatsViewModel(activity: Activity) : ViewModel() {

    val liveData=MutableLiveData<ArrayList<User>>()
    lateinit var chats_list:ArrayList<User>
    lateinit var chatsname:ArrayList<String>
     var toupdateList=ArrayList<String>()
    val databaseReference= FirebaseDatabase.getInstance().getReference()
    val dbChatsManage= db_chats_manage(activity)
    val id=FirebaseAuth.getInstance().currentUser!!.uid
     var count=0
     var ii=0



   fun getChats(){
       chatsname=dbChatsManage.getChatsid()
       chats_list=dbChatsManage.getChats(id!!)

       if(chatsname.size>0)
           liveData.value=chats_list

       databaseReference.child("users").child(id!!).child("messages").get().addOnSuccessListener {
           count=it.children.count()
           if(count==0)
              liveData.value=chats_list
           if(count==chats_list.size)
               updateChats()


           ii=chats_list.size

           for (i in it.children){

           if(!chatsname.contains( i.key.toString())) {
               chatsname.add(i.key.toString())
               getChat(i.key.toString())
           }


           }



       }
   }




    fun updateChats() {
        toupdateList.addAll(chatsname)
        databaseReference.child("users").child(id!!).child("messages").limitToFirst(1).get().addOnSuccessListener {
            for (i in it.children) {

                val userid = i.key

                 updateChatInfo(userid)


            }



            databaseReference.child("users").child(id!!).child("messages")
                .addChildEventListener(object :
                    ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        if (!chatsname.contains(snapshot.key.toString())) {
                            getChat(snapshot.key.toString())
                        }

                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {

                        val userid = snapshot.key!!
                        updateChatInfo(userid)
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }




    }

    private fun updateChatInfo(userID: String?) {
        databaseReference.child("users").child(userID!!).child("messages").child(id!!)
            .child("chats").get().addOnSuccessListener{

                val userid=userID
                var lastm = ""
                var count = 0
                var time= ""
                for (i in it.children) {
                    var message = i.getValue(Message::class.java)
                    if (message!!.senderid.equals(userid) && message!!.seen.equals("unseen"))
                        count++

                }


                var name = ""
                var text = ""
                databaseReference.child("users").child(id).child("messages")
                    .child(userid).child("last").get().addOnSuccessListener {

                        val message = it.getValue(Message::class.java)

                        if (message != null) {
                            if (message.text.equals("") && message.imgurl.equals("") && message.record.equals(""))
                                text = ""
                            else {
                                if (message.senderid.equals(id))
                                    name = "You: "
                                if (message.record != null)
                                    text = "Voice"
                                else if (!message.imgurl.equals("noImg"))
                                    text = "Image"
                                else
                                    text = message.text!!
                            }

                            lastm = name + text
                            time = message.time.toString()

                        }
                        toupdateList.remove(userid)
                        if(toupdateList.size>0)
                        {
                            updateChatInfo(toupdateList[0])
                        }
                        val user = dbChatsManage.getChatById(userid,id)
                        Log.d("ggggggggg",user.toString())
                        if(count!=user.useen?.toInt()||!user.last.equals(lastm)) {


                            val newuser = User(
                                user.name,
                                user.Imageurl,
                                user.userid,
                                user.email,
                                lastm,
                                user.status,
                                count.toString(),
                                time
                            )
                            chats_list.removeAt(chatsname.indexOf(userid))
                            chatsname.remove(user.userid)
                            chats_list.add(0, newuser)
                            chatsname.add(0, user.userid!!)
                            dbChatsManage.deleteChat(id, user.userid)
                            dbChatsManage.addChat(id, newuser)
                            liveData.value = chats_list

                        }

                    }
            }

    }

    fun addChat(user:User){

        if(user!=null&&!chatsname.contains(user.userid)) {
            dbChatsManage.addChat(id,user)
            chats_list .add(0,user)
            chatsname.add(0,user.userid!!)
            liveData.value=chats_list
        }

    }


    private fun getChat(idd: String) {

        databaseReference.child("users").child(idd).get().addOnSuccessListener{

            ii++

             val user=it.getValue(User::class.java)

             chatsname.add(0,idd)
             chats_list.add(0,user!!)

            dbChatsManage.addChat(id!!,user)
            liveData.value=chats_list

            if(ii==count) {
                updateChats()

            }

        }


        }



}
