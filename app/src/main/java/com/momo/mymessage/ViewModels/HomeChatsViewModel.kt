package com.momo.mymessage.ViewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.momo.mymessage.db.dbChatsManage
import com.momo.mymessage.pogo.Message
import com.momo.mymessage.pogo.User


class HomeChatsViewModel(application:Application) : AndroidViewModel(application) {


    private val context = getApplication<Application>().applicationContext
    private val id=FirebaseAuth.getInstance().currentUser!!.uid
    private val dbChatsManage= dbChatsManage(context)
    private var chats_list=dbChatsManage.getChats(id)
    private var chatsId=dbChatsManage.getChatsid(id)
    private var toupdateList=ArrayList<String>()
    private val databaseReference= FirebaseDatabase.getInstance().getReference()
    private var ref=databaseReference.child("users").child(id!!).child("messages")
    lateinit var ViewmodelListener:ChildEventListener
    private var count=0
    private var ii=0
    var flag=false
    var liveData=MutableLiveData<ArrayList<User>>()

   fun getChats(){

       if(chatsId.size>0)
           liveData.value = chats_list


       databaseReference.child("users").child(id!!).child("messages").get().addOnSuccessListener {
           count=it.children.count()

           if(count==0)
              liveData.postValue(chats_list)
           if(count<=chats_list.size)
               updateChats()


           ii=chats_list.size

           for (i in it.children){

           if(!chatsId.contains( i.key.toString())) {

               getChat(i.key.toString())
           }


           }



       }
   }




    fun updateChats() {

         flag=true
        toupdateList.addAll(chatsId)
        if(toupdateList.size>0)
         updateChatInfo(toupdateList[0])


        ViewmodelListener =   databaseReference.child("users").child(id!!).child("messages")
                .addChildEventListener(object :
                    ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        if (!chatsId.contains(snapshot.key.toString())) {
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
                var text: String
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
                        if(count!=user.useen?.toInt()||!user.last.equals(lastm)) {

                            user.last = lastm
                            user.time = time
                            user.useen = count.toString()

                            chats_list.removeAt(chatsId.indexOf(userid))
                            chatsId.remove(user.userid)
                            chats_list.add(0, user)
                            chatsId.add(0, user.userid!!)
                            dbChatsManage.deleteChat(id, user.userid)
                            dbChatsManage.addChat(id, user)
                            if (toupdateList.size == 0)
                                liveData.postValue(chats_list)


                        }

                    }
            }

    }

    fun addChat(user:User){

        if(user!=null&&!chatsId.contains(user.userid)) {
            dbChatsManage.addChat(id,user)
            chats_list .add(0,user)
            chatsId.add(0,user.userid!!)
            if(toupdateList.size==0)
             liveData.value=chats_list
        }

    }


    private fun getChat(idd: String) {

        databaseReference.child("users").child(idd).get().addOnSuccessListener{

            ii++

             val user=it.getValue(User::class.java)

             chatsId.add(0,idd)
             chats_list.add(0,user!!)
            dbChatsManage.addChat(id!!,user)

            if(flag)
             liveData.value=chats_list

            if(ii==count) {
                updateChats()

            }

        }


        }

    fun removeListener() {
        ref.removeEventListener(ViewmodelListener)
    }


}
