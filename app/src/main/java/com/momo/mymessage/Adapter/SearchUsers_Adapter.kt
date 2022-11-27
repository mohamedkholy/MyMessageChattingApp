package com.momo.mymessage.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.momo.mymessage.R
import com.momo.mymessage.databinding.UserListItemBinding
import com.momo.mymessage.pogo.User

import com.momo.mymessage.ui.home
import com.squareup.picasso.Picasso

class SearchUsers_Adapter(options: FirebaseRecyclerOptions<User>,var context: Activity) :
    FirebaseRecyclerAdapter<User, SearchUsers_Adapter.Holder>(options) {

val id=FirebaseAuth.getInstance().uid


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
       val binding = UserListItemBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.user_list_item, null, false)
        )

        var holder= SearchUsers_Adapter.Holder(binding)
        return holder

    }

    override fun onBindViewHolder(holder: Holder, position: Int, model: User) {
        if(!model.userid.equals(id)) {
            holder.binding.email.setText(model.email)
            holder.binding.name.setText(model.name)}
        else{
            holder.binding.name.setText("You")

        }
            Picasso.get().load(model.Imageurl).into(holder.binding.img)

            holder.binding.root.setOnClickListener {

                val intent = Intent()
                if(!model.userid.equals(id)) {
                    intent.putExtra(
                        "user",
                        User(model.name, model.Imageurl, model.userid, model.email))
                    context.setResult(1, intent)
                    context.finish()
                }
                else{
                    intent.putExtra(
                        "user",
                        User("me", model.Imageurl, model.userid, model.email))
                    context.setResult(1, intent)
                    context.finish()

                }


        }
    }


    class Holder(var binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root)  {

    }




}