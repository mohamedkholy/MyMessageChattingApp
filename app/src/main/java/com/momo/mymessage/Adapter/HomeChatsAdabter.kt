package com.momo.mymessage.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.momo.mymessage.R
import com.momo.mymessage.databinding.ChatOutItemBinding
import com.momo.mymessage.db.dbChatsManage
import com.momo.mymessage.pogo.User
import com.momo.mymessage.ui.ChatActivity
import com.squareup.picasso.Picasso

class HomeChatsAdabter(var activity: Activity, val list:ArrayList<User>) : RecyclerView.Adapter<HomeChatsAdabter.Holder>() {

val ref=FirebaseDatabase.getInstance().getReference().child("users")
val id=FirebaseAuth.getInstance().currentUser!!.uid
val dbChatsManage=dbChatsManage(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
       val binding=ChatOutItemBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.chat_out_item,null,false))
        val holder=Holder(binding)
        return holder

    }

    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {

        try {


        if(list.get(position).userid!!.equals(id))
            holder.binding.name.setText("You")
        else
            holder.binding.name.setText(list.get(position).name)

        Picasso.get().load(list.get(position).Imageurl).into(holder.binding.img)

        if(list.get(position).last.equals(null)||list.get(position).last.equals(""))
            holder.binding.lastMessage.visibility=View.GONE
        else
            holder.binding.lastMessage.visibility=View.VISIBLE

        holder.binding.lastMessage.setText(list.get(position).last)

        holder.binding.time.text=list[position].time

        holder.binding.unseen.setText(list[position].useen)
        if(list[position].useen.equals("0")||list[position].useen==null)
            holder.binding.unseen.visibility=View.GONE
        else
            holder.binding.unseen.visibility=View.VISIBLE








        ref.child(list.get(position).userid!!).child("status").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.getValue().toString().equals("online"))
                    holder.binding.status.visibility=View.VISIBLE
                else
                    holder.binding.status.visibility=View.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {}
        })






        holder.binding.root.setOnClickListener{
            var intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("user",User(list.get(position).name,list.get(position).Imageurl,list.get(position).userid,list.get(position).email))
            activity.startActivity(intent)
        }



      ref.child(list[position].userid!!).child("name").addValueEventListener(object :ValueEventListener{
          override fun onDataChange(snapshot: DataSnapshot) {
              val name=snapshot.getValue().toString()

              if(!name.equals("null")&&!list.get(position).userid!!.equals(id))
               holder.binding.name.setText(name)

              dbChatsManage.updateChatname(name,list.get(position).userid!!)
          }

          override fun onCancelled(error: DatabaseError) {

          }
      })


        ref.child(list[position].userid!!).child("Imageurl").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val img=snapshot.getValue().toString()

               if(!img.equals("null")){
                Picasso.get().load(img).into(holder.binding.img)

                dbChatsManage.updateChatimg(img,list.get(position).userid!!)}
                }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        }
        catch (e:Exception){
            Log.d("gggggggggggggggggggggg","gg  "+e.localizedMessage)
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    class Holder(val binding:  ChatOutItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}