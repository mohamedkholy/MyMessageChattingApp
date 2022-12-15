package com.momo.mymessage.Adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.momo.mymessage.R
import com.momo.mymessage.pogo.Message
import com.momo.mymessage.ui.ChatActivity
import com.momo.mymessage.ui.FullPhotoActivity
import com.squareup.picasso.Picasso
import kotlin.system.measureNanoTime


class ChatAdabter (val listPositions:ArrayList<String>, val l:ArrayList<Message>, val activity: ChatActivity, val list:ArrayList<Message>, onListItemClickk: onListItemClick) : RecyclerView.Adapter<ChatAdabter.Holder>() {


    var onListItem:onListItemClick
    var h:Int=0
    var s:Int=0
    var m:Int=0
    var flag=0
    val mediaPlayer=MediaPlayer()


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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {

        holder.seekbar.max=100

        holder.message.setText(list.get(position).text)
        holder.time.setText(list.get(position).time)
        Picasso.get().load(list.get(position).imgurl).into(holder.messageimg)

        if(listPositions.size!=0&&listPositions.contains(list[position].id))
         holder.background.background=R.color.blue.toDrawable()
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
                holder.background.background=R.color.blue.toDrawable()}
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
                holder.background.background=R.color.blue.toDrawable()}
            onListItem.onItemClick(holder,l)}

        }

        holder.messageimg.setOnClickListener{

            val intent=Intent(activity, FullPhotoActivity::class.java)
            intent.putExtra("photo",list[position].imgurl)
            activity.startActivity(intent)

        }




        holder.seekbar.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
            override fun onProgressChanged(v: SeekBar?, p1: Int, p2: Boolean) {

                    val seekBar=v as SeekBar
                    mediaPlayer.seekTo((mediaPlayer.duration / 100) * seekBar.progress)
                    holder.recordDuration.setText(getduration(mediaPlayer.duration - mediaPlayer.currentPosition))


                }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })


        holder.startbutton.setOnClickListener {

            if (activity.isNetworkAvailable()) {

                    if(flag==position)
                    {
                        if (mediaPlayer.isPlaying) {
                            holder.startbutton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            mediaPlayer.pause()
                        }
                        else{
                        holder.startbutton.setImageResource(R.drawable.ic_baseline_pause_24)
                        mediaPlayer.start()
                        updateseekbar(holder)
                        }
                    }

                    else
                    {
                        if(!mediaPlayer.isPlaying) {
                            mediaPlayer.reset()
                            flag = position
                            mediaPlayer.setDataSource(
                                activity.baseContext,
                                list[position].record!!.toUri()!!
                            )
                            mediaPlayer.prepareAsync()
                            mediaPlayer.setOnPreparedListener {
                                holder.startbutton.setImageResource(R.drawable.ic_baseline_pause_24)
                                it.start()
                                val playposition = mediaPlayer.duration / 100 * holder.seekbar.progress
                                mediaPlayer.seekTo(playposition)
                                holder.recordDuration.setText(getduration(it.currentPosition))
                                updateseekbar(holder)

                            }

                        }
                    }


            } else
                Toast.makeText(activity, "No Internet", Toast.LENGTH_SHORT).show()


            mediaPlayer.setOnCompletionListener {
                holder.startbutton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                holder.recordDuration.setText(getduration(mediaPlayer.duration))
                holder.seekbar.progress=0
            }

        }


    }

    private fun updateseekbar(holder: Holder) {
        if(mediaPlayer.isPlaying){
        val seekProgress=((mediaPlayer.currentPosition.toFloat()/mediaPlayer.duration)*100).toInt()
        holder.seekbar.progress=seekProgress
        holder.recordDuration.setText(getduration(mediaPlayer.duration-mediaPlayer.currentPosition))
        Handler(Looper.getMainLooper()).postDelayed({updateseekbar(holder)},1000)}
    }

    private fun getduration(duration: Int): String {

        var time=""
        h=0
        m=0
        s=0
        var timeBySeconds=duration/1000
        if(timeBySeconds>=3600){
           h=timeBySeconds/3600
            timeBySeconds=timeBySeconds-h*3600

            if(h<=9)
                time+="0"+h.toString()+":"
            else
                time+=h.toString()+":"
        }
        if(timeBySeconds>60)
        {
             m=timeBySeconds/60
            timeBySeconds=timeBySeconds-m*60


        }
        if(m<=9)
            time+="0"+m.toString()+":"
        else
            time+=m.toString()+":"

        if(timeBySeconds>0)
        {
             s=timeBySeconds

        }
        if(s<=9)
            time+="0"+s.toString()
        else
            time+=s.toString()

        return time
    }


    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView)  {


        val background=itemView.findViewById<RelativeLayout>(R.id.background)
        val  message = itemView.findViewById<TextView>(R.id.message_textView)
        val time = itemView.findViewById<TextView>(R.id.time)
        val messageimg = itemView.findViewById<ImageView>(R.id.message_img)
        val startbutton = itemView.findViewById<AppCompatImageButton>(R.id.start_button)
        val seekbar=itemView.findViewById<SeekBar>(R.id.seekbar)
        val recordDuration=itemView.findViewById<TextView>(R.id.recordDuration)


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







