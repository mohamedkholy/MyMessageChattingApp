package com.momo.mymessage.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.content.contentValuesOf
import com.momo.mymessage.pogo.Message
import com.momo.mymessage.pogo.User
import java.util.*
import kotlin.collections.ArrayList

class db_chats_manage(val context: Context) {
val mydatabase=MyDataBase(context)



fun addChat(id:String,user: User):Boolean{
   val db:SQLiteDatabase=mydatabase.writableDatabase
    val value= contentValuesOf().apply {
        put("name",user.name)
        put("eamil",user.email)
        put("id",user.userid)
        put("imgurl",user.Imageurl)
        put("user",id)
        put("last",user.last)

    }

    val u:Long= db.insert("chats",null,value)

    return u!=-1L
}

    fun getChats(id:String):ArrayList<User>{
        val list=ArrayList<User>()
        val db=mydatabase.readableDatabase
       val l= arrayOf(id)
        val cursor=db.rawQuery("select * from chats where user =?",l)
        while (cursor.moveToNext()){

          list.add(User(cursor.getString(1),cursor.getString(3),cursor.getString(0),cursor.getString(2),cursor.getString(5)))


        }


        return list
    }
    fun getChatsid():ArrayList<String>{
        val list=ArrayList<String>()
        val db=mydatabase.readableDatabase
        val cursor=db.rawQuery("select id from chats",null)
        while (cursor.moveToNext()){
            list.add(cursor.getString(0))


        }


        return list
    }




    fun setLastMesssage(message:String,chatID:String){
        val db=mydatabase.writableDatabase
        val values= contentValuesOf().apply {
            put("last",message)
        }
       db.update("chats",values,"id=?", arrayOf(chatID))


    }


    fun getLastMesssage(chatID:String):String{
        val db=mydatabase.readableDatabase
        val cursor=db.rawQuery("select last from chats where id=?", arrayOf(chatID))
        while (cursor.moveToNext()){
            if(cursor.getString(0)!=null)
            return cursor.getString(0)

        }
        return ""
    }

    fun updateunseen(i:String,chatID: String){

        val db=mydatabase.writableDatabase
        val values= contentValuesOf().apply {
            put("unseen",i)
        }
        db.update("chats",values,"id=?", arrayOf(chatID))

    }

    fun getunseen(chatID: String):String{

        val db=mydatabase.readableDatabase
        val cursor=db.rawQuery("select unseen from chats where id=?", arrayOf(chatID))
        while (cursor.moveToNext()){
            if(cursor.getString(0)!=null)
                return cursor.getString(0)

        }
        return "0"

    }

    fun updateChatname(name:String,chatID: String){
        val db=mydatabase.writableDatabase
        val values= contentValuesOf().apply {
            put("name",name)
        }
        db.update("chats",values,"id=?", arrayOf(chatID))

    }

    fun updateChatimg(imgurl:String,chatID: String){
        val db=mydatabase.writableDatabase
        val values= contentValuesOf().apply {
            put("imgurl",imgurl)
        }
        db.update("chats",values,"id=?", arrayOf(chatID))

    }


}