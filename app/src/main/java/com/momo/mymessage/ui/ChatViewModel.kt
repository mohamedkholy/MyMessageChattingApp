package com.momo.mymessage.ui

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.momo.mymessage.R
import com.momo.mymessage.db.db_messages_manage
import com.momo.mymessage.pogo.Message

class ChatViewModel(val activity: Chat_Activity, val userid:String, val id:String):ViewModel() {


   private val dbMessagesManage=db_messages_manage(activity)
   private  var  idList=ArrayList<String>()
   private  var  list=ArrayList<Message>()
   val liveData=MutableLiveData<ArrayList<Message>>(list)
    val databaseReference=FirebaseDatabase.getInstance().getReference().child("users")
   val ref=  databaseReference.child(id!!).child("messages").child(userid).child("chats")
    lateinit var listner:ChildEventListener
    val incoming_Message=MediaPlayer.create(activity,R.raw.incoming)
    var flag=0
    lateinit var x:String



    @RequiresApi(Build.VERSION_CODES.M)
    fun get_messages(){

        list=dbMessagesManage.getmessages(userid,id!!)
        idList=dbMessagesManage.getMessagesIDs(userid,id!!)
        liveData.value=list


        var listt=ArrayList<Message>()
        databaseReference.child(id!!).child("messages").child(userid).child("chats").get() .addOnCompleteListener{
            if(it.isSuccessful){
                val sp=activity.getSharedPreferences("CurrentUser",0)
                 x= sp.getString("userid",null).toString()
                for (i in it.result.children) {
                    val message = i.getValue(Message::class.java)!!
                    if(!list.contains(message)||(message.seen.equals("unseen")&&message.senderid.equals(userid))){
                        listt.add(message)
                    }
                }
                for (i in 0 until listt.size){
                    val message = listt[i]



                    save_message(message)


                }


            }



            flag=1
            Messages_listner()


    }


    }

    private fun Messages_listner() {


          listner=ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message=snapshot.getValue(Message::class.java)
                if(!idList.contains(message!!.id))
                    save_message(message!!)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message=snapshot.getValue(Message::class.java)

                save_message(message!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot){}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError){}


        })
    }

    private fun save_message(message: Message) {

        if(x.equals(userid)){
        if (!message.seen.equals("seen")&&message.senderid.equals(userid)){

            val map= hashMapOf<String,Any>()
            map.put("seen","seen")

            databaseReference.child(userid).child("messages").child(id!!).child("chats").child(message.id!!).updateChildren(map)
            if(!idList.contains(message.id))
                incoming_Message.start()
            list.add(message)
            idList.add(message.id!!)
            dbMessagesManage.addmessage(userid, id!!, message)


        }

        else if(idList.contains(message.id)&&message.seen.equals("seen")&&message.senderid.equals(id)) {

            list.set(idList.indexOf(message.id),Message(message.text,message.time, message.senderid, "seen",
                message.id, message.imgurl, message.record))!!
            dbMessagesManage.updateMessage(message.id!!)

        }

        else {



            list.add(message)
            idList.add(message.id!!)
            dbMessagesManage.addmessage(userid, id!!, message)




        }

        liveData.value=list
        }
    }

     fun deletMessages(deletelist: ArrayList<Message>) {

        for (i in deletelist)
        {
            databaseReference.child(id!!).child("messages").child(userid).child("chats").child(i.id!!).removeValue()
            if(i.senderid!!.equals(id))
                databaseReference.child(userid!!).child("messages").child(id).child("chats").child(i.id!!).removeValue()
            list.remove(i)
            dbMessagesManage.deleteMessage(i.id)

        }

        liveData.value=list


    }



}




