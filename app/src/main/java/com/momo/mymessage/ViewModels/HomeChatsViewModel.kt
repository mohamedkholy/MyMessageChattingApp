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
    val databaseReference= FirebaseDatabase.getInstance().getReference()
    val dbChatsManage= db_chats_manage(activity)
    val id=FirebaseAuth.getInstance().currentUser!!.uid
    var flag=false



   fun getChats(){
       chatsname=dbChatsManage.getChatsid()
       chats_list=dbChatsManage.getChats(id!!)

       if(chatsname.size>0)
           liveData.value=chats_list

       databaseReference.child("users").child(id!!).child("messages").get().addOnSuccessListener {

           for (i in it.children){

           if(!chatsname.contains( i.key.toString())) {
               chatsname.add(i.key.toString())
               getChat(i.key.toString())

           }
           }
          if(it.children.count()==0)
            liveData.value=chats_list

           android.os.Handler(Looper.getMainLooper()).postDelayed({

                  updateChats()
                 flag=true
           },5000)

       }
   }




    fun updateChats(){

        databaseReference.child("users").child(id!!).child("messages").get().addOnSuccessListener {
            for (i in it.children ) {

                val userid = i.key
                val x =
                    databaseReference.child("users").child(userid!!).child("messages").child(id!!)
                        .get().addOnCompleteListener {

                        if (it.isSuccessful) {
                            var lastm = ""
                            var count = 0
                            var time: String = ""
                            for (i in it.result.child("chats").children) {
                                var message = i.getValue(Message::class.java)
                                if (message!!.senderid.equals(userid) && message!!.seen.equals("unseen"))
                                    count++

                            }
                            dbChatsManage.updateunseen(count.toString(), userid)

                            var name = ""
                            var text = ""
                            val message = it.result.child("last").getValue(Message::class.java)
                            if (message != null) {
                                if (message.senderid.equals(id))
                                    name = "You: "
                                if (message.record != null)
                                    text = "Voice"
                                else if (message.text!!.isEmpty() && !message.imgurl.equals("noImg"))
                                    text = "Image"
                                else
                                    text = message.text

                                lastm = name + text
                                time = message.time.toString()
                                dbChatsManage.setLastMesssage(lastm, userid)
                                dbChatsManage.setLastMesssageTime(time, userid)
                            }
                            val user = dbChatsManage.getChatById(userid)
                            chats_list.set(
                                chatsname.indexOf(userid),
                                User(
                                    user.name,
                                    user.Imageurl,
                                    user.userid,
                                    user.email,
                                    lastm,
                                    "",
                                    count.toString(),
                                    time
                                )
                            )

                            liveData.value = chats_list

                        }

                    }


            }

            databaseReference.child("users").child(id!!).child("messages").addChildEventListener(object :
                ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    if(!chatsname.contains( snapshot.key.toString())) {
                        chatsname.add(snapshot.key.toString())
                        getChat(snapshot.key.toString())
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val userid=snapshot.key!!
                    databaseReference.child("users").child(userid).child("messages").child(id!!).child("chats").get().addOnSuccessListener {
                        var lastm=""
                        var count=0
                        var time:String=""
                        for (i in it.children )
                        {var message=i.getValue(Message::class.java)
                            if(message!!.senderid.equals(userid)&&message!!.seen.equals("unseen"))
                                count++

                        }
                        dbChatsManage.updateunseen(count.toString(),userid)

                        var name= ""
                        var text = ""
                        val message = snapshot.child("last").getValue(Message::class.java)
                        if (message != null) {
                            if (message.senderid.equals(id))
                                name = "You: "
                            if (message.record != null)
                                text = "Voice"
                            else if (message.text!!.isEmpty() && !message.imgurl.equals("noImg"))
                                text = "Image"
                            else
                                text = message.text

                            lastm=name + text
                            time=message.time.toString()
                            dbChatsManage.setLastMesssage(lastm, userid)
                            dbChatsManage.setLastMesssageTime(time,userid)
                        }
                        val user= dbChatsManage.getChatById(userid)
                        chats_list.set(chatsname.indexOf(userid),User(user.name,user.Imageurl,user.userid,user.email,lastm,"",count.toString(),time))
                        liveData.value=chats_list
                    }

                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }











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

        databaseReference.child("users").child(idd).get().addOnCompleteListener{


             if(it.isSuccessful){
            val user=it.result.getValue(User::class.java)

            dbChatsManage.addChat(id!!,user!!)
            chats_list.add(user)
             if(flag)
             liveData.value=chats_list
        }

    }

        }

}
