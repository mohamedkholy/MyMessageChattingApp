package com.momo.mymessage.db

import androidx.core.content.contentValuesOf
import com.momo.mymessage.pogo.Message

class dbMessagesManage(val context:android.content.Context) {
    val mydatabase=MyDataBase(context)


    fun addmessage(id_Of_OtherUser:String,currentuser: String,message: Message){
        val db=mydatabase.writableDatabase


        val values= contentValuesOf().apply {
            put("chatid",id_Of_OtherUser)
            put("text",message.text)
            put("time",message.time)
            put("sender",message.senderid)
            put("seen",message.seen)
            put("messageid",message.id)
            put("imgurl",message.imgurl)
            put("recordurl",message.record)
            put("user",currentuser)
        }
        db.insert("messages",null,values)

    }


    fun getmessages(id_Of_OtherUser: String,currentuser: String):ArrayList<Message>{
        val list=ArrayList<Message>()
        val db=mydatabase.readableDatabase
        val l= arrayOf(id_Of_OtherUser,currentuser)
         val cursor=db.rawQuery("select * from messages where chatid = ? and user=?",l)
       while (cursor.moveToNext()){

           list.add(Message(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7)))

       }
        return list

    }

    fun updateMessage(messageID:String){
        val db=mydatabase.writableDatabase
        val values= contentValuesOf().apply {
            put("seen","seen")
        }
        db.update("messages",values,"messageid=?", arrayOf(messageID))


    }





    fun getMessagesIDs(id_Of_OtherUser: String,currentuser: String):ArrayList<String>{
        val db=mydatabase.readableDatabase
        val l=ArrayList<String>()
        val cursor=db.rawQuery("select messageid from messages where chatid=? and user=?", arrayOf(id_Of_OtherUser,currentuser))
        while (cursor.moveToNext()){
            l.add(cursor.getString(0))
        }

        return l
    }


    fun deleteMessage(id:String){
        val db=mydatabase.writableDatabase

        db.delete("messages","messageid = ?",arrayOf(id))

    }



}