package com.momo.mymessage.ViewModels

import android.app.Application
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.momo.mymessage.R
import com.momo.mymessage.db.dbMessagesManage
import com.momo.mymessage.pogo.Message


class ChatViewModel(application: Application):AndroidViewModel(application) {


    private val context=getApplication<Application>().applicationContext
    private val id= FirebaseAuth.getInstance().currentUser!!.uid
    private val dbMessagesManage=dbMessagesManage(context)
    private  var  idList=ArrayList<String>()
    private  var  list=ArrayList<Message>()
    val liveData=MutableLiveData<ArrayList<Message>>(list)
    private val incoming_Message=MediaPlayer.create(context,R.raw.incoming)
    var flag=0
    private lateinit var x:String
    private lateinit var ChatViewModelListner:ChildEventListener
    private val sp=context.getSharedPreferences("CurrentUser",0)
    private val userid= sp.getString("userid",null).toString()
    private val ref= FirebaseDatabase.getInstance().getReference().child("users").child(id!!).child("messages").child(userid).child("chats")

    @RequiresApi(Build.VERSION_CODES.M)
    fun get_messages(){


        if(list.size==0){
        list=dbMessagesManage.getmessages(userid,id!!)
        idList=dbMessagesManage.getMessagesIDs(userid,id!!)}
        liveData.value=list


        var listt=ArrayList<Message>()
        ref.get() .addOnCompleteListener{
            if(it.isSuccessful){

                for (i in it.result.children) {
                    val message = i.getValue(Message::class.java)!!
                    if(!list.contains(message)||(message.seen.equals("unseen")&&message.senderid.equals(userid))){
                        listt.add(message)
                    }
                }
                for (i in 0 until listt.size){
                    val message = listt[i]



                    saveMessage(message)


                }


            }



            flag=1
            FireMessagesListner()


    }


    }


    private fun FireMessagesListner() {

          ChatViewModelListner=ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message=snapshot.getValue(Message::class.java)
                if(!idList.contains(message!!.id))
                    saveMessage(message!!)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                val message=snapshot.getValue(Message::class.java)
                if(message!!.senderid.equals(id))
                    saveMessage(message)
            }

            override fun onChildRemoved(snapshot: DataSnapshot){}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError){}

        })
    }


    private fun saveMessage(message: Message) {


        if (!message.seen.equals("seen")&&message.senderid.equals(userid)){

            val map= hashMapOf<String,Any>()
            map.put("seen","seen")

            FirebaseDatabase.getInstance().getReference().child("users")
                .child(userid).child("messages").child(id!!).child("chats").child(message.id!!).updateChildren(map)
            FirebaseDatabase.getInstance().getReference().child("users")
            .child(id).child("messages").child(userid!!).child("chats").child(message.id!!).updateChildren(map)
            if(!idList.contains(message.id)){
                incoming_Message.start()

            list.add(message)
            idList.add(message.id!!)
            dbMessagesManage.addmessage(userid, id!!, message)}


        }

        else if(idList.contains(message.id)&&message.seen.equals("seen")&&message.senderid.equals(id)) {

            list.set(idList.indexOf(message.id),Message(message.text,message.time, message.senderid, "seen",
                message.id, message.imgurl, message.record))!!
            dbMessagesManage.updateMessage(message.id!!)

        }

        else {


            if(!idList.contains(message.id)){
            list.add(message)
            idList.add(message.id!!)
            dbMessagesManage.addmessage(userid, id!!, message)}




        }

        liveData.value=list

    }

     fun deletMessages(deletelist: ArrayList<Message>) {

        for (i in deletelist)
        {
            Log.d("wwwwwwwwwwww","ggggggggggggg")
            FirebaseDatabase.getInstance().getReference("users").child(id!!).child("messages").child(userid).child("chats").child(i.id!!).removeValue()
            if(i.senderid!!.equals(id)&&i.seen.equals("unseen"))
                FirebaseDatabase.getInstance().getReference("users").child(userid!!).child("messages").child(id).child("chats").child(i.id!!).removeValue()
            list.remove(i)
            idList.remove(i.id)
            dbMessagesManage.deleteMessage(i.id)

        }

        liveData.value=list


    }

    fun removeListener() {
        ref.removeEventListener(ChatViewModelListner)
    }


}




