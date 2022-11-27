package com.momo.mymessage.db

import android.content.Context

class ClearDatabase(context: Context) {

    val myDataBase=MyDataBase(context)

    fun clearAll(){

        val db=myDataBase.writableDatabase
        db.delete("chats",null,null)
        db.delete("messages",null,null)



    }

    fun clearUnseen(){

        val db=myDataBase.writableDatabase
        db.delete("messages","seen = ?",arrayOf("unseen"))




    }


}