package com.momo.mymessage.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import com.momo.mymessage.pogo.User
import kotlin.collections.ArrayList

class dbChatsManage(val context: Context) {
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
        put("time",user.time)
        put("unseen",user.useen)

    }

    val u:Long= db.insert("chats",null,value)

    return u!=-1L
}

fun deleteChat(id:String,userid:String){
  val db=mydatabase.writableDatabase
  db.delete("chats","id=? and user=? ", arrayOf(userid,id))

}

    fun getChats(id:String):ArrayList<User>{
        val list=ArrayList<User>()
        val db=mydatabase.readableDatabase
       val l= arrayOf(id)
        val cursor=db.rawQuery("select * from chats where user =? ORDER by order1 DESC",l)
        while (cursor.moveToNext()){

          list.add(User(cursor.getString(1),cursor.getString(3),cursor.getString(0),cursor.getString(2),cursor.getString(5),"",cursor.getString(6),cursor.getString(7)))


        }


        return list
    }
    fun getChatsid(id:String):ArrayList<String>{
        val list=ArrayList<String>()
        val db=mydatabase.readableDatabase
        val cursor=db.rawQuery("select id from chats where user=?  ORDER by order1 DESC ",
            arrayOf(id)
        )
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

    fun setLastMesssageTime(time:String,chatID:String){
        val db=mydatabase.writableDatabase
        val values= contentValuesOf().apply {
            put("time",time)
        }
        db.update("chats",values,"id=?", arrayOf(chatID))


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


    fun getChatById(chatID: String,id: String):User{

        val db=mydatabase.readableDatabase
        val cursor=db.rawQuery("select * from chats where id=? and user=?", arrayOf(chatID,id))
        while (cursor.moveToNext()){
            return User(cursor.getString(1),cursor.getString(3),cursor.getString(0),cursor.getString(2),cursor.getString(5),"",cursor.getString(6),cursor.getString(7))

        }

        return User()

    }



}