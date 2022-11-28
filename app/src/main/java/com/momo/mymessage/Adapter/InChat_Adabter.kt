package com.momo.mymessage.Adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.momo.mymessage.ui.FullPhotoActivity
import com.momo.mymessage.R
import com.momo.mymessage.pogo.Message
import com.momo.mymessage.ui.Chat_Activity
import com.squareup.picasso.Picasso


class InChat_Adabter (val listPositions:ArrayList<String>, val l:ArrayList<Message>, val activity: Chat_Activity, val list:ArrayList<Message>, onListItemClickk: onListItemClick) : RecyclerView.Adapter<InChat_Adabter.Holder>() {


    var onListItem:onListItemClick

    init {
        this.onListItem=onListItemClickk
    }

    val id = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if (viewType == 1) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.chat_item_me, null, false)
            return Holder(view)

        } else if (viewType == 0) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.chat_item_other, null, false)
            return Holder(view)

        } else if (viewType == 4) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_imgmessage_seen_item, null, false)
            return Holder(view)

        } else if (viewType == 5) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_imgmessage_unseen_item, null, false)
            return Holder(view)

        } else if (viewType == 6) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_imgmessage_other_item, null, false)
            return Holder(view)

        } else if (viewType == 7) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_record_seen_item, null, false)
            return Holder(view)

        } else if (viewType == 8) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_record_unseen_item, null, false)
            return Holder(view)

        } else if (viewType == 9) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_record_other_item, null, false)
            return Holder(view)

        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.chat_seen_item, null, false)
            return Holder(view)

        }

    }
    var flag=0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {

        holder.message.setText(list.get(position).text)
        holder.time.setText(list.get(position).time)
        Picasso.get().load(list.get(position).imgurl).into(holder.messageimg)

        if(listPositions.size!=0&&listPositions.contains(list[position].id))
        holder.background.background=R.color.primary.toDrawable()
        else
        holder.background.background=null




        holder.itemView.setOnLongClickListener(object :View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {

                if(listPositions.contains(list[position].id)){
                    l.removeAt(listPositions.indexOf(list[position].id))
                    listPositions.remove(list[position].id)
                holder.background.background=null}
                else{
                listPositions.add(list[position].id!!)
                l.add(list.get(position))
                holder.background.background=R.color.primary.toDrawable()}
                onListItem.onItemClick(holder,l)

               return true
            }
        })

        holder.itemView.setOnClickListener{

            if(listPositions.size>0){
            if(listPositions.contains(list[position].id)){
                l.removeAt(listPositions.indexOf(list[position].id))
                listPositions.remove(list[position].id)
                holder.background.background=null

            }
            else{
                listPositions.add(list[position].id!!)
                l.add(list.get(position))
                holder.background.background=R.color.primary.toDrawable()}
            onListItem.onItemClick(holder,l)}

        }

        holder.messageimg.setOnClickListener{

            val intent=Intent(activity, FullPhotoActivity::class.java)
            intent.putExtra("photo",list[position].imgurl)
            activity.startActivity(intent)

        }

        var mediaPlayer:MediaPlayer=MediaPlayer()
        holder.startbutton.setOnClickListener {


            if (activity.isNetworkAvailable()) {



                if (flag == 0) {
                    flag = position
                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(
                        activity.baseContext,
                        list[position].record!!.toUri()!!
                    )
                    mediaPlayer.prepare()
                }
                    if(flag==position){
                holder.startbutton.visibility = View.GONE
                mediaPlayer.start()}







                holder.pausebutton.setOnClickListener {
                    holder.startbutton.visibility = View.VISIBLE

                    mediaPlayer.pause()
                     flag=position

                }
                mediaPlayer.setOnCompletionListener {
                    holder.startbutton.visibility = View.VISIBLE
                    mediaPlayer.release()
                    flag = 0

                }
            } else
                Toast.makeText(activity, "No Internet", Toast.LENGTH_SHORT).show()


        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView)  {


        val background=itemView.findViewById<RelativeLayout>(R.id.background)
        val  message = itemView.findViewById<TextView>(R.id.message_textView)
        val time = itemView.findViewById<TextView>(R.id.time)
        val messageimg = itemView.findViewById<ImageView>(R.id.message_img)
        val pausebutton = itemView.findViewById<AppCompatImageButton>(R.id.pause_button)
        val startbutton = itemView.findViewById<AppCompatImageButton>(R.id.start_button)



    }



    override fun getItemViewType(position: Int): Int {

        val message = list.get(position)
       return if (TextUtils.isEmpty(message.text)&&!message.record.toString()!!.equals("null")) {
            if (message.senderid.equals(id)) {
                if (message.seen.equals("seen"))
                    7
                else
                    8
            }
            else
                return 9


        }

        else if (message.senderid.equals(id)) {

            return if (!message.imgurl.equals("noImg")) {
                if (message.seen.equals("seen"))
                    4
                else
                    5

            } else {
                if (message.seen.equals("seen"))
                    3
                else
                    1
            }
        }

        else {

            return if (!message.imgurl.equals("noImg"))
                6
            else
                0
        }
    }




    interface onListItemClick{

        fun onItemClick(holder: Holder,positions:ArrayList<Message>)


    }





}







